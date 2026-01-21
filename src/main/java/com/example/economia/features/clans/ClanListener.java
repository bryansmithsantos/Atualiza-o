package com.example.economia.features.clans;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.example.economia.features.messages.Messages;
import com.example.economia.features.tags.TagService;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ClanListener implements Listener {

    private final ClanService clanService;
    private TagService tagService;

    public ClanListener(ClanService clanService) {
        this.clanService = clanService;
    }

    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Clan clan = clanService.getClan(player.getUniqueId());
        String serverTag = tagService != null ? tagService.getFormattedTag(player) : "";

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            Component prefix = Component.empty();

            // Add server tag first
            if (!serverTag.isEmpty()) {
                prefix = prefix.append(LegacyComponentSerializer.legacySection().deserialize(serverTag));
            }

            // Add clan tag
            if (clan != null) {
                prefix = prefix.append(Component.text("[" + clan.getTag() + "] ", NamedTextColor.GRAY));
            }

            return prefix
                    .append(sourceDisplayName)
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(message);
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updateTabAndTag(event.getPlayer());
    }

    public void updateTabAndTag(Player player) {
        Clan clan = clanService.getClan(player.getUniqueId());
        String serverTag = tagService != null ? tagService.getFormattedTag(player) : "";

        Component tabName = Component.empty();

        // Server tag
        if (!serverTag.isEmpty()) {
            tabName = tabName.append(LegacyComponentSerializer.legacySection().deserialize(serverTag));
        }

        // Clan tag
        if (clan != null) {
            tabName = tabName.append(Component.text("[" + clan.getTag() + "] ", NamedTextColor.GRAY));
        }

        tabName = tabName.append(Component.text(player.getName(), NamedTextColor.WHITE));

        player.playerListName(tabName);
        player.displayName(tabName);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker) {
            Clan c1 = clanService.getClan(victim.getUniqueId());
            Clan c2 = clanService.getClan(attacker.getUniqueId());

            if (c1 != null && c2 != null && c1.getId().equals(c2.getId())) {
                if (!c1.isFriendlyFire()) {
                    event.setCancelled(true);
                    Messages.warning(attacker, "Fogo amigo desativado!");
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        Clan victimClan = clanService.getClan(victim.getUniqueId());
        if (victimClan != null) {
            victimClan.addDeath();
            clanService.save(); // Salvar a cada morte pode ser pesado, ideal seria async ou batch, mas ok por
                                // agora
        }

        if (killer != null) {
            Clan killerClan = clanService.getClan(killer.getUniqueId());
            if (killerClan != null) {
                killerClan.addKill();
                clanService.save();
            }
        }
    }
}

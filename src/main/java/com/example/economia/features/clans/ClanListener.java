package com.example.economia.features.clans;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.example.economia.features.messages.Messages;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ClanListener implements Listener {

    private final ClanService clanService;

    public ClanListener(ClanService clanService) {
        this.clanService = clanService;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncChatEvent event) {
        // Player player = event.getPlayer();
        // Clan clan = clanService.getClan(player.getUniqueId());

        // if (clan != null) {
        // Component original = event.renderer().render(player, player.displayName(),
        // event.message(), player);
        // Component prefix = Component.text("[" + clan.getTag() + "] ",
        // NamedTextColor.GRAY);
        // Simples prefixo: [TAG] Player: Msg
        // }
    }

    // Usando formatação de chat mais compatível com Paper moderno
    // Mas para garantir que a tag apareça, vamos usar uma abordagem de alterar o
    // display name temporariamente ou usar Team/Scoreboard
    // Vamos usar a abordagem de Team para TAB e DisplayName para Chat

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updateTabAndTag(event.getPlayer());
    }

    public void updateTabAndTag(Player player) {
        Clan clan = clanService.getClan(player.getUniqueId());
        if (clan != null) {
            Component tabName = Component.text("[" + clan.getTag() + "] ", NamedTextColor.GRAY)
                    .append(Component.text(player.getName(), NamedTextColor.WHITE));
            player.playerListName(tabName);
            player.displayName(tabName); // Atualiza também o display name para o chat pegar
        } else {
            player.playerListName(Component.text(player.getName()));
            player.displayName(Component.text(player.getName()));
        }
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

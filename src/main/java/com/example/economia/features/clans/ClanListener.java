package com.example.economia.features.clans;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.example.economia.features.messages.Messages;
import com.example.economia.features.tags.TagService;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ClanListener implements Listener {

    private final ClanService clanService;
    private TagService tagService;
    private Plugin plugin;
    private int glowTaskId = -1;

    public ClanListener(ClanService clanService) {
        this.clanService = clanService;
    }

    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
        startGlowTask();
    }

    public void stopGlowTask() {
        if (glowTaskId != -1) {
            Bukkit.getScheduler().cancelTask(glowTaskId);
            glowTaskId = -1;
        }
    }

    private void startGlowTask() {
        if (plugin == null)
            return;

        // Update glow every 2 seconds
        glowTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateGlow(player);
            }
        }, 40L, 40L);
    }

    private void updateGlow(Player player) {
        Clan playerClan = clanService.getClan(player.getUniqueId());

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player))
                continue;

            Clan otherClan = clanService.getClan(other.getUniqueId());

            // Same clan = glowing green
            if (playerClan != null && otherClan != null &&
                    playerClan.getId().equals(otherClan.getId())) {
                // Make clan member glow for this player
                showGlowForPlayer(player, other, NamedTextColor.GREEN);
            } else {
                // Remove glow
                hideGlowForPlayer(player, other);
            }
        }
    }

    private void showGlowForPlayer(Player viewer, Player target, NamedTextColor color) {
        Scoreboard scoreboard = viewer.getScoreboard();
        String teamName = "clan_" + target.getName().substring(0, Math.min(target.getName().length(), 10));

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.color(color);
        team.addEntry(target.getName());

        // Apply glowing effect
        target.setGlowing(true);
    }

    private void hideGlowForPlayer(Player viewer, Player target) {
        Scoreboard scoreboard = viewer.getScoreboard();
        String teamName = "clan_" + target.getName().substring(0, Math.min(target.getName().length(), 10));

        Team team = scoreboard.getTeam(teamName);
        if (team != null) {
            team.removeEntry(target.getName());
        }

        // Check if anyone still needs them glowing
        boolean anyoneNeedsGlow = false;
        Clan targetClan = clanService.getClan(target.getUniqueId());
        if (targetClan != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(target))
                    continue;
                Clan pClan = clanService.getClan(p.getUniqueId());
                if (pClan != null && pClan.getId().equals(targetClan.getId())) {
                    anyoneNeedsGlow = true;
                    break;
                }
            }
        }

        if (!anyoneNeedsGlow) {
            target.setGlowing(false);
        }
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

        // Delay glow update to let scoreboard setup
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                updateGlow(event.getPlayer());
            }, 20L);
        }
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
            clanService.save();
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

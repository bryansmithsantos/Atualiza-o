package com.example.economia.features.dungeon;

import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DungeonListener implements Listener {

    private final DungeonService dungeonService;

    public DungeonListener(DungeonService dungeonService) {
        this.dungeonService = dungeonService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        DungeonSession session = dungeonService.getSession(player);

        if (session != null && session.isActive()) {
            // Cancel drops in dungeon
            event.getDrops().clear();
            event.setDroppedExp(0);

            // Save for respawn
            dungeonService.handlePlayerDeath(player);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // Delay to let normal respawn happen first
        dungeonService.handlePlayerRespawn(event.getPlayer());
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        // Check if in any dungeon arena
        for (var entry : dungeonService.getActiveSessions().entrySet()) {
            DungeonSession session = entry.getValue();
            if (session.isActive() && session.getArenaCenter() != null) {
                try {
                    if (entity.getLocation().distance(session.getArenaCenter()) < 60) {
                        // It's a dungeon mob
                        if (entity instanceof IronGolem && entity.equals(session.getBoss())) {
                            // Boss killed
                            session.onBossKill();
                            dungeonService.giveRewards(session, session.getDifficulty().getMaxReward() * 1.5);
                        } else if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                            session.onMobKill();
                        }
                        break;
                    }
                } catch (Exception e) {
                    // Different worlds, skip
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DungeonSession session = dungeonService.getSession(player);

        if (session != null) {
            // Player left during dungeon
            session.getPlayers().remove(player.getUniqueId());

            if (session.getPlayers().isEmpty()) {
                session.defeat();
                dungeonService.endSession(session);
            }
        }
    }
}

package com.example.economia.features.scoreboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerJoinListener implements Listener {

    private final ScoreboardService scoreboardService;

    public PlayerJoinListener(ScoreboardService scoreboardService) {
        this.scoreboardService = scoreboardService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        scoreboardService.applyScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        scoreboardService.removeScoreboard(event.getPlayer());
    }
}

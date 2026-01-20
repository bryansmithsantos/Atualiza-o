package com.example.economia.features.economy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class EconomyListener implements Listener {

    private final EconomyService economyService;

    public EconomyListener(EconomyService economyService) {
        this.economyService = economyService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        economyService.ensureAccount(event.getPlayer());
    }
}

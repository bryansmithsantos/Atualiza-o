package com.example.economia.features.auth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.example.economia.features.gui.AuthGui;

public final class AuthListener implements Listener {

    private final AuthService authService;

    public AuthListener(AuthService authService) {
        this.authService = authService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        authService.setLoggedIn(player.getUniqueId(), false);
        if (authService.isRegistered(player.getUniqueId())) {
            AuthGui.openLogin(player);
        } else {
            AuthGui.openRegister(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        authService.setLoggedIn(event.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (authService.isLoggedIn(player.getUniqueId())) {
            return;
        }
        String message = event.getMessage().toLowerCase();
        if (message.startsWith("/login") || message.startsWith("/register") || message.startsWith("/painel")) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage("Você precisa fazer login pelo painel.");
    }
}

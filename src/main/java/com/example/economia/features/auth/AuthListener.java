package com.example.economia.features.auth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.example.economia.features.gui.AuthGui;
import com.example.economia.features.messages.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import java.time.Duration;

public final class AuthListener implements Listener {

    private final AuthService authService;

    public AuthListener(AuthService authService) {
        this.authService = authService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        authService.setLoggedIn(player.getUniqueId(), false);

        // Mensagem de boas-vindas bonita
        player.sendMessage(Component.empty());
        player.sendMessage(
                Component.text("  ").append(Component.text("═══════════════════════════════", NamedTextColor.GOLD)));
        player.sendMessage(Component.text("      ")
                .append(Component.text("✦ ", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("BLINDED", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text(" ✦", NamedTextColor.LIGHT_PURPLE)));
        player.sendMessage(
                Component.text("  ").append(Component.text("═══════════════════════════════", NamedTextColor.GOLD)));
        player.sendMessage(Component.empty());

        if (authService.isRegistered(player.getUniqueId())) {

            if (authService.tryAutoLogin(player)) {
                Messages.success(player, "🔓 Login automático realizado! (Sessão ativa)");
                return;
            }

            // Título de login
            Title loginTitle = Title.title(
                    Component.text("🔐 Login Necessário").color(NamedTextColor.GOLD),
                    Component.text("Digite sua senha no chat").color(NamedTextColor.YELLOW),
                    Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(5), Duration.ofMillis(500)));
            player.showTitle(loginTitle);

            Messages.info(player, "Bem-vindo de volta, " + player.getName() + "!");
            Messages.warning(player, "Digite sua senha no chat para fazer login.");
            AuthGui.openLogin(player);
        } else {
            // Título de registro
            Title registerTitle = Title.title(
                    Component.text("👋 Novo Jogador!").color(NamedTextColor.AQUA),
                    Component.text("Crie sua conta para jogar").color(NamedTextColor.GREEN),
                    Title.Times.times(Duration.ofMillis(300), Duration.ofSeconds(5), Duration.ofMillis(500)));
            player.showTitle(registerTitle);

            Messages.info(player, "Olá, " + player.getName() + "! Você é novo aqui!");
            Messages.info(player, "Crie uma senha digitando no chat.");
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
        Messages.error(player, "Você precisa fazer login primeiro!");
    }
}

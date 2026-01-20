package com.example.economia.features.auth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.gui.MainMenuGui;
import com.example.economia.features.jobs.JobsService;
import com.example.economia.features.jobs.WorkService;
import com.example.economia.features.messages.Messages;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import java.time.Duration;

public final class AuthInputListener implements Listener {

    private final AuthService authService;
    private final EconomyService economyService;
    private final JobsService jobsService;
    private final WorkService workService;

    public AuthInputListener(AuthService authService, EconomyService economyService, JobsService jobsService,
            WorkService workService) {
        this.authService = authService;
        this.economyService = economyService;
        this.jobsService = jobsService;
        this.workService = workService;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        AuthRequest request = authService.consumePending(player);
        if (request == null) {
            return;
        }
        event.setCancelled(true);
        String password = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (password.equalsIgnoreCase("cancelar")) {
            Messages.warning(player, "Ação cancelada.");
            return;
        }

        if (password.length() < authService.getMinPasswordLength()) {
            Messages.error(player, "Senha muito curta! Mínimo: " + authService.getMinPasswordLength() + " caracteres.");
            return;
        }

        if (request == AuthRequest.REGISTER) {
            if (!authService.register(player, password)) {
                Messages.error(player, "Você já possui uma conta!");
                return;
            }
            authService.setLoggedIn(player.getUniqueId(), true);
            economyService.ensureAccount(player);

            // Título de boas-vindas
            Title welcomeTitle = Title.title(
                    Component.text("🎉 Bem-vindo!").color(NamedTextColor.GREEN),
                    Component.text("Conta criada com sucesso").color(NamedTextColor.YELLOW),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(500)));
            player.showTitle(welcomeTitle);

            Messages.success(player, "Conta criada com sucesso! Seja bem-vindo ao servidor!");
            player.getScheduler().runDelayed(player.getServer().getPluginManager().getPlugin("Blinded"),
                    task -> MainMenuGui.open(player, economyService, jobsService, workService),
                    null, 20L);
            return;
        }

        if (authService.verify(player, password)) {
            authService.setLoggedIn(player.getUniqueId(), true);
            economyService.ensureAccount(player);

            // Título de login
            Title loginTitle = Title.title(
                    Component.text("👋 Olá, " + player.getName() + "!").color(NamedTextColor.AQUA),
                    Component.text("Login efetuado com sucesso").color(NamedTextColor.GREEN),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(500)));
            player.showTitle(loginTitle);

            Messages.success(player, "Login efetuado! Bom jogo!");
            player.getScheduler().runDelayed(player.getServer().getPluginManager().getPlugin("Blinded"),
                    task -> MainMenuGui.open(player, economyService, jobsService, workService),
                    null, 20L);
            return;
        }

        Messages.error(player, "Senha incorreta! Tente novamente.");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }
}

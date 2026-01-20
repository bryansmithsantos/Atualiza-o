package com.example.economia.features.auth;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.gui.MainMenuGui;
import com.example.economia.features.jobs.JobsService;
import com.example.economia.features.jobs.WorkService;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class AuthInputListener implements Listener {

    private final AuthService authService;
    private final EconomyService economyService;
    private final JobsService jobsService;
    private final WorkService workService;

    public AuthInputListener(AuthService authService, EconomyService economyService, JobsService jobsService, WorkService workService) {
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
            player.sendMessage("Ação cancelada.");
            return;
        }
        if (password.length() < authService.getMinPasswordLength()) {
            player.sendMessage("Senha muito curta. Mínimo: " + authService.getMinPasswordLength());
            return;
        }
        if (request == AuthRequest.REGISTER) {
            if (!authService.register(player, password)) {
                player.sendMessage("Você já possui conta.");
                return;
            }
            authService.setLoggedIn(player.getUniqueId(), true);
            economyService.ensureAccount(player);
            player.sendMessage("Conta criada com sucesso!");
            MainMenuGui.open(player, economyService, jobsService, workService);
            return;
        }
        if (authService.verify(player, password)) {
            authService.setLoggedIn(player.getUniqueId(), true);
            economyService.ensureAccount(player);
            player.sendMessage("Login efetuado.");
            MainMenuGui.open(player, economyService, jobsService, workService);
            return;
        }
        player.sendMessage("Senha incorreta. Tente novamente.");
    }
}

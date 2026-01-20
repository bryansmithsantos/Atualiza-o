package com.example.economia.features.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.economia.features.auth.AuthService;
import com.example.economia.features.gui.AuthGui;
import com.example.economia.features.messages.Messages;

public final class AuthCommand implements CommandExecutor {

    private final AuthService authService;
    private final boolean register;

    public AuthCommand(AuthService authService, boolean register) {
        this.authService = authService;
        this.register = register;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        // Se não tem argumentos, abre a GUI
        if (args.length == 0) {
            if (register || !authService.isRegistered(player.getUniqueId())) {
                AuthGui.openRegister(player);
            } else {
                AuthGui.openLogin(player);
            }
            return true;
        }

        // Com argumentos = registro/login direto via comando
        String password = args[0];

        if (register) {
            // /register <senha>
            if (authService.isRegistered(player.getUniqueId())) {
                Messages.error(player, "Você já está registrado! Use /login");
                return true;
            }
            if (password.length() < authService.getMinPasswordLength()) {
                Messages.error(player,
                        "Senha muito curta! Mínimo: " + authService.getMinPasswordLength() + " caracteres.");
                return true;
            }
            if (authService.register(player, password)) {
                authService.setLoggedIn(player.getUniqueId(), true);
                Messages.success(player, "Conta criada e logado com sucesso!");
            } else {
                Messages.error(player, "Falha ao registrar. Tente novamente.");
            }
        } else {
            // /login <senha>
            if (!authService.isRegistered(player.getUniqueId())) {
                Messages.error(player, "Você não está registrado! Use /register <senha>");
                return true;
            }
            if (authService.verify(player, password)) {
                authService.setLoggedIn(player.getUniqueId(), true);
                Messages.success(player, "Login realizado com sucesso!");
            } else {
                Messages.error(player, "Senha incorreta!");
            }
        }
        return true;
    }
}

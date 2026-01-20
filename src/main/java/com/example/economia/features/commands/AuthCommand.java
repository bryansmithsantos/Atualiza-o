package com.example.economia.features.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.economia.features.auth.AuthService;
import com.example.economia.features.gui.AuthGui;

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
        if (register || !authService.isRegistered(player.getUniqueId())) {
            AuthGui.openRegister(player);
        } else {
            AuthGui.openLogin(player);
        }
        return true;
    }
}

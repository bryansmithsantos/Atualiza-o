package com.example.economia.features.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        int ping = player.getPing();
        String color = ping < 50 ? "§a" : (ping < 150 ? "§e" : "§c");

        player.sendMessage(Component.text("🏓 Pong! Seu ping: " + color + ping + "ms").color(NamedTextColor.GREEN));
        return true;
    }
}

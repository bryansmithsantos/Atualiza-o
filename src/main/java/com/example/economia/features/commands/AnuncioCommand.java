package com.example.economia.features.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class AnuncioCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("blinded.admin")) {
            sender.sendMessage("§cSem permissão.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUse: /anuncio <mensagem>");
            return true;
        }

        String msg = String.join(" ", args);

        Component announcement = Component.empty()
                .append(Component.text("\n"))
                .append(Component.text("📢 ANÚNCIO", NamedTextColor.YELLOW, TextDecoration.BOLD))
                .append(Component.text("\n\n"))
                .append(Component.text(msg, NamedTextColor.WHITE))
                .append(Component.text("\n"));

        Bukkit.broadcast(announcement);

        // Sound effect
        Bukkit.getOnlinePlayers()
                .forEach(p -> p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f));

        return true;
    }
}

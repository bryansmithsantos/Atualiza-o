package com.example.economia.features.homes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class HomeCommand implements CommandExecutor {

    private final HomeService homeService;

    public HomeCommand(HomeService homeService) {
        this.homeService = homeService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        if (label.equalsIgnoreCase("sethome")) {
            String name = args.length > 0 ? args[0] : "home";
            homeService.setHome(player, name);
            player.sendMessage(
                    Component.text("🏠 Home '" + name + "' definida com sucesso!").color(NamedTextColor.GREEN));
            return true;
        }

        if (label.equalsIgnoreCase("delhome")) {
            String name = args.length > 0 ? args[0] : "home";
            if (homeService.deleteHome(player, name)) {
                player.sendMessage(Component.text("🗑 Home '" + name + "' excluída.").color(NamedTextColor.RED));
            } else {
                player.sendMessage(Component.text("❌ Home '" + name + "' não encontrada.").color(NamedTextColor.RED));
            }
            return true;
        }

        if (label.equalsIgnoreCase("homes")) {
            List<Home> homes = homeService.getHomes(player);
            if (homes.isEmpty()) {
                player.sendMessage(Component.text("❌ Você não possui homes.").color(NamedTextColor.RED));
                return true;
            }

            player.sendMessage(Component.text("🏠 Seus Homes:", NamedTextColor.GOLD));
            for (Home home : homes) {
                player.sendMessage(Component.text(" - " + home.name() + " (" + home.getWorldName() + ")")
                        .color(NamedTextColor.YELLOW));
            }
            return true;
        }
        // /home command
        String name = args.length > 0 ? args[0] : "home";
        Home home = homeService.getHome(player, name);

        if (home == null) {
            player.sendMessage(Component.text("❌ Home '" + name + "' não encontrada.").color(NamedTextColor.RED));
            return true;
        }

        player.teleport(home.location());
        player.sendMessage(Component.text("⚡ Teleportado para '" + name + "'.").color(NamedTextColor.GREEN));

        return true;
    }
}

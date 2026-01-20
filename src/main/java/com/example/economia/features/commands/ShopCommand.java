package com.example.economia.features.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.economia.features.auth.AuthService;
import com.example.economia.features.gui.AuthGui;
import com.example.economia.features.gui.ShopGui;
import com.example.economia.features.shop.ShopService;

public final class ShopCommand implements CommandExecutor {

    private final AuthService authService;
    private final ShopService shopService;
    private final String currencySymbol;

    public ShopCommand(AuthService authService, ShopService shopService, String currencySymbol) {
        this.authService = authService;
        this.shopService = shopService;
        this.currencySymbol = currencySymbol;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }
        if (!authService.isLoggedIn(player.getUniqueId())) {
            if (authService.isRegistered(player.getUniqueId())) {
                AuthGui.openLogin(player);
            } else {
                AuthGui.openRegister(player);
            }
            return true;
        }
        ShopGui.open(player, shopService, currencySymbol);
        return true;
    }
}

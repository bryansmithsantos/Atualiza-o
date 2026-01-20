package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.vault.VaultService;

public final class VaultGui {

    private VaultGui() {
    }

    public static void open(Player player, EconomyService economyService, VaultService vaultService) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.VAULT);
        String wallet = economyService.formatBalance(player.getUniqueId());
        String vault = economyService.getCurrencySymbol() + String.format("%.2f", vaultService.getBalance(player.getUniqueId()));

        inv.setItem(11, GuiUtils.item(Material.CHEST, "Guardar 50", "Carteira: " + wallet, "Cofre: " + vault));
        inv.setItem(13, GuiUtils.item(Material.CHEST, "Guardar 200", "Carteira: " + wallet, "Cofre: " + vault));
        inv.setItem(15, GuiUtils.item(Material.CHEST_MINECART, "Retirar 50", "Cofre: " + vault));
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));

        player.openInventory(inv);
    }
}

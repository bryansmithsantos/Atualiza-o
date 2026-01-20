package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.bank.BankService;
import com.example.economia.features.economy.EconomyService;

public final class BankGui {

    private BankGui() {
    }

    public static void open(Player player, EconomyService economyService, BankService bankService) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.BANK);
        String wallet = economyService.formatBalance(player.getUniqueId());
        String bank = economyService.getCurrencySymbol() + String.format("%.2f", bankService.getBalance(player.getUniqueId()));
        String remaining = economyService.getCurrencySymbol() + String.format("%.2f", bankService.getRemainingLimit(player.getUniqueId()));

        inv.setItem(11, GuiUtils.item(Material.GOLD_INGOT, "Depositar 50", "Carteira: " + wallet, "Restante hoje: " + remaining));
        inv.setItem(13, GuiUtils.item(Material.GOLD_BLOCK, "Depositar 200", "Carteira: " + wallet, "Restante hoje: " + remaining));
        inv.setItem(15, GuiUtils.item(Material.GOLD_NUGGET, "Sacar 50", "Banco: " + bank));
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));

        player.openInventory(inv);
    }
}

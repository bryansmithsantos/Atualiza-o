package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.fines.Fine;
import com.example.economia.features.fines.FinesService;

public final class FinesGui {

    private FinesGui() {
    }

    public static void open(Player player, EconomyService economyService, FinesService finesService) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.FINES);
        int slot = 10;
        for (Fine fine : finesService.getFines(player.getUniqueId())) {
            inv.setItem(slot, GuiUtils.item(Material.PAPER, fine.reason(),
                    "Valor: " + economyService.getCurrencySymbol() + fine.amount(),
                    "Pagar 50",
                    "ID: " + fine.id()));
            slot += 2;
            if (slot >= inv.getSize()) {
                break;
            }
        }
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));
        player.openInventory(inv);
    }
}

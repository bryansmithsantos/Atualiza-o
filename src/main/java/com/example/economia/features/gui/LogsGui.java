package com.example.economia.features.gui;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.logs.LogService;

public final class LogsGui {

    private LogsGui() {
    }

    public static void open(Player player, LogService logService) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.LOGS);
        List<String> logs = logService.get(player.getUniqueId());
        int slot = 10;
        for (int i = Math.max(0, logs.size() - 5); i < logs.size(); i++) {
            inv.setItem(slot, GuiUtils.item(Material.PAPER, "Registro", logs.get(i)));
            slot += 2;
            if (slot >= inv.getSize()) {
                break;
            }
        }
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));
        player.openInventory(inv);
    }
}

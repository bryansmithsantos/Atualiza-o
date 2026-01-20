package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class CompanyInviteGui {

    private CompanyInviteGui() {
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.COMPANY_INVITE);
        inv.setItem(11, GuiUtils.item(Material.EMERALD, "Aceitar", "Entrar na empresa"));
        inv.setItem(15, GuiUtils.item(Material.BARRIER, "Recusar", "Ignorar convite"));
        player.openInventory(inv);
    }
}

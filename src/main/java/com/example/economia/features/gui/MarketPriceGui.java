package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class MarketPriceGui {

    private MarketPriceGui() {
    }

    public static void open(Player player, String currency) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.MARKET_PRICE);
        inv.setItem(11, GuiUtils.item(Material.GOLD_NUGGET, "Preço 50", "Listar por " + currency + "50"));
        inv.setItem(13, GuiUtils.item(Material.GOLD_INGOT, "Preço 200", "Listar por " + currency + "200"));
        inv.setItem(15, GuiUtils.item(Material.GOLD_BLOCK, "Preço 500", "Listar por " + currency + "500"));
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Mercado"));
        player.openInventory(inv);
    }
}

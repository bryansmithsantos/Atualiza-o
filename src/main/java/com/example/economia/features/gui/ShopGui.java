package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.shop.ShopItem;
import com.example.economia.features.shop.ShopService;

public final class ShopGui {

    private ShopGui() {
    }

    public static void open(Player player, ShopService shopService, String currencySymbol) {
        Inventory inv = Bukkit.createInventory(null, 54, GuiTitles.SHOP);
        int slot = 0;
        for (ShopItem item : shopService.getItems()) {
            if (slot >= 45) {
                break;
            }
            inv.setItem(slot++, GuiUtils.item(item.material(), item.displayName(),
                    "Comprar: " + currencySymbol + item.buyPrice(),
                    "Vender: " + currencySymbol + item.sellPrice(),
                    "Clique: comprar 1",
                    "Clique direito: vender 1"));
        }
        inv.setItem(49, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));
        player.openInventory(inv);
    }
}

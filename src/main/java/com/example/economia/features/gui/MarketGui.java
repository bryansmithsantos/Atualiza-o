package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.market.MarketListing;
import com.example.economia.features.market.MarketService;

public final class MarketGui {

    private MarketGui() {
    }

    public static void open(Player player, MarketService marketService, String currency) {
        Inventory inv = Bukkit.createInventory(null, 54, GuiTitles.MARKET);
        int slot = 0;
        for (MarketListing listing : marketService.getListings()) {
            if (slot >= 45) {
                break;
            }
            inv.setItem(slot++, GuiUtils.item(listing.material(), listing.material().name(),
                    "Quantidade: " + listing.amount(),
                    "Preço: " + currency + listing.price(),
                    "Clique para comprar",
                    "ID: " + listing.id()));
        }
        inv.setItem(49, GuiUtils.item(Material.PAPER, "Listar item", "Item na mão", "Clique e escolha preço"));
        inv.setItem(53, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));
        player.openInventory(inv);
    }
}

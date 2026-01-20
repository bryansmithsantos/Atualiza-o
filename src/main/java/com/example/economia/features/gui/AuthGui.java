package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class AuthGui {

    private AuthGui() {
    }

    public static void openRegister(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.AUTH);

        // Decoração
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, GuiUtils.item(Material.BLACK_STAINED_GLASS_PANE, " ", " "));
        }

        inv.setItem(11, GuiUtils.item(Material.EMERALD_BLOCK, "§a§lREGISTRAR",
                "§7Clique aqui pare criar sua conta.",
                "",
                "§e▶ Clique para iniciar"));

        inv.setItem(13, GuiUtils.item(Material.BOOK, "§6§lAJUDA",
                "§7Siga as instruções no chat",
                "§7para criar sua senha segura."));

        inv.setItem(15, GuiUtils.item(Material.PAPER, "§fLogin",
                "§7Já possui uma conta?",
                "§7Clique para entrar."));

        player.openInventory(inv);
    }

    public static void openLogin(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.AUTH);

        // Decoração
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, GuiUtils.item(Material.BLACK_STAINED_GLASS_PANE, " ", " "));
        }

        inv.setItem(11, GuiUtils.item(Material.PAPER, "§a§lLOGIN",
                "§7Clique aqui para entrar na sua conta.",
                "",
                "§e▶ Clique para iniciar"));

        inv.setItem(13, GuiUtils.item(Material.BOOK, "§6§lAJUDA",
                "§7Digite sua senha no chat",
                "§7após clicar no botão."));

        inv.setItem(15, GuiUtils.item(Material.EMERALD_ORE, "§fRegistrar",
                "§7Não tem conta?",
                "§7Clique para criar uma."));

        player.openInventory(inv);
    }
}

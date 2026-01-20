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
        inv.setItem(11, GuiUtils.item(Material.EMERALD, "Registrar", "Clique e digite sua senha no chat"));
        inv.setItem(15, GuiUtils.item(Material.PAPER, "Login", "Já tem conta?"));
        inv.setItem(22, GuiUtils.item(Material.BOOK, "Ajuda", "Digite 'cancelar' para cancelar"));
        player.openInventory(inv);
    }

    public static void openLogin(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.AUTH);
        inv.setItem(11, GuiUtils.item(Material.PAPER, "Login", "Clique e digite sua senha no chat"));
        inv.setItem(15, GuiUtils.item(Material.EMERALD, "Registrar", "Criar nova conta"));
        inv.setItem(22, GuiUtils.item(Material.BOOK, "Ajuda", "Digite 'cancelar' para cancelar"));
        player.openInventory(inv);
    }
}

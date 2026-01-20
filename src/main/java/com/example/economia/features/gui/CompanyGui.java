package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.company.Company;
import com.example.economia.features.company.CompanyService;
import com.example.economia.features.economy.EconomyService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class CompanyGui {

    private CompanyGui() {
    }

    public static void open(Player player, EconomyService economyService, CompanyService companyService) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(GuiTitles.COMPANY_TEXT, NamedTextColor.DARK_GREEN));
        Company company = companyService.getCompany(player.getUniqueId());
        if (company == null) {
            inv.setItem(13, GuiUtils.item(Material.NAME_TAG, "Criar empresa", "Clique para criar"));
        } else {
            String vault = economyService.getCurrencySymbol() + String.format("%.2f", company.vault());
            inv.setItem(11, GuiUtils.item(Material.CHEST, "Cofre da empresa", "Saldo: " + vault));
            inv.setItem(13, GuiUtils.item(Material.PLAYER_HEAD, "Membros", "Total: " + company.members().size()));
            inv.setItem(15, GuiUtils.item(Material.PAPER, "Convidar", "Convidar jogadores online"));
        }
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));
        player.openInventory(inv);
    }
}

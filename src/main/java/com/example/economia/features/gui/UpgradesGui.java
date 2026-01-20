package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.upgrades.UpgradeType;
import com.example.economia.features.upgrades.UpgradesService;

public final class UpgradesGui {

    private UpgradesGui() {
    }

    public static void open(Player player, EconomyService economyService, UpgradesService upgradesService) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.UPGRADES);
        String currency = economyService.getCurrencySymbol();

        int salaryLevel = upgradesService.getLevel(player.getUniqueId(), UpgradeType.SALARY);
        int cooldownLevel = upgradesService.getLevel(player.getUniqueId(), UpgradeType.COOLDOWN);
        int bonusLevel = upgradesService.getLevel(player.getUniqueId(), UpgradeType.JOB_BONUS);

        inv.setItem(11, GuiUtils.item(Material.EMERALD, "Salário", "Nível: " + salaryLevel,
                "Custo: " + currency + upgradesService.getCost(player.getUniqueId(), UpgradeType.SALARY)));
        inv.setItem(13, GuiUtils.item(Material.CLOCK, "Cooldown", "Nível: " + cooldownLevel,
                "Custo: " + currency + upgradesService.getCost(player.getUniqueId(), UpgradeType.COOLDOWN)));
        inv.setItem(15, GuiUtils.item(Material.ENCHANTED_BOOK, "Bônus por Job", "Nível: " + bonusLevel,
                "Custo: " + currency + upgradesService.getCost(player.getUniqueId(), UpgradeType.JOB_BONUS)));
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));

        player.openInventory(inv);
    }
}

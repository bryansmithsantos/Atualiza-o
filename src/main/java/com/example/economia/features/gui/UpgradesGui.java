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

                // Calculate bonus as whole number (e.g., level 1 = +100, level 2 = +200, etc.)
                int salaryBonus = salaryLevel * 100;
                int nextBonus = (salaryLevel + 1) * 100;

                inv.setItem(11, GuiUtils.item(Material.EMERALD, "§aSalário +$" + salaryBonus,
                                "§7Nível: §f" + salaryLevel,
                                "§7Próximo: §a+$" + nextBonus,
                                "§7Custo: §e" + currency + String.format("%.0f",
                                                upgradesService.getCost(player.getUniqueId(), UpgradeType.SALARY))));
                inv.setItem(13, GuiUtils.item(Material.CLOCK, "§bCooldown -" + (cooldownLevel * 5) + "s",
                                "§7Nível: §f" + cooldownLevel,
                                "§7Próximo: §b-" + ((cooldownLevel + 1) * 5) + "s",
                                "§7Custo: §e" + currency + String.format("%.0f",
                                                upgradesService.getCost(player.getUniqueId(), UpgradeType.COOLDOWN))));
                inv.setItem(15, GuiUtils.item(Material.ENCHANTED_BOOK, "§dBônus +" + (bonusLevel * 10) + "%",
                                "§7Nível: §f" + bonusLevel,
                                "§7Próximo: §d+" + ((bonusLevel + 1) * 10) + "%",
                                "§7Custo: §e" + currency + String.format("%.0f",
                                                upgradesService.getCost(player.getUniqueId(), UpgradeType.JOB_BONUS))));
                inv.setItem(22, GuiUtils.item(Material.BARRIER, "§cVoltar", "§7Retornar ao painel"));

                player.openInventory(inv);
        }
}

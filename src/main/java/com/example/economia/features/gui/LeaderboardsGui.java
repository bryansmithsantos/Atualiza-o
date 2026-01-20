package com.example.economia.features.gui;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.jobs.JobsService;
import com.example.economia.features.missions.MissionsService;

public final class LeaderboardsGui {

    private LeaderboardsGui() {
    }

    public static void open(Player player, EconomyService economyService, JobsService jobsService, MissionsService missionsService) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.LEADERBOARDS);
        List<Map.Entry<UUID, Double>> topBalance = economyService.getBalances().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toList());
        int slot = 10;
        for (Map.Entry<UUID, Double> entry : topBalance) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
            inv.setItem(slot, GuiUtils.item(Material.EMERALD, p.getName() == null ? "Jogador" : p.getName(),
                    "Saldo: " + economyService.getCurrencySymbol() + String.format("%.2f", entry.getValue())));
            slot += 2;
        }
        List<Map.Entry<UUID, Integer>> topXp = jobsService.getTotalXp().entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(3)
            .collect(Collectors.toList());
        int xpSlot = 16;
        for (Map.Entry<UUID, Integer> entry : topXp) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
            inv.setItem(xpSlot, GuiUtils.item(Material.BOOK, p.getName() == null ? "Jogador" : p.getName(),
                "XP total: " + entry.getValue()));
            xpSlot++;
        }
        List<Map.Entry<UUID, Integer>> topMissions = missionsService.getCompletedCounts().entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(3)
            .collect(Collectors.toList());
        int missionSlot = 24;
        for (Map.Entry<UUID, Integer> entry : topMissions) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
            inv.setItem(missionSlot, GuiUtils.item(Material.WRITABLE_BOOK, p.getName() == null ? "Jogador" : p.getName(),
                "Missões: " + entry.getValue()));
            missionSlot--;
            if (missionSlot < 23) {
            break;
            }
        }
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));
        player.openInventory(inv);
    }
}

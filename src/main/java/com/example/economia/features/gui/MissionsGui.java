package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.missions.Mission;
import com.example.economia.features.missions.MissionProgress;
import com.example.economia.features.missions.MissionsService;

public final class MissionsGui {

    private MissionsGui() {
    }

    public static void open(Player player, EconomyService economyService, MissionsService missionsService) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.MISSIONS);
        int slot = 10;
        String currency = economyService.getCurrencySymbol();
        for (Mission mission : missionsService.getMissions()) {
            MissionProgress progress = missionsService.getProgress(player.getUniqueId(), mission.id());
            String line1 = "Progresso: " + progress.progress() + "/" + mission.goal();
            String line2 = "Recompensa: " + currency + mission.reward();
            String line3 = progress.claimed() ? "Reivindicado" : "Clique para coletar";
            inv.setItem(slot, GuiUtils.item(Material.BOOK, mission.title(), line1, line2, line3));
            slot += 2;
        }
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));
        player.openInventory(inv);
    }
}

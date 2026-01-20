package com.example.economia.features.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.missions.Mission;
import com.example.economia.features.missions.MissionProgress;
import com.example.economia.features.missions.MissionsService;

public final class MissionsGui {

    private static final int ITEMS_PER_PAGE = 21; // 3 rows of 7
    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    private MissionsGui() {
    }

    public static void open(Player player, EconomyService economyService, MissionsService missionsService) {
        open(player, economyService, missionsService, 0);
    }

    public static void open(Player player, EconomyService economyService, MissionsService missionsService, int page) {
        List<Mission> allMissions = missionsService.getMissions();
        int totalPages = (int) Math.ceil(allMissions.size() / (double) ITEMS_PER_PAGE);

        if (page < 0)
            page = 0;
        if (page >= totalPages)
            page = totalPages - 1;

        playerPages.put(player.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, 54, GuiTitles.MISSIONS);
        String currency = economyService.getCurrencySymbol();

        // === DECORAÇÃO ===
        int[] border = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53 };
        for (int slot : border) {
            inv.setItem(slot, GuiUtils.item(Material.CYAN_STAINED_GLASS_PANE, " ", " "));
        }

        // === MISSÕES ===
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allMissions.size());

        int[] missionSlots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };
        int slotIndex = 0;

        for (int i = startIndex; i < endIndex && slotIndex < missionSlots.length; i++) {
            Mission mission = allMissions.get(i);
            MissionProgress progress = missionsService.getProgress(player.getUniqueId(), mission.id());

            boolean completed = progress.progress() >= mission.goal();
            boolean claimed = progress.claimed();

            Material icon = claimed ? Material.LIME_DYE : (completed ? Material.YELLOW_DYE : Material.GRAY_DYE);
            String statusColor = claimed ? "§7§m" : (completed ? "§a" : "§e");

            String line1 = "§7Progresso: §f" + progress.progress() + "/" + mission.goal();
            String line2 = "§7Recompensa: §a" + currency + String.format("%.0f", mission.reward());
            String line3 = claimed ? "§8✓ Reivindicado"
                    : (completed ? "§a► Clique para coletar!" : "§7Em andamento...");

            inv.setItem(missionSlots[slotIndex],
                    GuiUtils.item(icon, statusColor + mission.title(), line1, line2, line3));
            slotIndex++;
        }

        // === NAVEGAÇÃO ===
        if (page > 0) {
            inv.setItem(45, GuiUtils.item(Material.ARROW, "§b◀ Página Anterior", "§7Ir para página " + page));
        }

        inv.setItem(49, GuiUtils.item(Material.BARRIER, "§cVoltar", "§7Retornar ao painel"));

        if (page < totalPages - 1) {
            inv.setItem(53, GuiUtils.item(Material.ARROW, "§bPróxima Página ▶", "§7Ir para página " + (page + 2)));
        }

        // === INFO ===
        int completed = missionsService.getCompletedCount(player.getUniqueId());
        inv.setItem(4, GuiUtils.item(Material.NETHER_STAR, "§6★ Missões",
                "§7Página: §f" + (page + 1) + "/" + totalPages,
                "§7Completas: §a" + completed + "/" + allMissions.size()));

        player.openInventory(inv);
    }

    public static int getPlayerPage(UUID uuid) {
        return playerPages.getOrDefault(uuid, 0);
    }
}

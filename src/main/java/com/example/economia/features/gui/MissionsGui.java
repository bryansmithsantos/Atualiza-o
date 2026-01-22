package com.example.economia.features.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.missions.Mission;
import com.example.economia.features.missions.MissionProgress;
import com.example.economia.features.missions.MissionsService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class MissionsGui {

    private static final int ITEMS_PER_PAGE = 21;
    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    private MissionsGui() {
    }

    public static void open(Player player, EconomyService economyService, MissionsService missionsService) {
        open(player, economyService, missionsService, 0);
    }

    public static void open(Player player, EconomyService economyService, MissionsService missionsService, int page) {
        List<Mission> allMissions = missionsService.getMissions();
        int totalPages = Math.max(1, (int) Math.ceil(allMissions.size() / (double) ITEMS_PER_PAGE));

        if (page < 0)
            page = 0;
        if (page >= totalPages)
            page = totalPages - 1;

        playerPages.put(player.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, 54, GuiTitles.MISSIONS);
        String currency = economyService.getCurrencySymbol();

        // Border
        int[] border = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 50, 51, 52 };
        for (int slot : border) {
            inv.setItem(slot, GuiUtils.item(Material.CYAN_STAINED_GLASS_PANE, " ", " "));
        }

        // Missions
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

            // Create item with mission ID in lore
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();

            // Title with status color
            NamedTextColor titleColor = claimed ? NamedTextColor.DARK_GRAY
                    : (completed ? NamedTextColor.GREEN : NamedTextColor.YELLOW);
            Component title = Component.text(mission.title(), titleColor);
            if (claimed) {
                title = title.decorate(TextDecoration.STRIKETHROUGH);
            }
            meta.displayName(title.decoration(TextDecoration.ITALIC, false));

            // Lore with ID for claiming
            java.util.List<Component> lore = new java.util.ArrayList<>();
            lore.add(Component.text("Progresso: ", NamedTextColor.GRAY)
                    .append(Component.text(progress.progress() + "/" + mission.goal(), NamedTextColor.WHITE))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Recompensa: ", NamedTextColor.GRAY)
                    .append(Component.text(currency + String.format("%.0f", mission.reward()), NamedTextColor.GREEN))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());

            if (claimed) {
                lore.add(Component.text("✓ Já reivindicado", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC,
                        false));
            } else if (completed) {
                lore.add(Component.text("▶ Clique para coletar!", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("Em andamento...", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC,
                        false));
            }

            // Hidden mission ID at end
            lore.add(Component.text("ID: " + mission.id(), NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC,
                    false));

            meta.lore(lore);
            item.setItemMeta(meta);

            inv.setItem(missionSlots[slotIndex], item);
            slotIndex++;
        }

        // Navigation
        if (page > 0) {
            inv.setItem(45, GuiUtils.item(Material.ARROW, "§b◀ Página Anterior", "§7Página " + page));
        }

        inv.setItem(49, GuiUtils.item(Material.BARRIER, "§cVoltar", "§7Retornar ao painel"));

        if (page < totalPages - 1) {
            inv.setItem(53, GuiUtils.item(Material.ARROW, "§bPróxima Página ▶", "§7Página " + (page + 2)));
        }

        // Info
        int completedCount = missionsService.getCompletedCount(player.getUniqueId());
        inv.setItem(4, GuiUtils.item(Material.NETHER_STAR, "§6★ Missões",
                "§7Página: §f" + (page + 1) + "/" + totalPages,
                "§7Completas: §a" + completedCount + "/" + allMissions.size()));

        player.openInventory(inv);
    }

    public static int getPlayerPage(UUID uuid) {
        return playerPages.getOrDefault(uuid, 0);
    }
}

package com.example.economia.features.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.example.economia.features.clans.Clan;
import com.example.economia.features.clans.ClanService;
import com.example.economia.features.economy.EconomyService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ScoreboardService {

    private final Plugin plugin;
    private final EconomyService economyService;
    private final ClanService clanService;
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private int taskID = -1;

    public ScoreboardService(Plugin plugin, EconomyService economyService, ClanService clanService) {
        this.plugin = plugin;
        this.economyService = economyService;
        this.clanService = clanService;
    }

    public void start() {
        if (taskID != -1)
            return;

        taskID = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L).getTaskId();
    }

    public void stop() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
            taskID = -1;
        }
    }

    public void setupScoreboard(Player player) {
        // Reuse current scoreboard if it exists to preserve teams (Clans/Glow)
        Scoreboard board = player.getScoreboard();
        if (board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }

        playerScoreboards.put(player.getUniqueId(), board);

        Objective obj = board.getObjective("blinded");
        if (obj == null) {
            obj = board.registerNewObjective("blinded", Criteria.DUMMY,
                    LegacyComponentSerializer.legacySection().deserialize("§6§l⚡ BLINDED"));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        updateScoreboard(player);
    }

    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
    }

    private void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("blinded");

        if (obj == null) {
            setupScoreboard(player);
            return;
        }

        double balance = economyService.getBalance(player.getUniqueId());
        Clan clan = clanService.getClan(player.getUniqueId());
        String clanTag = (clan != null) ? "§7[" + clan.getTag() + "]" : "§7Nenhum";

        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        // Updated design: Clean, informative, uses components
        updateLine(board, "line1", 12, "§1", Component.text("§8§m----------------"));
        updateLine(board, "line2", 11, "§2",
                Component.text("👤 ", NamedTextColor.WHITE).append(Component.text("Perfil:", NamedTextColor.GOLD)));
        updateLine(board, "line3", 10, "§3", Component.text("  Nome: ", NamedTextColor.GRAY)
                .append(Component.text(player.getName(), NamedTextColor.WHITE)));
        updateLine(board, "line4", 9, "§4", Component.text("  Clan: ", NamedTextColor.GRAY)
                .append(LegacyComponentSerializer.legacySection().deserialize(clanTag)));
        updateLine(board, "line5", 8, "§5", Component.empty());
        updateLine(board, "line6", 7, "§6",
                Component.text("💰 ", NamedTextColor.WHITE).append(Component.text("Economia:", NamedTextColor.GOLD)));
        updateLine(board, "line7", 6, "§7", Component.text("  Saldo: ", NamedTextColor.GRAY)
                .append(Component.text(formatMoney(balance), NamedTextColor.GREEN)));
        updateLine(board, "line8", 5, "§8", Component.empty());
        updateLine(board, "line9", 4, "§9",
                Component.text("📍 ", NamedTextColor.WHITE).append(Component.text("Local:", NamedTextColor.GOLD)));
        updateLine(board, "line10", 3, "§a", Component.text("  " + x + " " + y + " " + z, NamedTextColor.WHITE));
        updateLine(board, "line11", 2, "§b", Component.empty());
        updateLine(board, "line12", 1, "§c", Component.text("      blinded.com", NamedTextColor.YELLOW));
    }

    private void updateLine(Scoreboard board, String teamName, int score, String entry, Component text) {
        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
            team.addEntry(entry);
        }

        // Use Adventure Component for prefix
        team.prefix(text);

        Objective obj = board.getObjective("blinded");
        if (obj != null) {
            obj.getScore(entry).setScore(score);
        }
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000)
            return String.format("$%.1fM", amount / 1_000_000);
        if (amount >= 1_000)
            return String.format("$%.1fK", amount / 1_000);
        return String.format("$%.0f", amount);
    }
}

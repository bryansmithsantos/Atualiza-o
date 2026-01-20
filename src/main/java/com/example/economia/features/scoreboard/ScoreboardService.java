package com.example.economia.features.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.example.economia.features.bedrock.BedrockSupport;
import com.example.economia.features.economy.EconomyService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class ScoreboardService {

    private static final String OBJECTIVE_ID = "economia_stats";

    private final Plugin plugin;
    private final BedrockSupport bedrockSupport;
    private final EconomyService economyService;
    private final PlayerJoinListener playerListener;
    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private int taskId = -1;

    public ScoreboardService(Plugin plugin, BedrockSupport bedrockSupport, EconomyService economyService) {
        this.plugin = plugin;
        this.bedrockSupport = bedrockSupport;
        this.economyService = economyService;
        this.playerListener = new PlayerJoinListener(this);
    }

    public PlayerJoinListener getPlayerListener() {
        return playerListener;
    }

    public void start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin,
                this::updateAll,
                20L,
                20L
        );
        Bukkit.getOnlinePlayers().forEach(this::applyScoreboard);
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        scoreboards.clear();
    }

    public void applyScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return;
        }

        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective(OBJECTIVE_ID, Criteria.DUMMY, Component.text("Economia", NamedTextColor.GOLD));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        String lineName = entry("Jogador:", NamedTextColor.WHITE);
        String lineHealth = entry("Vida:", NamedTextColor.RED);
        String lineFood = entry("Fome:", NamedTextColor.YELLOW);
        String lineMoney = entry("Saldo:", NamedTextColor.GREEN);
        String linePlatform = entry("Plataforma:", NamedTextColor.AQUA);

        objective.getScore(lineName).setScore(5);
        objective.getScore(lineHealth).setScore(4);
        objective.getScore(lineFood).setScore(3);
        objective.getScore(lineMoney).setScore(2);
        objective.getScore(linePlatform).setScore(1);

        setupTeam(board, "name", lineName, player.getName());
        setupTeam(board, "health", lineHealth, formatHealth(player));
        setupTeam(board, "food", lineFood, String.valueOf(player.getFoodLevel()));
        setupTeam(board, "money", lineMoney, formatMoney(player));
        setupTeam(board, "platform", linePlatform, formatPlatform(player));

        player.setScoreboard(board);
        scoreboards.put(player.getUniqueId(), board);
    }

    public void removeScoreboard(Player player) {
        scoreboards.remove(player.getUniqueId());
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard board = scoreboards.get(player.getUniqueId());
            if (board == null) {
                applyScoreboard(player);
                continue;
            }

            Team nameTeam = board.getTeam("name");
            Team healthTeam = board.getTeam("health");
            Team foodTeam = board.getTeam("food");
            Team moneyTeam = board.getTeam("money");
            Team platformTeam = board.getTeam("platform");

            if (nameTeam != null) {
                nameTeam.suffix(Component.text(" " + player.getName()));
            }
            if (healthTeam != null) {
                healthTeam.suffix(Component.text(" " + formatHealth(player)));
            }
            if (foodTeam != null) {
                foodTeam.suffix(Component.text(" " + player.getFoodLevel()));
            }
            if (moneyTeam != null) {
                moneyTeam.suffix(Component.text(" " + formatMoney(player)));
            }
            if (platformTeam != null) {
                platformTeam.suffix(Component.text(" " + formatPlatform(player)));
            }
        }
    }

    private void setupTeam(Scoreboard board, String id, String entry, String value) {
        Team team = board.registerNewTeam(id);
        team.addEntry(entry);
        team.suffix(Component.text(" " + value));
    }

    private String formatHealth(Player player) {
        double hearts = Math.max(0, player.getHealth()) / 2.0;
        return String.format("%.1f", hearts);
    }

    private String formatPlatform(Player player) {
        if (bedrockSupport == null || !bedrockSupport.isAvailable()) {
            return "Java";
        }
        return bedrockSupport.isBedrock(player) ? "Bedrock" : "Java";
    }

    private String formatMoney(Player player) {
        if (economyService == null) {
            return "0.00";
        }
        return economyService.formatBalance(player.getUniqueId());
    }

    private String entry(String label, NamedTextColor color) {
        return LegacyComponentSerializer.legacySection().serialize(Component.text(label, color));
    }
}

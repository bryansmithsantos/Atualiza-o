package com.example.economia.features.scoreboard;

import java.util.HashMap;
import java.util.List;
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
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class ScoreboardService {

    private static final String OBJECTIVE_ID = "economia_stats";

    // Cores personalizadas
    private static final TextColor GOLD = TextColor.color(255, 170, 0);
    private static final TextColor LIGHT_PURPLE = TextColor.color(255, 85, 255);
    private static final TextColor AQUA = TextColor.color(85, 255, 255);
    private static final TextColor LIME = TextColor.color(85, 255, 85);
    private static final TextColor RED = TextColor.color(255, 85, 85);
    private static final TextColor YELLOW = TextColor.color(255, 255, 85);
    private static final TextColor WHITE = TextColor.color(255, 255, 255);
    private static final TextColor GRAY = TextColor.color(170, 170, 170);
    private static final TextColor DARK_GRAY = TextColor.color(85, 85, 85);

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
                40L // Atualiza a cada 2 segundos
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

        // Título da scoreboard com gradiente
        Component title = Component.text("✦ ", LIGHT_PURPLE)
                .append(Component.text("BLINDED", GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text(" ✦", LIGHT_PURPLE));

        Objective objective = board.registerNewObjective(OBJECTIVE_ID, Criteria.DUMMY, title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Linhas da scoreboard (de baixo pra cima)
        String line1 = entry("", DARK_GRAY, "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        String line2 = entry("⌚ ", GRAY, "Online:");
        String line3 = entry("", DARK_GRAY, "");
        String line4 = entry("🎮 ", AQUA, "Plataforma:");
        String line5 = entry("🏆 ", YELLOW, "Ranking:");
        String line6 = entry("💰 ", LIME, "Saldo:");
        String line7 = entry("❤ ", RED, "Vida:");
        String line8 = entry("", DARK_GRAY, " ");
        String line9 = entry("👤 ", WHITE, "");
        String line10 = entry("", DARK_GRAY, "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        objective.getScore(line10).setScore(10);
        objective.getScore(line9).setScore(9);
        objective.getScore(line8).setScore(8);
        objective.getScore(line7).setScore(7);
        objective.getScore(line6).setScore(6);
        objective.getScore(line5).setScore(5);
        objective.getScore(line4).setScore(4);
        objective.getScore(line3).setScore(3);
        objective.getScore(line2).setScore(2);
        objective.getScore(line1).setScore(1);

        setupTeam(board, "divider_top", line10, "");
        setupTeam(board, "name", line9, player.getName());
        setupTeam(board, "spacer1", line8, "");
        setupTeam(board, "health", line7, formatHealth(player));
        setupTeam(board, "money", line6, formatMoney(player));
        setupTeam(board, "rank", line5, getRanking(player));
        setupTeam(board, "platform", line4, formatPlatform(player));
        setupTeam(board, "spacer2", line3, "");
        setupTeam(board, "online", line2, String.valueOf(Bukkit.getOnlinePlayers().size()));
        setupTeam(board, "divider_bottom", line1, "");

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
            Team moneyTeam = board.getTeam("money");
            Team rankTeam = board.getTeam("rank");
            Team platformTeam = board.getTeam("platform");
            Team onlineTeam = board.getTeam("online");

            if (nameTeam != null) {
                nameTeam.suffix(Component.text(player.getName(), GOLD).decorate(TextDecoration.BOLD));
            }
            if (healthTeam != null) {
                healthTeam.suffix(Component.text(formatHealth(player), WHITE));
            }
            if (moneyTeam != null) {
                moneyTeam.suffix(Component.text(formatMoney(player), WHITE));
            }
            if (rankTeam != null) {
                rankTeam.suffix(Component.text(getRanking(player), WHITE));
            }
            if (platformTeam != null) {
                platformTeam.suffix(Component.text(formatPlatform(player), WHITE));
            }
            if (onlineTeam != null) {
                onlineTeam.suffix(
                        Component.text(" " + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers(), WHITE));
            }
        }
    }

    private void setupTeam(Scoreboard board, String id, String entry, String value) {
        Team team = board.registerNewTeam(id);
        team.addEntry(entry);
        team.suffix(Component.text(value, WHITE));
    }

    private String formatHealth(Player player) {
        double health = Math.max(0, player.getHealth());
        double maxHealth = 20.0; // Default
        var attr = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        if (attr != null)
            maxHealth = attr.getValue();
        int hearts = (int) Math.ceil(health / 2);
        int maxHearts = (int) Math.ceil(maxHealth / 2);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hearts; i++) {
            sb.append("§c❤");
        }
        for (int i = hearts; i < maxHearts; i++) {
            sb.append("§8❤");
        }
        return sb.toString();
    }

    private String formatPlatform(Player player) {
        if (bedrockSupport == null || !bedrockSupport.isAvailable()) {
            return "§bJava";
        }
        return bedrockSupport.isBedrock(player) ? "§aBedrock" : "§bJava";
    }

    private String formatMoney(Player player) {
        if (economyService == null) {
            return "§a$0.00";
        }
        return "§a" + economyService.formatBalance(player.getUniqueId());
    }

    private String getRanking(Player player) {
        if (economyService == null) {
            return "§7#?";
        }

        List<Map.Entry<UUID, Double>> leaderboard = economyService.getLeaderboard(100);
        int position = 1;
        for (Map.Entry<UUID, Double> entry : leaderboard) {
            if (entry.getKey().equals(player.getUniqueId())) {
                String rankColor = switch (position) {
                    case 1 -> "§6§l"; // Ouro
                    case 2 -> "§7§l"; // Prata
                    case 3 -> "§c§l"; // Bronze
                    default -> "§f";
                };
                String medal = switch (position) {
                    case 1 -> " §6⭐";
                    case 2 -> " §7⭐";
                    case 3 -> " §c⭐";
                    default -> "";
                };
                return rankColor + "#" + position + medal;
            }
            position++;
        }
        return "§7#" + position;
    }

    private String entry(String prefix, TextColor color, String label) {
        return LegacyComponentSerializer.legacySection().serialize(
                Component.text(prefix, color).append(Component.text(label, color)));
    }
}

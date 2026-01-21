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

    // Colors (Hex)
    private static final TextColor PRIMARY = TextColor.fromHexString("#FF5555");
    private static final TextColor SECONDARY = TextColor.fromHexString("#FFAA00");
    private static final TextColor TEXT = TextColor.fromHexString("#E0E0E0");
    private static final TextColor VALUE = TextColor.fromHexString("#FFFFFF");
    private static final TextColor ACCENT = TextColor.fromHexString("#55FFFF");
    private static final TextColor DARK = TextColor.fromHexString("#555555");

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
                plugin, this::updateAll, 20L, 40L);
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
        if (manager == null)
            return;

        Scoreboard board = manager.getNewScoreboard();
        Component title = Component.text("⚡ BLINDED ⚡", PRIMARY).decorate(TextDecoration.BOLD);
        Objective objective = board.registerNewObjective(OBJECTIVE_ID, Criteria.DUMMY, title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Lines
        String line0 = entry("", DARK, "§m------------------");
        String line1 = entry("🌐 ", SECONDARY, "Info:");
        String line2 = entry("  ▪ Online: ", TEXT, "");
        String line3 = entry("  ▪ Ping: ", TEXT, "");
        String line4 = entry("", DARK, " ");
        String line5 = entry("👤 ", SECONDARY, "Perfil:");
        String line6 = entry("  ▪ Rank: ", TEXT, "");
        String line7 = entry("  ▪ Saldo: ", TEXT, "");
        String line8 = entry("  ▪ Plataforma: ", ACCENT, "");
        String line9 = entry("", DARK, "");
        String line10 = entry("", DARK, "§m------------------");

        setScore(objective, line10, 10);
        setScore(objective, line9, 9);
        setScore(objective, line5, 8);
        setScore(objective, line6, 7);
        setScore(objective, line7, 6);
        setScore(objective, line8, 5);
        setScore(objective, line4, 4);
        setScore(objective, line1, 3);
        setScore(objective, line2, 2);
        setScore(objective, line3, 1);
        setScore(objective, line0, 0);

        createTeam(board, "rank", line6, getRanking(player));
        createTeam(board, "money", line7, formatMoney(player));
        createTeam(board, "online", line2, " " + Bukkit.getOnlinePlayers().size());
        createTeam(board, "ping", line3, " " + player.getPing() + "ms");
        createTeam(board, "platform", line8, formatPlatform(player));

        player.setScoreboard(board);
        scoreboards.put(player.getUniqueId(), board);
    }

    private void setScore(Objective obj, String entry, int score) {
        obj.getScore(entry).setScore(score);
    }

    private void createTeam(Scoreboard board, String name, String entry, String suffix) {
        Team team = board.registerNewTeam(name);
        team.addEntry(entry);
        team.suffix(Component.text(suffix, VALUE));
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
            updateTeam(board, "rank", getRanking(player));
            updateTeam(board, "money", formatMoney(player));
            updateTeam(board, "online", " " + Bukkit.getOnlinePlayers().size());
            updateTeam(board, "ping", " " + player.getPing() + "ms");
            updateTeam(board, "platform", formatPlatform(player));
        }
    }

    private void updateTeam(Scoreboard board, String name, String suffix) {
        Team team = board.getTeam(name);
        if (team != null) {
            team.suffix(Component.text(suffix, VALUE));
        }
    }

    private String formatMoney(Player player) {
        if (economyService == null)
            return " $0.00";
        return " " + economyService.formatBalance(player.getUniqueId());
    }

    private String formatPlatform(Player player) {
        if (bedrockSupport == null || !bedrockSupport.isAvailable())
            return " Java";
        return bedrockSupport.isBedrock(player) ? " Bedrock" : " Java";
    }

    private String getRanking(Player player) {
        if (economyService == null)
            return " #?";
        List<Map.Entry<UUID, Double>> leaderboard = economyService.getLeaderboard(100);
        int position = 1;
        for (Map.Entry<UUID, Double> entry : leaderboard) {
            if (entry.getKey().equals(player.getUniqueId())) {
                return " #" + position;
            }
            position++;
        }
        return " >100";
    }

    private String entry(String prefix, TextColor color, String label) {
        return LegacyComponentSerializer.legacySection().serialize(
                Component.text(prefix, color).append(Component.text(label, color)));
    }
}

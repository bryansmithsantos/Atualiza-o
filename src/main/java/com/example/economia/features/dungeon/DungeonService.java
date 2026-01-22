package com.example.economia.features.dungeon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.example.economia.features.clans.Clan;
import com.example.economia.features.clans.ClanService;
import com.example.economia.features.economy.EconomyService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class DungeonService {

    private final Plugin plugin;
    private final EconomyService economyService;
    private ClanService clanService;
    private final Map<UUID, DungeonSession> activeSessions = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Location> deathLocations = new HashMap<>();

    // Natural dungeon spawn
    private DungeonSession naturalDungeon = null;
    private int spawnTaskId = -1;
    private int timeoutTaskId = -1;

    private static final long COOLDOWN_MS = 5 * 60 * 1000; // 5 minutes (as requested)

    public DungeonService(Plugin plugin, EconomyService economyService) {
        this.plugin = plugin;
        this.economyService = economyService;
    }

    public void setClanService(ClanService clanService) {
        this.clanService = clanService;
    }

    public void startNaturalSpawnCycle() {
        // Spawn dungeon every 5 minutes (updated from 15)
        spawnTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin,
                this::spawnNaturalDungeon,
                20 * 60, // First spawn after 1 minute
                20 * 60 * 5 // Then every 5 minutes
        );
    }

    public void stopNaturalSpawnCycle() {
        if (spawnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(spawnTaskId);
        }
        if (timeoutTaskId != -1) {
            Bukkit.getScheduler().cancelTask(timeoutTaskId);
        }
    }

    private void spawnNaturalDungeon() {
        if (naturalDungeon != null && naturalDungeon.isActive()) {
            return; // Already a natural dungeon active
        }

        // Random difficulty
        DungeonDifficulty[] diffs = DungeonDifficulty.values();
        DungeonDifficulty difficulty = diffs[(int) (Math.random() * diffs.length)];

        // Create waiting dungeon
        naturalDungeon = new DungeonSession(plugin, null, difficulty);
        naturalDungeon.generateArenaOnly();

        // Announce globally
        Component msg = Component.text("")
                .append(Component.text("⚔ ", NamedTextColor.GOLD))
                .append(Component.text("DUNGEON ABERTA! ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text(difficulty.getColor() + difficulty.getName(), NamedTextColor.WHITE))
                .appendNewline()
                .append(Component.text("  Use ", NamedTextColor.GRAY))
                .append(Component.text("/dungeon entrar", NamedTextColor.YELLOW))
                .append(Component.text(" para participar! (5 min)", NamedTextColor.GRAY));

        Bukkit.getServer().sendMessage(msg);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
        }

        // 5 minute timeout
        timeoutTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(
                plugin,
                () -> {
                    if (naturalDungeon != null && !naturalDungeon.isActive()) {
                        naturalDungeon.cleanup();
                        naturalDungeon = null;

                        Bukkit.getServer().sendMessage(
                                Component.text("⚔ Dungeon fechou - ninguém entrou!", NamedTextColor.RED));
                    }
                },
                20 * 60 * 5 // 5 minutes
        );
    }

    public boolean joinNaturalDungeon(Player player) {
        if (naturalDungeon == null) {
            player.sendMessage("§cNenhuma dungeon natural disponível. Aguarde o spawn!");
            return false;
        }

        // Check cooldown
        if (isOnCooldown(player.getUniqueId())) {
            long remaining = getCooldownRemaining(player.getUniqueId());
            player.sendMessage("§cAguarde " + formatTime(remaining) + " para entrar novamente.");
            return false;
        }

        // Check balance
        double cost = naturalDungeon.getDifficulty().getEntryCost();
        if (economyService.getBalance(player.getUniqueId()) < cost) {
            player.sendMessage("§cVocê precisa de §e$" + String.format("%.0f", cost) + " §cpara entrar.");
            return false;
        }

        economyService.removeBalance(player.getUniqueId(), cost);

        // Add player
        naturalDungeon.addPlayer(player);
        activeSessions.put(player.getUniqueId(), naturalDungeon);

        // If first player, start the dungeon
        if (!naturalDungeon.isActive()) {
            naturalDungeon.start();

            // Cancel timeout
            if (timeoutTaskId != -1) {
                Bukkit.getScheduler().cancelTask(timeoutTaskId);
                timeoutTaskId = -1;
            }
        } else {
            // Join existing
            player.teleport(naturalDungeon.getArenaCenter());
            player.sendMessage("§aVocê entrou na dungeon!");
        }

        // Announce
        Bukkit.getServer().sendMessage(
                Component.text("⚔ ", NamedTextColor.GOLD)
                        .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" entrou na dungeon!", NamedTextColor.GRAY)));

        return true;
    }

    public boolean startDungeon(Player player, DungeonDifficulty difficulty) {
        UUID uuid = player.getUniqueId();

        if (getSession(player) != null) {
            player.sendMessage("§cVocê já está em uma dungeon!");
            return false;
        }

        if (isOnCooldown(uuid)) {
            long remaining = getCooldownRemaining(uuid);
            player.sendMessage("§cAguarde " + formatTime(remaining) + " para entrar novamente.");
            return false;
        }

        double cost = difficulty.getEntryCost();
        if (economyService.getBalance(uuid) < cost) {
            player.sendMessage("§cVocê precisa de §e$" + String.format("%.0f", cost) + " §cpara entrar.");
            return false;
        }

        economyService.removeBalance(uuid, cost);
        player.sendMessage("§aPagou §e$" + String.format("%.0f", cost) + " §apara entrar na dungeon.");

        DungeonSession session = new DungeonSession(plugin, player, difficulty);
        activeSessions.put(uuid, session);
        session.start();

        return true;
    }

    public boolean joinDungeon(Player joiner, Player owner) {
        DungeonSession session = activeSessions.get(owner.getUniqueId());

        if (session == null) {
            joiner.sendMessage("§c" + owner.getName() + " não está em uma dungeon.");
            return false;
        }

        if (!session.isActive()) {
            joiner.sendMessage("§cA dungeon já terminou.");
            return false;
        }

        if (getSession(joiner) != null) {
            joiner.sendMessage("§cVocê já está em uma dungeon!");
            return false;
        }

        if (isOnCooldown(joiner.getUniqueId())) {
            long remaining = getCooldownRemaining(joiner.getUniqueId());
            joiner.sendMessage("§cAguarde " + formatTime(remaining) + " para entrar novamente.");
            return false;
        }

        double cost = session.getDifficulty().getEntryCost() / 2;
        if (economyService.getBalance(joiner.getUniqueId()) < cost) {
            joiner.sendMessage("§cVocê precisa de §e$" + String.format("%.0f", cost) + " §cpara ajudar.");
            return false;
        }

        economyService.removeBalance(joiner.getUniqueId(), cost);

        session.addPlayer(joiner);
        activeSessions.put(joiner.getUniqueId(), session);

        joiner.teleport(session.getArenaCenter());
        joiner.sendMessage("§aVocê entrou na dungeon de " + owner.getName() + "!");

        for (UUID uuid : session.getPlayers()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && !p.equals(joiner)) {
                p.sendMessage("§a" + joiner.getName() + " §7entrou na dungeon para ajudar!");
            }
        }

        return true;
    }

    public void handlePlayerDeath(Player player) {
        DungeonSession session = getSession(player);
        if (session != null && session.isActive()) {
            // Save death location for respawn in dungeon
            deathLocations.put(player.getUniqueId(), session.getArenaCenter().clone());
        }
    }

    public void handlePlayerRespawn(Player player) {
        Location respawnLoc = deathLocations.remove(player.getUniqueId());
        if (respawnLoc != null) {
            DungeonSession session = getSession(player);
            if (session != null && session.isActive()) {
                // Respawn in dungeon
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.teleport(respawnLoc);
                    player.sendMessage("§eVocê renasceu na dungeon! Continue lutando!");
                }, 1L);
            }
        }
    }

    public DungeonSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public void endSession(DungeonSession session) {
        for (UUID uuid : session.getPlayers()) {
            activeSessions.remove(uuid);
            cooldowns.put(uuid, System.currentTimeMillis());
        }

        if (session == naturalDungeon) {
            naturalDungeon = null;
        }
    }

    public void giveRewards(DungeonSession session, double totalReward) {
        int playerCount = session.getPlayers().size();
        double share = totalReward / playerCount;

        StringBuilder playerNames = new StringBuilder();
        String clanTag = null;

        for (UUID uuid : session.getPlayers()) {
            economyService.addBalance(uuid, share);
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) {
                p.sendMessage("§a§l+$" + String.format("%.0f", share) + " §ade recompensa!");

                if (playerNames.length() > 0)
                    playerNames.append(", ");
                playerNames.append(p.getName());

                // Get clan tag
                if (clanService != null && clanTag == null) {
                    Clan clan = clanService.getClan(uuid);
                    if (clan != null) {
                        clanTag = clan.getTag();
                    }
                }
            }
            cooldowns.put(uuid, System.currentTimeMillis());
        }

        // Global announcement
        Component announcement = Component.text("")
                .append(Component.text("🏆 ", NamedTextColor.GOLD))
                .append(Component.text("DUNGEON COMPLETA! ", NamedTextColor.GREEN).decorate(TextDecoration.BOLD));

        if (clanTag != null) {
            announcement = announcement.append(Component.text("[" + clanTag + "] ", NamedTextColor.GRAY));
        }

        announcement = announcement
                .append(Component.text(playerNames.toString(), NamedTextColor.YELLOW))
                .append(Component.text(" completou ", NamedTextColor.GRAY))
                .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                        .deserialize(session.getDifficulty().getColor() + session.getDifficulty().getName()));

        Bukkit.getServer().sendMessage(announcement);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1f);
        }

        endSession(session);
    }

    public DungeonSession getNaturalDungeon() {
        return naturalDungeon;
    }

    private boolean isOnCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid))
            return false;
        return System.currentTimeMillis() - cooldowns.get(uuid) < COOLDOWN_MS;
    }

    private long getCooldownRemaining(UUID uuid) {
        if (!cooldowns.containsKey(uuid))
            return 0;
        return COOLDOWN_MS - (System.currentTimeMillis() - cooldowns.get(uuid));
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public Map<UUID, DungeonSession> getActiveSessions() {
        return activeSessions;
    }
}

package com.example.economia.features.economy;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class EconomyService {

    private final Plugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final DecimalFormat formatter = new DecimalFormat("#,##0.00");
    private File dataFile;
    private FileConfiguration dataConfig;

    public EconomyService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            plugin.saveResource("config.yml", false);
            save();
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.isConfigurationSection("balances")) {
            for (String key : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                double value = dataConfig.getDouble("balances." + key, 0.0);
                balances.put(UUID.fromString(key), value);
            }
        }
    }

    public void save() {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            dataConfig.set("balances." + entry.getKey(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar data.yml: " + ex.getMessage());
        }
    }

    public void ensureAccount(Player player) {
        balances.putIfAbsent(player.getUniqueId(), getStartingBalance());
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public String formatBalance(UUID uuid) {
        return getCurrencySymbol() + formatter.format(getBalance(uuid));
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
    }

    public void addBalance(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + Math.max(0, amount));
    }

    public boolean removeBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        if (amount <= 0 || current < amount) {
            return false;
        }
        setBalance(uuid, current - amount);
        return true;
    }

    public double getStartingBalance() {
        return plugin.getConfig().getDouble("economy.starting-balance", 0.0);
    }

    public String getCurrencySymbol() {
        return plugin.getConfig().getString("economy.currency-symbol", "$");
    }

    public Map<UUID, Double> getBalances() {
        return Map.copyOf(balances);
    }

    public java.util.List<Map.Entry<UUID, Double>> getLeaderboard(int limit) {
        return balances.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .toList();
    }
}

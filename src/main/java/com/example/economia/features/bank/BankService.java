package com.example.economia.features.bank;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class BankService {

    private final Plugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final Map<UUID, Double> dailyUsed = new HashMap<>();
    private final Map<UUID, LocalDate> dailyDate = new HashMap<>();
    private File bankFile;
    private FileConfiguration bankConfig;

    public BankService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        bankFile = new File(plugin.getDataFolder(), "bank.yml");
        if (!bankFile.exists()) {
            bankFile.getParentFile().mkdirs();
            bankConfig = new YamlConfiguration();
            save();
        }
        bankConfig = YamlConfiguration.loadConfiguration(bankFile);
        if (bankConfig.isConfigurationSection("bank")) {
            for (String key : bankConfig.getConfigurationSection("bank").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                balances.put(uuid, bankConfig.getDouble("bank." + key + ".balance", 0));
                dailyUsed.put(uuid, bankConfig.getDouble("bank." + key + ".dailyUsed", 0));
                String date = bankConfig.getString("bank." + key + ".date", LocalDate.now().toString());
                dailyDate.put(uuid, LocalDate.parse(date));
            }
        }
    }

    public void save() {
        if (bankConfig == null) {
            bankConfig = new YamlConfiguration();
        }
        for (UUID uuid : balances.keySet()) {
            bankConfig.set("bank." + uuid + ".balance", balances.getOrDefault(uuid, 0.0));
            bankConfig.set("bank." + uuid + ".dailyUsed", dailyUsed.getOrDefault(uuid, 0.0));
            LocalDate date = dailyDate.getOrDefault(uuid, LocalDate.now());
            bankConfig.set("bank." + uuid + ".date", date.toString());
        }
        try {
            bankConfig.save(bankFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar bank.yml: " + ex.getMessage());
        }
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public double getDailyLimit() {
        return plugin.getConfig().getDouble("bank.daily-limit", 1000.0);
    }

    public double getRemainingLimit(UUID uuid) {
        resetDailyIfNeeded(uuid);
        return Math.max(0, getDailyLimit() - dailyUsed.getOrDefault(uuid, 0.0));
    }

    public boolean deposit(UUID uuid, double amount) {
        if (amount <= 0) {
            return false;
        }
        resetDailyIfNeeded(uuid);
        double remaining = getRemainingLimit(uuid);
        if (amount > remaining) {
            return false;
        }
        balances.put(uuid, getBalance(uuid) + amount);
        dailyUsed.put(uuid, dailyUsed.getOrDefault(uuid, 0.0) + amount);
        return true;
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) {
            return false;
        }
        double current = getBalance(uuid);
        if (current < amount) {
            return false;
        }
        balances.put(uuid, current - amount);
        return true;
    }

    private void resetDailyIfNeeded(UUID uuid) {
        LocalDate today = LocalDate.now();
        LocalDate last = dailyDate.getOrDefault(uuid, today);
        if (!last.equals(today)) {
            dailyDate.put(uuid, today);
            dailyUsed.put(uuid, 0.0);
        }
    }
}

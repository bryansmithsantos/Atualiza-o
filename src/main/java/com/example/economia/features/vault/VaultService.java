package com.example.economia.features.vault;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class VaultService {

    private final Plugin plugin;
    private final Map<UUID, Double> balances = new HashMap<>();
    private File vaultFile;
    private FileConfiguration vaultConfig;

    public VaultService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        vaultFile = new File(plugin.getDataFolder(), "vault.yml");
        if (!vaultFile.exists()) {
            vaultFile.getParentFile().mkdirs();
            vaultConfig = new YamlConfiguration();
            save();
        }
        vaultConfig = YamlConfiguration.loadConfiguration(vaultFile);
        if (vaultConfig.isConfigurationSection("vault")) {
            for (String key : vaultConfig.getConfigurationSection("vault").getKeys(false)) {
                balances.put(UUID.fromString(key), vaultConfig.getDouble("vault." + key, 0.0));
            }
        }
    }

    public void save() {
        if (vaultConfig == null) {
            vaultConfig = new YamlConfiguration();
        }
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            vaultConfig.set("vault." + entry.getKey(), entry.getValue());
        }
        try {
            vaultConfig.save(vaultFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar vault.yml: " + ex.getMessage());
        }
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public void deposit(UUID uuid, double amount) {
        if (amount <= 0) {
            return;
        }
        balances.put(uuid, getBalance(uuid) + amount);
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
}

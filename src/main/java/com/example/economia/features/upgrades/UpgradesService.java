package com.example.economia.features.upgrades;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class UpgradesService {

    private final Plugin plugin;
    private final Map<UUID, EnumMap<UpgradeType, Integer>> levels = new HashMap<>();
    private File upgradesFile;
    private FileConfiguration upgradesConfig;

    public UpgradesService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        upgradesFile = new File(plugin.getDataFolder(), "upgrades.yml");
        if (!upgradesFile.exists()) {
            upgradesFile.getParentFile().mkdirs();
            upgradesConfig = new YamlConfiguration();
            save();
        }
        upgradesConfig = YamlConfiguration.loadConfiguration(upgradesFile);
        if (upgradesConfig.isConfigurationSection("upgrades")) {
            for (String key : upgradesConfig.getConfigurationSection("upgrades").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                EnumMap<UpgradeType, Integer> map = new EnumMap<>(UpgradeType.class);
                for (UpgradeType type : UpgradeType.values()) {
                    map.put(type, upgradesConfig.getInt("upgrades." + key + "." + type.name(), 0));
                }
                levels.put(uuid, map);
            }
        }
    }

    public void save() {
        if (upgradesConfig == null) {
            upgradesConfig = new YamlConfiguration();
        }
        for (UUID uuid : levels.keySet()) {
            for (UpgradeType type : UpgradeType.values()) {
                upgradesConfig.set("upgrades." + uuid + "." + type.name(), getLevel(uuid, type));
            }
        }
        try {
            upgradesConfig.save(upgradesFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar upgrades.yml: " + ex.getMessage());
        }
    }

    public int getLevel(UUID uuid, UpgradeType type) {
        return levels.getOrDefault(uuid, new EnumMap<>(UpgradeType.class)).getOrDefault(type, 0);
    }

    public double getMultiplier(UUID uuid, UpgradeType type) {
        int level = getLevel(uuid, type);
        double step = plugin.getConfig().getDouble("upgrades." + type.name().toLowerCase() + ".step", 0.05);
        // Fix floating-point precision
        return Math.round((1.0 + (level * step)) * 1000.0) / 1000.0;
    }

    public double getCost(UUID uuid, UpgradeType type) {
        double base = plugin.getConfig().getDouble("upgrades." + type.name().toLowerCase() + ".base", 100.0);
        int level = getLevel(uuid, type);
        return base * (level + 1);
    }

    public void increase(UUID uuid, UpgradeType type) {
        EnumMap<UpgradeType, Integer> map = levels.computeIfAbsent(uuid, key -> new EnumMap<>(UpgradeType.class));
        map.put(type, getLevel(uuid, type) + 1);
    }
}

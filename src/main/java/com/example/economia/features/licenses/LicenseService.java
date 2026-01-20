package com.example.economia.features.licenses;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class LicenseService {

    private final Plugin plugin;
    private final Map<UUID, Set<String>> licenses = new HashMap<>();
    private File licenseFile;
    private FileConfiguration licenseConfig;

    public LicenseService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        licenseFile = new File(plugin.getDataFolder(), "licenses.yml");
        if (!licenseFile.exists()) {
            licenseFile.getParentFile().mkdirs();
            licenseConfig = new YamlConfiguration();
            save();
        }
        licenseConfig = YamlConfiguration.loadConfiguration(licenseFile);
        if (licenseConfig.isConfigurationSection("licenses")) {
            for (String key : licenseConfig.getConfigurationSection("licenses").getKeys(false)) {
                Set<String> set = new HashSet<>(licenseConfig.getStringList("licenses." + key));
                licenses.put(UUID.fromString(key), set);
            }
        }
    }

    public void save() {
        if (licenseConfig == null) {
            licenseConfig = new YamlConfiguration();
        }
        for (Map.Entry<UUID, Set<String>> entry : licenses.entrySet()) {
            licenseConfig.set("licenses." + entry.getKey(), new java.util.ArrayList<>(entry.getValue()));
        }
        try {
            licenseConfig.save(licenseFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar licenses.yml: " + ex.getMessage());
        }
    }

    public boolean has(UUID uuid, String licenseId) {
        if (licenseId == null) {
            return true;
        }
        return licenses.getOrDefault(uuid, Set.of()).contains(licenseId);
    }

    public void grant(UUID uuid, String licenseId) {
        if (licenseId == null) {
            return;
        }
        licenses.computeIfAbsent(uuid, key -> new HashSet<>()).add(licenseId);
    }
}

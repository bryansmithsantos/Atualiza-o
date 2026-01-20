package com.example.economia.features.logs;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class LogService {

    private final Plugin plugin;
    private final Map<UUID, List<String>> logs = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private File logsFile;
    private FileConfiguration logsConfig;

    public LogService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        logsFile = new File(plugin.getDataFolder(), "logs.yml");
        if (!logsFile.exists()) {
            logsFile.getParentFile().mkdirs();
            logsConfig = new YamlConfiguration();
            save();
        }
        logsConfig = YamlConfiguration.loadConfiguration(logsFile);
        if (logsConfig.isConfigurationSection("logs")) {
            for (String key : logsConfig.getConfigurationSection("logs").getKeys(false)) {
                List<String> list = logsConfig.getStringList("logs." + key);
                logs.put(UUID.fromString(key), new ArrayList<>(list));
            }
        }
    }

    public void save() {
        if (logsConfig == null) {
            logsConfig = new YamlConfiguration();
        }
        for (Map.Entry<UUID, List<String>> entry : logs.entrySet()) {
            logsConfig.set("logs." + entry.getKey(), entry.getValue());
        }
        try {
            logsConfig.save(logsFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar logs.yml: " + ex.getMessage());
        }
    }

    public void add(UUID uuid, String message) {
        int max = plugin.getConfig().getInt("logs.max", 30);
        List<String> list = logs.computeIfAbsent(uuid, key -> new ArrayList<>());
        list.add("[" + LocalDateTime.now().format(formatter) + "] " + message);
        while (list.size() > max) {
            list.remove(0);
        }
    }

    public List<String> get(UUID uuid) {
        return List.copyOf(logs.getOrDefault(uuid, List.of()));
    }
}

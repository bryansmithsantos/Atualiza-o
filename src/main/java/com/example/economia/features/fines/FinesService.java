package com.example.economia.features.fines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class FinesService {

    private final Plugin plugin;
    private final Map<UUID, List<Fine>> fines = new HashMap<>();
    private File finesFile;
    private FileConfiguration finesConfig;

    public FinesService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        finesFile = new File(plugin.getDataFolder(), "fines.yml");
        if (!finesFile.exists()) {
            finesFile.getParentFile().mkdirs();
            finesConfig = new YamlConfiguration();
            save();
        }
        finesConfig = YamlConfiguration.loadConfiguration(finesFile);
        if (finesConfig.isConfigurationSection("fines")) {
            for (String key : finesConfig.getConfigurationSection("fines").getKeys(false)) {
                List<Fine> list = new ArrayList<>();
                if (finesConfig.isConfigurationSection("fines." + key)) {
                    for (String fineId : finesConfig.getConfigurationSection("fines." + key).getKeys(false)) {
                        String reason = finesConfig.getString("fines." + key + "." + fineId + ".reason", "-");
                        double amount = finesConfig.getDouble("fines." + key + "." + fineId + ".amount", 0.0);
                        list.add(new Fine(fineId, reason, amount));
                    }
                }
                fines.put(UUID.fromString(key), list);
            }
        }
    }

    public void save() {
        if (finesConfig == null) {
            finesConfig = new YamlConfiguration();
        }
        for (Map.Entry<UUID, List<Fine>> entry : fines.entrySet()) {
            String path = "fines." + entry.getKey();
            finesConfig.set(path, null);
            for (Fine fine : entry.getValue()) {
                finesConfig.set(path + "." + fine.id() + ".reason", fine.reason());
                finesConfig.set(path + "." + fine.id() + ".amount", fine.amount());
            }
        }
        try {
            finesConfig.save(finesFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar fines.yml: " + ex.getMessage());
        }
    }

    public List<Fine> getFines(UUID uuid) {
        return List.copyOf(fines.getOrDefault(uuid, List.of()));
    }

    public void addFine(UUID uuid, String reason, double amount) {
        String id = "fine_" + System.currentTimeMillis();
        fines.computeIfAbsent(uuid, key -> new ArrayList<>()).add(new Fine(id, reason, amount));
    }

    public boolean payFine(UUID uuid, String fineId, double amount) {
        List<Fine> list = fines.get(uuid);
        if (list == null) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            Fine fine = list.get(i);
            if (fine.id().equals(fineId)) {
                double remaining = fine.amount() - amount;
                if (remaining <= 0) {
                    list.remove(i);
                } else {
                    list.set(i, new Fine(fine.id(), fine.reason(), remaining));
                }
                return true;
            }
        }
        return false;
    }
}

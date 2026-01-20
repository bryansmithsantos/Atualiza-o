package com.example.economia.features.missions;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class MissionsService {

    private final Plugin plugin;
    private final List<Mission> missions = new ArrayList<>();
    private final Map<UUID, Map<String, MissionProgress>> progress = new HashMap<>();
    private final Map<UUID, LocalDate> dateMap = new HashMap<>();
    private File missionsFile;
    private FileConfiguration missionsConfig;

    public MissionsService(Plugin plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    public void load() {
        missionsFile = new File(plugin.getDataFolder(), "missions.yml");
        if (!missionsFile.exists()) {
            missionsFile.getParentFile().mkdirs();
            missionsConfig = new YamlConfiguration();
            save();
        }
        missionsConfig = YamlConfiguration.loadConfiguration(missionsFile);
        if (missionsConfig.isConfigurationSection("missions")) {
            for (String key : missionsConfig.getConfigurationSection("missions").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                String dateString = missionsConfig.getString("missions." + key + ".date", LocalDate.now().toString());
                dateMap.put(uuid, LocalDate.parse(dateString));
                Map<String, MissionProgress> map = new HashMap<>();
                if (missionsConfig.isConfigurationSection("missions." + key + ".progress")) {
                    for (String missionId : missionsConfig.getConfigurationSection("missions." + key + ".progress").getKeys(false)) {
                        int value = missionsConfig.getInt("missions." + key + ".progress." + missionId + ".value", 0);
                        boolean claimed = missionsConfig.getBoolean("missions." + key + ".progress." + missionId + ".claimed", false);
                        map.put(missionId, new MissionProgress(value, claimed));
                    }
                }
                progress.put(uuid, map);
            }
        }
    }

    public void save() {
        if (missionsConfig == null) {
            missionsConfig = new YamlConfiguration();
        }
        for (UUID uuid : progress.keySet()) {
            missionsConfig.set("missions." + uuid + ".date", dateMap.getOrDefault(uuid, LocalDate.now()).toString());
            for (Map.Entry<String, MissionProgress> entry : progress.get(uuid).entrySet()) {
                missionsConfig.set("missions." + uuid + ".progress." + entry.getKey() + ".value", entry.getValue().progress());
                missionsConfig.set("missions." + uuid + ".progress." + entry.getKey() + ".claimed", entry.getValue().claimed());
            }
        }
        try {
            missionsConfig.save(missionsFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar missions.yml: " + ex.getMessage());
        }
    }

    public List<Mission> getMissions() {
        return List.copyOf(missions);
    }

    public MissionProgress getProgress(UUID uuid, String missionId) {
        resetIfNeeded(uuid);
        return progress.getOrDefault(uuid, Map.of()).getOrDefault(missionId, new MissionProgress(0, false));
    }

    public void recordWork(UUID uuid) {
        increment(uuid, MissionType.WORK_COUNT, 1);
    }

    public void recordEarn(UUID uuid, double amount) {
        increment(uuid, MissionType.EARN_AMOUNT, (int) Math.round(amount));
    }

    public void recordSell(UUID uuid, int amount) {
        increment(uuid, MissionType.SELL_COUNT, amount);
    }

    public boolean claim(UUID uuid, Mission mission) {
        MissionProgress mp = getProgress(uuid, mission.id());
        if (mp.claimed() || mp.progress() < mission.goal()) {
            return false;
        }
        setProgress(uuid, mission.id(), new MissionProgress(mp.progress(), true));
        return true;
    }

    public int getCompletedCount(UUID uuid) {
        int count = 0;
        for (Mission mission : missions) {
            MissionProgress mp = getProgress(uuid, mission.id());
            if (mp.progress() >= mission.goal()) {
                count++;
            }
        }
        return count;
    }

    public Map<UUID, Integer> getCompletedCounts() {
        Map<UUID, Integer> result = new HashMap<>();
        for (UUID uuid : progress.keySet()) {
            result.put(uuid, getCompletedCount(uuid));
        }
        return result;
    }

    private void increment(UUID uuid, MissionType type, int amount) {
        resetIfNeeded(uuid);
        for (Mission mission : missions) {
            if (mission.type() == type) {
                MissionProgress mp = getProgress(uuid, mission.id());
                setProgress(uuid, mission.id(), new MissionProgress(mp.progress() + amount, mp.claimed()));
            }
        }
    }

    private void setProgress(UUID uuid, String missionId, MissionProgress value) {
        progress.computeIfAbsent(uuid, key -> new HashMap<>()).put(missionId, value);
    }

    private void resetIfNeeded(UUID uuid) {
        LocalDate today = LocalDate.now();
        LocalDate date = dateMap.getOrDefault(uuid, today);
        if (!date.equals(today)) {
            dateMap.put(uuid, today);
            progress.put(uuid, new HashMap<>());
        }
    }

    private void registerDefaults() {
        missions.add(new Mission("work_5", "Trabalhe 5x", MissionType.WORK_COUNT, 5, 150));
        missions.add(new Mission("earn_500", "Ganhe 500", MissionType.EARN_AMOUNT, 500, 200));
        missions.add(new Mission("sell_10", "Venda 10 itens", MissionType.SELL_COUNT, 10, 120));
    }
}

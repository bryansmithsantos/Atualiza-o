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
                    for (String missionId : missionsConfig.getConfigurationSection("missions." + key + ".progress")
                            .getKeys(false)) {
                        int value = missionsConfig.getInt("missions." + key + ".progress." + missionId + ".value", 0);
                        boolean claimed = missionsConfig
                                .getBoolean("missions." + key + ".progress." + missionId + ".claimed", false);
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
                missionsConfig.set("missions." + uuid + ".progress." + entry.getKey() + ".value",
                        entry.getValue().progress());
                missionsConfig.set("missions." + uuid + ".progress." + entry.getKey() + ".claimed",
                        entry.getValue().claimed());
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

    public void increment(UUID uuid, MissionType type, int amount) {
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
        // === TRABALHO ===
        missions.add(new Mission("work_5", "Trabalhe 5x", MissionType.WORK_COUNT, 5, 150));
        missions.add(new Mission("work_10", "Trabalhe 10x", MissionType.WORK_COUNT, 10, 300));
        missions.add(new Mission("work_25", "Trabalhe 25x", MissionType.WORK_COUNT, 25, 750));
        missions.add(new Mission("work_50", "Trabalhe 50x", MissionType.WORK_COUNT, 50, 1500));

        // === DINHEIRO ===
        missions.add(new Mission("earn_500", "Ganhe $500", MissionType.EARN_AMOUNT, 500, 200));
        missions.add(new Mission("earn_1000", "Ganhe $1.000", MissionType.EARN_AMOUNT, 1000, 400));
        missions.add(new Mission("earn_5000", "Ganhe $5.000", MissionType.EARN_AMOUNT, 5000, 1000));
        missions.add(new Mission("earn_10000", "Ganhe $10.000", MissionType.EARN_AMOUNT, 10000, 2000));
        missions.add(new Mission("earn_50000", "Ganhe $50.000", MissionType.EARN_AMOUNT, 50000, 10000));

        // === VENDAS ===
        missions.add(new Mission("sell_10", "Venda 10 itens", MissionType.SELL_COUNT, 10, 120));
        missions.add(new Mission("sell_50", "Venda 50 itens", MissionType.SELL_COUNT, 50, 500));
        missions.add(new Mission("sell_100", "Venda 100 itens", MissionType.SELL_COUNT, 100, 1000));
        missions.add(new Mission("sell_500", "Venda 500 itens", MissionType.SELL_COUNT, 500, 5000));

        // === MINERAÇÃO ===
        missions.add(new Mission("stone_100", "Quebre 100 pedras", MissionType.BREAK_STONE, 100, 100));
        missions.add(new Mission("stone_500", "Quebre 500 pedras", MissionType.BREAK_STONE, 500, 400));
        missions.add(new Mission("stone_1000", "Quebre 1.000 pedras", MissionType.BREAK_STONE, 1000, 800));
        missions.add(new Mission("ore_20", "Minere 20 minérios", MissionType.BREAK_ORE, 20, 300));
        missions.add(new Mission("ore_50", "Minere 50 minérios", MissionType.BREAK_ORE, 50, 750));
        missions.add(new Mission("ore_100", "Minere 100 minérios", MissionType.BREAK_ORE, 100, 1500));
        missions.add(new Mission("ore_200", "Minere 200 minérios", MissionType.BREAK_ORE, 200, 3000));

        // === MADEIRA ===
        missions.add(new Mission("log_50", "Corte 50 troncos", MissionType.BREAK_LOG, 50, 150));
        missions.add(new Mission("log_100", "Corte 100 troncos", MissionType.BREAK_LOG, 100, 300));
        missions.add(new Mission("log_500", "Corte 500 troncos", MissionType.BREAK_LOG, 500, 1500));
        missions.add(new Mission("log_1000", "Corte 1.000 troncos", MissionType.BREAK_LOG, 1000, 3000));

        // === COMBATE ===
        missions.add(new Mission("kill_10", "Mate 10 mobs", MissionType.KILL_MOB, 10, 200));
        missions.add(new Mission("kill_50", "Mate 50 mobs", MissionType.KILL_MOB, 50, 800));
        missions.add(new Mission("kill_100", "Mate 100 mobs", MissionType.KILL_MOB, 100, 1600));
        missions.add(new Mission("kill_500", "Mate 500 mobs", MissionType.KILL_MOB, 500, 8000));
        missions.add(new Mission("pvp_1", "Mate 1 jogador", MissionType.KILL_PLAYER, 1, 500));
        missions.add(new Mission("pvp_5", "Mate 5 jogadores", MissionType.KILL_PLAYER, 5, 2500));
        missions.add(new Mission("pvp_10", "Mate 10 jogadores", MissionType.KILL_PLAYER, 10, 5000));

        // === FARMING ===
        missions.add(new Mission("harvest_50", "Colha 50 plantações", MissionType.HARVEST_CROP, 50, 200));
        missions.add(new Mission("harvest_200", "Colha 200 plantações", MissionType.HARVEST_CROP, 200, 800));
        missions.add(new Mission("plant_50", "Plante 50 sementes", MissionType.PLANT_CROP, 50, 150));
        missions.add(new Mission("plant_200", "Plante 200 sementes", MissionType.PLANT_CROP, 200, 600));
        missions.add(new Mission("breed_10", "Crie 10 animais", MissionType.BREED_ANIMAL, 10, 300));
        missions.add(new Mission("breed_50", "Crie 50 animais", MissionType.BREED_ANIMAL, 50, 1500));

        // === PESCA ===
        missions.add(new Mission("fish_10", "Pesque 10 peixes", MissionType.FISH_CATCH, 10, 200));
        missions.add(new Mission("fish_50", "Pesque 50 peixes", MissionType.FISH_CATCH, 50, 1000));
        missions.add(new Mission("fish_100", "Pesque 100 peixes", MissionType.FISH_CATCH, 100, 2000));

        // === CRAFTING ===
        missions.add(new Mission("craft_20", "Crie 20 itens", MissionType.CRAFT_ITEM, 20, 150));
        missions.add(new Mission("craft_100", "Crie 100 itens", MissionType.CRAFT_ITEM, 100, 750));
        missions.add(new Mission("smelt_50", "Fundição 50 itens", MissionType.SMELT_ITEM, 50, 200));
        missions.add(new Mission("smelt_200", "Fundição 200 itens", MissionType.SMELT_ITEM, 200, 800));
        missions.add(new Mission("enchant_5", "Encante 5 itens", MissionType.ENCHANT_ITEM, 5, 500));
        missions.add(new Mission("enchant_20", "Encante 20 itens", MissionType.ENCHANT_ITEM, 20, 2000));

        // === SOCIAL ===
        missions.add(new Mission("chat_50", "Envie 50 mensagens", MissionType.CHAT_MESSAGE, 50, 100));
        missions.add(new Mission("trade_5", "Troque com 5 jogadores", MissionType.TRADE_PLAYER, 5, 500));
    }
}

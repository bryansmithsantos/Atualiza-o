package com.example.economia.features.jobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class JobsService {

    private final Plugin plugin;
    private final List<Job> jobs = new ArrayList<>();
    private final Map<UUID, Map<String, Integer>> jobXp = new HashMap<>();
    private File jobsFile;
    private FileConfiguration jobsConfig;

    public JobsService(Plugin plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    public void load() {
        jobsFile = new File(plugin.getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            jobsFile.getParentFile().mkdirs();
            jobsConfig = new YamlConfiguration();
            save();
        }
        jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);
        if (jobsConfig.isConfigurationSection("xp")) {
            for (String key : jobsConfig.getConfigurationSection("xp").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                Map<String, Integer> map = new HashMap<>();
                for (String jobId : jobsConfig.getConfigurationSection("xp." + key).getKeys(false)) {
                    map.put(jobId, jobsConfig.getInt("xp." + key + "." + jobId, 0));
                }
                jobXp.put(uuid, map);
            }
        }
    }

    public void save() {
        if (jobsConfig == null) {
            jobsConfig = new YamlConfiguration();
        }
        for (UUID uuid : jobXp.keySet()) {
            for (Map.Entry<String, Integer> entry : jobXp.get(uuid).entrySet()) {
                jobsConfig.set("xp." + uuid + "." + entry.getKey(), entry.getValue());
            }
        }
        try {
            jobsConfig.save(jobsFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar jobs.yml: " + ex.getMessage());
        }
    }

    public List<Job> getJobs() {
        return List.copyOf(jobs);
    }

    public Job getCurrentJob(Player player) {
        String jobId = jobsConfig.getString("jobs." + player.getUniqueId(), jobs.get(0).id());
        return getJobById(jobId);
    }

    public void setJob(Player player, String jobId) {
        jobsConfig.set("jobs." + player.getUniqueId(), jobId);
        save();
    }

    public int getXp(Player player, String jobId) {
        return jobXp.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(jobId, 0);
    }

    public void addXp(Player player, String jobId, int amount) {
        jobXp.computeIfAbsent(player.getUniqueId(), key -> new HashMap<>())
                .put(jobId, getXp(player, jobId) + Math.max(0, amount));
    }

    public int getLevel(Player player, String jobId) {
        int xp = getXp(player, jobId);
        int perLevel = plugin.getConfig().getInt("jobs.xp-per-level", 100);
        return Math.max(1, (xp / perLevel) + 1);
    }

    public Map<UUID, Integer> getTotalXp() {
        Map<UUID, Integer> totals = new HashMap<>();
        for (Map.Entry<UUID, Map<String, Integer>> entry : jobXp.entrySet()) {
            int total = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
            totals.put(entry.getKey(), total);
        }
        return totals;
    }

    public Job getJobById(String id) {
        for (Job job : jobs) {
            if (job.id().equalsIgnoreCase(id)) {
                return job;
            }
        }
        return jobs.get(0);
    }

    private void registerDefaults() {
        jobs.add(new Job("minerador", "Minerador", Material.IRON_PICKAXE, 20, null, 0));
        jobs.add(new Job("lenhador", "Lenhador", Material.IRON_AXE, 18, null, 0));
        jobs.add(new Job("pescador", "Pescador", Material.FISHING_ROD, 16, null, 0));
        jobs.add(new Job("fazendeiro", "Fazendeiro", Material.WHEAT, 12, null, 0));
        jobs.add(new Job("cacador", "Caçador", Material.BOW, 18, "lic_cacador", 250));
        jobs.add(new Job("ferreiro", "Ferreiro", Material.ANVIL, 22, "lic_ferreiro", 350));
        jobs.add(new Job("pedreiro", "Pedreiro", Material.BRICKS, 15, null, 0));
        jobs.add(new Job("alquimista", "Alquimista", Material.BREWING_STAND, 20, "lic_alquimista", 300));
        jobs.add(new Job("encantador", "Encantador", Material.ENCHANTING_TABLE, 24, "lic_encantador", 400));
        jobs.add(new Job("construtor", "Construtor", Material.STONE_BRICKS, 17, null, 0));
        jobs.add(new Job("cozinheiro", "Cozinheiro", Material.COOKED_BEEF, 14, null, 0));
        jobs.add(new Job("mecanico", "Mecânico", Material.REDSTONE, 16, "lic_mecanico", 280));
        jobs.add(new Job("mercador", "Mercador", Material.EMERALD, 25, "lic_mercador", 500));
        jobs.add(new Job("explorador", "Explorador", Material.COMPASS, 19, null, 0));
        jobs.add(new Job("jardineiro", "Jardineiro", Material.OAK_SAPLING, 13, null, 0));
        jobs.add(new Job("mensageiro", "Mensageiro", Material.FEATHER, 12, null, 0));
        jobs.add(new Job("arqueiro", "Arqueiro", Material.CROSSBOW, 18, "lic_arqueiro", 260));
        jobs.add(new Job("pintor", "Pintor", Material.PAINTING, 11, null, 0));
        jobs.add(new Job("carpinteiro", "Carpinteiro", Material.CRAFTING_TABLE, 16, null, 0));
        jobs.add(new Job("cartografo", "Cartógrafo", Material.MAP, 21, "lic_cartografo", 320));
    }
}

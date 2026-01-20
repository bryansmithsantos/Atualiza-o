package com.example.economia.features.homes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HomeService {

    private final Plugin plugin;
    private final Map<UUID, List<Home>> homes = new HashMap<>();
    private File homesFile;
    private FileConfiguration homesConfig;

    public HomeService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        homesFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            homesFile.getParentFile().mkdirs();
            homesConfig = new YamlConfiguration();
            save();
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);

        if (homesConfig.isConfigurationSection("homes")) {
            for (String uuidStr : homesConfig.getConfigurationSection("homes").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                List<Home> playerHomes = new ArrayList<>();

                String path = "homes." + uuidStr;
                for (String homeName : homesConfig.getConfigurationSection(path).getKeys(false)) {
                    Location loc = homesConfig.getLocation(path + "." + homeName);
                    if (loc != null) {
                        playerHomes.add(new Home(homeName, loc));
                    }
                }
                homes.put(uuid, playerHomes);
            }
        }
    }

    public void save() {
        if (homesConfig == null)
            return;

        // Clear existing to handle deletions properly (simple way)
        homesConfig.set("homes", null);

        for (Map.Entry<UUID, List<Home>> entry : homes.entrySet()) {
            for (Home home : entry.getValue()) {
                homesConfig.set("homes." + entry.getKey() + "." + home.name(), home.location());
            }
        }

        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Falha ao salvar homes.yml: " + e.getMessage());
        }
    }

    public void setHome(Player player, String name) {
        List<Home> list = homes.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());

        // Remove existing with same name
        list.removeIf(h -> h.name().equalsIgnoreCase(name));

        list.add(new Home(name, player.getLocation()));
        save();
    }

    public Home getHome(Player player, String name) {
        List<Home> list = homes.get(player.getUniqueId());
        if (list == null)
            return null;

        return list.stream()
                .filter(h -> h.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean deleteHome(Player player, String name) {
        List<Home> list = homes.get(player.getUniqueId());
        if (list == null)
            return false;

        boolean removed = list.removeIf(h -> h.name().equalsIgnoreCase(name));
        if (removed)
            save();
        return removed;
    }

    public List<Home> getHomes(Player player) {
        return homes.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
}

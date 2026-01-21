package com.example.economia.features.tags;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TagService {

    private final Plugin plugin;
    private final Map<UUID, String> playerTags = new HashMap<>();
    private File tagsFile;
    private FileConfiguration tagsConfig;

    // Default server tags (can be expanded)
    public static final String TAG_VIP = "§6[VIP]";
    public static final String TAG_MVP = "§b[MVP]";
    public static final String TAG_ELITE = "§5[ELITE]";
    public static final String TAG_LEGEND = "§c[LENDA]";
    public static final String TAG_OWNER = "§4[DONO]";
    public static final String TAG_ADMIN = "§c[ADMIN]";
    public static final String TAG_MOD = "§9[MOD]";
    public static final String TAG_HELPER = "§a[HELPER]";
    public static final String TAG_BUILDER = "§e[BUILDER]";
    public static final String TAG_YOUTUBER = "§c[§fYOU§cTUBER]";

    public TagService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        tagsFile = new File(plugin.getDataFolder(), "tags.yml");
        if (!tagsFile.exists()) {
            tagsFile.getParentFile().mkdirs();
            tagsConfig = new YamlConfiguration();
            save();
        }
        tagsConfig = YamlConfiguration.loadConfiguration(tagsFile);

        if (tagsConfig.isConfigurationSection("tags")) {
            for (String key : tagsConfig.getConfigurationSection("tags").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String tag = tagsConfig.getString("tags." + key);
                    playerTags.put(uuid, tag);
                } catch (IllegalArgumentException e) {
                    // Invalid UUID, skip
                }
            }
        }
    }

    public void save() {
        if (tagsConfig == null) {
            tagsConfig = new YamlConfiguration();
        }

        tagsConfig.set("tags", null);
        for (Map.Entry<UUID, String> entry : playerTags.entrySet()) {
            tagsConfig.set("tags." + entry.getKey().toString(), entry.getValue());
        }

        try {
            tagsConfig.save(tagsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Falha ao salvar tags.yml: " + e.getMessage());
        }
    }

    public void setTag(UUID uuid, String tag) {
        if (tag == null || tag.isEmpty()) {
            playerTags.remove(uuid);
        } else {
            playerTags.put(uuid, tag);
        }
        save();
    }

    public String getTag(UUID uuid) {
        return playerTags.get(uuid);
    }

    public boolean hasTag(UUID uuid) {
        return playerTags.containsKey(uuid);
    }

    public void removeTag(UUID uuid) {
        playerTags.remove(uuid);
        save();
    }

    public String getFormattedTag(Player player) {
        String tag = playerTags.get(player.getUniqueId());
        if (tag != null) {
            return tag + " ";
        }

        // Check permissions for default tags
        if (player.hasPermission("blinded.tag.owner"))
            return TAG_OWNER + " ";
        if (player.hasPermission("blinded.tag.admin"))
            return TAG_ADMIN + " ";
        if (player.hasPermission("blinded.tag.mod"))
            return TAG_MOD + " ";
        if (player.hasPermission("blinded.tag.helper"))
            return TAG_HELPER + " ";
        if (player.hasPermission("blinded.tag.legend"))
            return TAG_LEGEND + " ";
        if (player.hasPermission("blinded.tag.elite"))
            return TAG_ELITE + " ";
        if (player.hasPermission("blinded.tag.mvp"))
            return TAG_MVP + " ";
        if (player.hasPermission("blinded.tag.vip"))
            return TAG_VIP + " ";
        if (player.hasPermission("blinded.tag.youtuber"))
            return TAG_YOUTUBER + " ";
        if (player.hasPermission("blinded.tag.builder"))
            return TAG_BUILDER + " ";

        return "";
    }
}

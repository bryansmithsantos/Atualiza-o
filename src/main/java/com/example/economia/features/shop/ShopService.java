package com.example.economia.features.shop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class ShopService {

    private final Plugin plugin;
    private final List<ShopItem> items = new ArrayList<>();
    private File shopFile;
    private FileConfiguration shopConfig;

    public ShopService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        shopFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            shopFile.getParentFile().mkdirs();
            plugin.saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        items.clear();
        for (String key : shopConfig.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + key + ".";
            String name = shopConfig.getString(path + "name", key);
            String materialName = shopConfig.getString(path + "material", "STONE");
            double buy = shopConfig.getDouble(path + "buy", 0.0);
            double sell = shopConfig.getDouble(path + "sell", 0.0);
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("Material inválido em shop.yml: " + materialName);
                continue;
            }
            items.add(new ShopItem(key, name, material, buy, sell));
        }
    }

    public void save() {
        if (shopConfig == null) {
            shopConfig = new YamlConfiguration();
        }
        try {
            shopConfig.save(shopFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar shop.yml: " + ex.getMessage());
        }
    }

    public List<ShopItem> getItems() {
        return List.copyOf(items);
    }

    public ShopItem getByMaterial(Material material) {
        for (ShopItem item : items) {
            if (item.material() == material) {
                return item;
            }
        }
        return null;
    }
}

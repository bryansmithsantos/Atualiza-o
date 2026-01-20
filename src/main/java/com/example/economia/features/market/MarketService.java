package com.example.economia.features.market;

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
import org.bukkit.plugin.Plugin;

public final class MarketService {

    private final Plugin plugin;
    private final Map<String, MarketListing> listings = new HashMap<>();
    private File marketFile;
    private FileConfiguration marketConfig;

    public MarketService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        marketFile = new File(plugin.getDataFolder(), "market.yml");
        if (!marketFile.exists()) {
            marketFile.getParentFile().mkdirs();
            marketConfig = new YamlConfiguration();
            save();
        }
        marketConfig = YamlConfiguration.loadConfiguration(marketFile);
        if (marketConfig.isConfigurationSection("listings")) {
            for (String key : marketConfig.getConfigurationSection("listings").getKeys(false)) {
                String path = "listings." + key + ".";
                UUID seller = UUID.fromString(marketConfig.getString(path + "seller"));
                Material material = Material.matchMaterial(marketConfig.getString(path + "material", "STONE"));
                int amount = marketConfig.getInt(path + "amount", 1);
                double price = marketConfig.getDouble(path + "price", 0.0);
                if (material != null) {
                    listings.put(key, new MarketListing(key, seller, material, amount, price));
                }
            }
        }
    }

    public void save() {
        if (marketConfig == null) {
            marketConfig = new YamlConfiguration();
        }
        marketConfig.set("listings", null);
        for (MarketListing listing : listings.values()) {
            String path = "listings." + listing.id() + ".";
            marketConfig.set(path + "seller", listing.seller().toString());
            marketConfig.set(path + "material", listing.material().name());
            marketConfig.set(path + "amount", listing.amount());
            marketConfig.set(path + "price", listing.price());
        }
        try {
            marketConfig.save(marketFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar market.yml: " + ex.getMessage());
        }
    }

    public List<MarketListing> getListings() {
        return new ArrayList<>(listings.values());
    }

    public void addListing(UUID seller, Material material, int amount, double price) {
        String id = "listing_" + System.currentTimeMillis();
        listings.put(id, new MarketListing(id, seller, material, amount, price));
    }

    public MarketListing getListing(String id) {
        return listings.get(id);
    }

    public boolean removeListing(String id) {
        return listings.remove(id) != null;
    }
}

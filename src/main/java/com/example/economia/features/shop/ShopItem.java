package com.example.economia.features.shop;

import org.bukkit.Material;

public record ShopItem(String id, String displayName, Material material, double buyPrice, double sellPrice) {
}

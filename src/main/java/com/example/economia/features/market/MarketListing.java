package com.example.economia.features.market;

import java.util.UUID;

import org.bukkit.Material;

public record MarketListing(String id, UUID seller, Material material, int amount, double price) {
}

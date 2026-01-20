package com.example.economia.features.generators;

import org.bukkit.Material;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum GeneratorType {

    COAL("Gerador de Carvão", Material.COAL_BLOCK, Material.COAL, 1000.0, 5, NamedTextColor.DARK_GRAY),
    IRON("Gerador de Ferro", Material.IRON_BLOCK, Material.IRON_INGOT, 5000.0, 5, NamedTextColor.GRAY),
    GOLD("Gerador de Ouro", Material.GOLD_BLOCK, Material.GOLD_INGOT, 10000.0, 10, NamedTextColor.GOLD),
    DIAMOND("Gerador de Diamante", Material.DIAMOND_BLOCK, Material.DIAMOND, 50000.0, 15, NamedTextColor.AQUA),
    EMERALD("Gerador de Esmeralda", Material.EMERALD_BLOCK, Material.EMERALD, 100000.0, 20, NamedTextColor.GREEN),
    NETHERITE("Gerador de Netherite", Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT, 500000.0, 30,
            NamedTextColor.DARK_PURPLE);

    private final String name;
    private final Material blockMaterial;
    private final Material dropMaterial;
    private final double price;
    private final int intervalSeconds;
    private final NamedTextColor color;

    GeneratorType(String name, Material blockMaterial, Material dropMaterial, double price, int intervalSeconds,
            NamedTextColor color) {
        this.name = name;
        this.blockMaterial = blockMaterial;
        this.dropMaterial = dropMaterial;
        this.price = price;
        this.intervalSeconds = intervalSeconds;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public Material getDropMaterial() {
        return dropMaterial;
    }

    public double getPrice() {
        return price;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public Component getDisplayName() {
        return Component.text(name).color(color);
    }
}

package com.example.economia.features.generators;

import org.bukkit.Material;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum GeneratorType {

    // === BÁSICOS (Caros) ===
    COAL("Gerador de Carvão", Material.COAL_BLOCK, Material.COAL, 25000.0, 5, NamedTextColor.DARK_GRAY),
    COPPER("Gerador de Cobre", Material.COPPER_BLOCK, Material.COPPER_INGOT, 50000.0, 5, NamedTextColor.GOLD),
    IRON("Gerador de Ferro", Material.IRON_BLOCK, Material.IRON_INGOT, 100000.0, 6, NamedTextColor.GRAY),

    // === INTERMEDIÁRIOS (Muito caros) ===
    LAPIS("Gerador de Lápis", Material.LAPIS_BLOCK, Material.LAPIS_LAZULI, 150000.0, 8, NamedTextColor.BLUE),
    REDSTONE("Gerador de Redstone", Material.REDSTONE_BLOCK, Material.REDSTONE, 200000.0, 8, NamedTextColor.RED),
    GOLD("Gerador de Ouro", Material.GOLD_BLOCK, Material.GOLD_INGOT, 350000.0, 10, NamedTextColor.GOLD),

    // === AVANÇADOS (Extremamente caros) ===
    DIAMOND("Gerador de Diamante", Material.DIAMOND_BLOCK, Material.DIAMOND, 750000.0, 15, NamedTextColor.AQUA),
    EMERALD("Gerador de Esmeralda", Material.EMERALD_BLOCK, Material.EMERALD, 1500000.0, 20, NamedTextColor.GREEN),

    // === LENDÁRIOS (Preço absurdo) ===
    NETHERITE("Gerador de Netherite", Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT, 5000000.0, 30,
            NamedTextColor.DARK_PURPLE),
    AMETHYST("Gerador de Ametista", Material.AMETHYST_BLOCK, Material.AMETHYST_SHARD, 2500000.0, 25,
            NamedTextColor.LIGHT_PURPLE);

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

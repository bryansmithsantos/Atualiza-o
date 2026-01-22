package com.example.economia.features.dungeon;

public enum DungeonDifficulty {
    NIVEL_1("Cripta dos Mortos", 2000, 20000, 5, "§a"),
    NIVEL_2("Caverna Sombria", 10000, 100000, 6, "§e"),
    NIVEL_3("Fortaleza das Trevas", 30000, 300000, 8, "§6"),
    NIVEL_4("Abismo Infernal", 100000, 1000000, 10, "§c"),
    NIVEL_5("Portal do Caos", 300000, 3000000, 12, "§5");

    private final String name;
    private final double entryCost;
    private final double maxReward;
    private final int waves;
    private final String color;

    DungeonDifficulty(String name, double entryCost, double maxReward, int waves, String color) {
        this.name = name;
        this.entryCost = entryCost;
        this.maxReward = maxReward;
        this.waves = waves;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public double getEntryCost() {
        return entryCost;
    }

    public double getMaxReward() {
        return maxReward;
    }

    public int getWaves() {
        return waves;
    }

    public String getColor() {
        return color;
    }

    public double getRewardPerWave() {
        return maxReward / waves;
    }

    public int getBossHP() {
        return switch (this) {
            case NIVEL_1 -> 500;
            case NIVEL_2 -> 1000;
            case NIVEL_3 -> 2000;
            case NIVEL_4 -> 4000;
            case NIVEL_5 -> 8000;
        };
    }

    public int getMobsPerWave(int wave) {
        int base = 10 + (this.ordinal() * 5);
        return base + (wave * 3);
    }
}

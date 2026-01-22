package com.example.economia.features.dungeon;

public enum DungeonDifficulty {
    NIVEL_1("Cripta dos Mortos", 500, 5000, 5, "§a"),
    NIVEL_2("Caverna Sombria", 2000, 20000, 6, "§e"),
    NIVEL_3("Fortaleza das Trevas", 5000, 50000, 8, "§6"),
    NIVEL_4("Abismo Infernal", 15000, 150000, 10, "§c"),
    NIVEL_5("Portal do Caos", 40000, 400000, 12, "§5"),
    NIVEL_6("Reino do Vazio", 100000, 1000000, 14, "§b"),
    NIVEL_7("Dimensão Esquecida", 250000, 2500000, 16, "§d"),
    NIVEL_8("Pesadelo Ancestral", 500000, 5000000, 18, "§1"),
    NIVEL_9("Domínio do Soberano", 1000000, 10000000, 20, "§0"),
    NIVEL_10("Fim da Existência", 2000000, 20000000, 25, "§k§l");

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
            case NIVEL_6 -> 15000;
            case NIVEL_7 -> 25000;
            case NIVEL_8 -> 50000;
            case NIVEL_9 -> 100000;
            case NIVEL_10 -> 250000;
        };
    }

    public int getMobsPerWave(int wave) {
        int base = 10 + (this.ordinal() * 5);
        return base + (wave * 3);
    }
}

package com.example.economia.features.generators;

import java.util.UUID;

import org.bukkit.Location;

public class Generator {

    private final UUID id;
    private final UUID ownerId;
    private final Location location;
    private final GeneratorType type;
    private long lastGenerationTime;

    public Generator(UUID id, UUID ownerId, Location location, GeneratorType type) {
        this.id = id;
        this.ownerId = ownerId;
        this.location = location;
        this.type = type;
        this.lastGenerationTime = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Location getLocation() {
        return location;
    }

    public GeneratorType getType() {
        return type;
    }

    public long getLastGenerationTime() {
        return lastGenerationTime;
    }

    public void setLastGenerationTime(long lastGenerationTime) {
        this.lastGenerationTime = lastGenerationTime;
    }
}

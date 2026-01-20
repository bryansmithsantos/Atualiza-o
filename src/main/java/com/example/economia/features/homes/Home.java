package com.example.economia.features.homes;

import org.bukkit.Location;

public record Home(String name, Location location) {

    // Helper helper to serialize/deserialize if needed, or just use config
    // getLocation

    public String getWorldName() {
        return location.getWorld().getName();
    }

    public double getX() {
        return location.getX();
    }

    public double getY() {
        return location.getY();
    }

    public double getZ() {
        return location.getZ();
    }

    public float getYaw() {
        return location.getYaw();
    }

    public float getPitch() {
        return location.getPitch();
    }
}

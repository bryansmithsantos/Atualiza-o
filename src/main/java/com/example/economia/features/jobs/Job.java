package com.example.economia.features.jobs;

import org.bukkit.Material;

public record Job(String id, String displayName, Material icon, double basePay, String licenseId, double licensePrice) {
}

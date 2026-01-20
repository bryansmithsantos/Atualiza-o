package com.example.economia.features.tax;

import org.bukkit.plugin.Plugin;

public final class TaxService {

    private final Plugin plugin;

    public TaxService(Plugin plugin) {
        this.plugin = plugin;
    }

    public double applyTax(double amount, String path) {
        double rate = plugin.getConfig().getDouble(path, 0.0);
        if (rate <= 0) {
            return amount;
        }
        return Math.max(0, amount - (amount * rate));
    }

    public double taxValue(double amount, String path) {
        double rate = plugin.getConfig().getDouble(path, 0.0);
        if (rate <= 0) {
            return 0.0;
        }
        return amount * rate;
    }
}

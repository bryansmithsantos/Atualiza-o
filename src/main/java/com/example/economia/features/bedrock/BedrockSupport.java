package com.example.economia.features.bedrock;

import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class BedrockSupport {

    private final Plugin plugin;
    private final boolean geyserPresent;
    private final boolean floodgatePresent;

    public BedrockSupport(Plugin plugin) {
        this.plugin = plugin;
        this.geyserPresent = Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null;
        this.floodgatePresent = Bukkit.getPluginManager().getPlugin("floodgate") != null;
        logStatus();
    }

    public boolean isAvailable() {
        return geyserPresent || floodgatePresent;
    }

    public boolean isBedrock(Player player) {
        if (!floodgatePresent) {
            return false;
        }
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Method getInstance = apiClass.getMethod("getInstance");
            Object api = getInstance.invoke(null);
            Method isFloodgatePlayer = apiClass.getMethod("isFloodgatePlayer", UUID.class);
            Object result = isFloodgatePlayer.invoke(api, player.getUniqueId());
            return result instanceof Boolean && (Boolean) result;
        } catch (Exception ex) {
            plugin.getLogger().warning("Falha ao detectar jogador Bedrock via Floodgate: " + ex.getMessage());
            return false;
        }
    }

    private void logStatus() {
        if (geyserPresent && floodgatePresent) {
            plugin.getLogger().info("Integração Bedrock ativa (Geyser + Floodgate detectados)." );
        } else if (geyserPresent) {
            plugin.getLogger().info("Geyser detectado. Floodgate não encontrado.");
        } else if (floodgatePresent) {
            plugin.getLogger().info("Floodgate detectado. Geyser não encontrado.");
        } else {
            plugin.getLogger().info("Integração Bedrock não detectada (Geyser/Floodgate ausentes)." );
        }
    }
}

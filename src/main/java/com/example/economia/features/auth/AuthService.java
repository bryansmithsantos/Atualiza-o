package com.example.economia.features.auth;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class AuthService {

    private static final int SALT_LENGTH = 16;

    private final Plugin plugin;
    private final SecureRandom random = new SecureRandom();
    private final Map<UUID, AuthRecord> records = new HashMap<>();
    private final Set<UUID> loggedIn = new HashSet<>();
    private final Map<UUID, AuthRequest> pending = new HashMap<>();

    private File authFile;
    private FileConfiguration authConfig;

    public AuthService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        authFile = new File(plugin.getDataFolder(), "auth.yml");
        if (!authFile.exists()) {
            authFile.getParentFile().mkdirs();
            authConfig = new YamlConfiguration();
            save();
        }
        authConfig = YamlConfiguration.loadConfiguration(authFile);
        if (authConfig.isConfigurationSection("auth")) {
            for (String key : authConfig.getConfigurationSection("auth").getKeys(false)) {
                String salt = authConfig.getString("auth." + key + ".salt", "");
                String hash = authConfig.getString("auth." + key + ".hash", "");
                if (!salt.isEmpty() && !hash.isEmpty()) {
                    records.put(UUID.fromString(key), new AuthRecord(salt, hash));
                }
            }
        }
    }

    public void save() {
        if (authConfig == null) {
            authConfig = new YamlConfiguration();
        }
        for (Map.Entry<UUID, AuthRecord> entry : records.entrySet()) {
            authConfig.set("auth." + entry.getKey() + ".salt", entry.getValue().salt());
            authConfig.set("auth." + entry.getKey() + ".hash", entry.getValue().hash());
        }
        try {
            authConfig.save(authFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar auth.yml: " + ex.getMessage());
        }
    }

    public boolean isRegistered(UUID uuid) {
        return records.containsKey(uuid);
    }

    public boolean isLoggedIn(UUID uuid) {
        return loggedIn.contains(uuid);
    }

    public void setLoggedIn(UUID uuid, boolean value) {
        if (value) {
            loggedIn.add(uuid);
        } else {
            loggedIn.remove(uuid);
        }
    }

    public void requestInput(Player player, AuthRequest request) {
        pending.put(player.getUniqueId(), request);
    }

    public AuthRequest consumePending(Player player) {
        return pending.remove(player.getUniqueId());
    }

    public boolean register(Player player, String password) {
        UUID uuid = player.getUniqueId();
        if (records.containsKey(uuid)) {
            return false;
        }
        String salt = createSalt();
        String hash = hash(salt, password);
        records.put(uuid, new AuthRecord(salt, hash));
        save();
        return true;
    }

    public boolean verify(Player player, String password) {
        AuthRecord record = records.get(player.getUniqueId());
        if (record == null) {
            return false;
        }
        String hash = hash(record.salt(), password);
        return hash.equals(record.hash());
    }

    public int getMinPasswordLength() {
        return plugin.getConfig().getInt("auth.min-password-length", 4);
    }

    private String createSalt() {
        byte[] bytes = new byte[SALT_LENGTH];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String hash(String salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Hash falhou", ex);
        }
    }

    private record AuthRecord(String salt, String hash) {}
}

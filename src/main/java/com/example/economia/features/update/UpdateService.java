package com.example.economia.features.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class UpdateService {

    private final Plugin plugin;
    private final HttpClient httpClient;

    public UpdateService(Plugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public UpdateInfo checkForUpdate() {
        String directUrl = plugin.getConfig().getString("update.direct-url", "").trim();
        if (!directUrl.isEmpty()) {
            return new UpdateInfo("direct", directUrl);
        }
        String repo = plugin.getConfig().getString("update.repo", "").trim();
        String asset = plugin.getConfig().getString("update.asset", "").trim();
        if (repo.isEmpty() || asset.isEmpty()) {
            return null;
        }
        try {
            String api = "https://api.github.com/repos/" + repo + "/releases/latest";
            HttpRequest request = HttpRequest.newBuilder(URI.create(api))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                plugin.getLogger().warning("Falha ao buscar release: " + response.statusCode());
                return null;
            }
            String body = response.body();
            String tag = extractJsonValue(body, "tag_name");
            String url = extractAssetUrl(body, asset);
            if (url == null) {
                plugin.getLogger().warning("Asset não encontrado no release: " + asset);
                return null;
            }
            return new UpdateInfo(tag == null ? "" : tag, url);
        } catch (Exception ex) {
            plugin.getLogger().warning("Erro ao verificar update: " + ex.getMessage());
            return null;
        }
    }

    public boolean download(UpdateInfo info) {
        if (info == null || info.downloadUrl() == null || info.downloadUrl().isEmpty()) {
            return false;
        }
        try {
            File updateDir = new File(plugin.getDataFolder().getParentFile(), "update");
            if (!updateDir.exists()) {
                updateDir.mkdirs();
            }
            String fileName = plugin.getConfig().getString("update.save-as", plugin.getName() + ".jar");
            File outFile = new File(updateDir, fileName);

            HttpRequest request = HttpRequest.newBuilder(URI.create(info.downloadUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                plugin.getLogger().warning("Falha no download: " + response.statusCode());
                return false;
            }
            try (InputStream in = response.body(); FileOutputStream out = new FileOutputStream(outFile)) {
                in.transferTo(out);
            }
            plugin.getLogger().info("Update baixado para: " + outFile.getAbsolutePath());
            return Files.exists(outFile.toPath());
        } catch (Exception ex) {
            plugin.getLogger().warning("Erro ao baixar update: " + ex.getMessage());
            return false;
        }
    }

    public void shutdownServer() {
        Bukkit.getScheduler().runTask(plugin, Bukkit::shutdown);
    }

    private String extractJsonValue(String json, String key) {
        String token = "\"" + key + "\"";
        int idx = json.indexOf(token);
        if (idx == -1) {
            return null;
        }
        int start = json.indexOf('"', idx + token.length());
        if (start == -1) {
            return null;
        }
        int end = json.indexOf('"', start + 1);
        if (end == -1) {
            return null;
        }
        return json.substring(start + 1, end);
    }

    private String extractAssetUrl(String json, String assetName) {
        String nameToken = "\"name\":\"" + assetName + "\"";
        int nameIdx = json.indexOf(nameToken);
        if (nameIdx == -1) {
            return null;
        }
        int urlIdx = json.indexOf("\"browser_download_url\":\"", nameIdx);
        if (urlIdx == -1) {
            return null;
        }
        int start = json.indexOf('"', urlIdx + "\"browser_download_url\":".length());
        if (start == -1) {
            return null;
        }
        int end = json.indexOf('"', start + 1);
        if (end == -1) {
            return null;
        }
        return json.substring(start + 1, end);
    }
}

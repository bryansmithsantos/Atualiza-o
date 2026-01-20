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

/**
 * Sistema de atualização baseado em commits do GitHub.
 * Verifica o último commit e baixa o JAR da pasta dist/ do repositório.
 */
public final class UpdateService {

    private final Plugin plugin;
    private final HttpClient httpClient;
    private String lastKnownCommit;

    public UpdateService(Plugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        loadLastCommit();
    }

    private void loadLastCommit() {
        File file = new File(plugin.getDataFolder(), "last_commit.txt");
        if (file.exists()) {
            try {
                lastKnownCommit = Files.readString(file.toPath()).trim();
            } catch (Exception e) {
                lastKnownCommit = "";
            }
        } else {
            lastKnownCommit = "";
        }
    }

    private void saveLastCommit(String sha) {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            File file = new File(plugin.getDataFolder(), "last_commit.txt");
            Files.writeString(file.toPath(), sha);
            lastKnownCommit = sha;
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao salvar commit: " + e.getMessage());
        }
    }

    /**
     * Verifica se há um novo commit no repositório.
     * 
     * @return UpdateInfo com informações do commit, ou null se não houver novidades
     */
    public UpdateInfo checkForUpdate() {
        String repo = plugin.getConfig().getString("update.repo", "").trim();
        String branch = plugin.getConfig().getString("update.branch", "main").trim();
        String jarPath = plugin.getConfig().getString("update.jar-path", "dist/Blinded.jar").trim();

        if (repo.isEmpty()) {
            plugin.getLogger().warning("Repositório não configurado em config.yml (update.repo)");
            return null;
        }

        try {
            // Busca o último commit da branch
            String api = "https://api.github.com/repos/" + repo + "/commits/" + branch;
            HttpRequest request = HttpRequest.newBuilder(URI.create(api))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                plugin.getLogger().warning("Falha ao buscar commits: " + response.statusCode());
                return null;
            }

            String body = response.body();
            String sha = extractJsonValue(body, "sha");
            String message = extractCommitMessage(body);
            String date = extractCommitDate(body);

            if (sha == null) {
                plugin.getLogger().warning("Não foi possível obter SHA do commit");
                return null;
            }

            // Verifica se é um commit novo
            if (sha.equals(lastKnownCommit)) {
                return null; // Sem atualizações
            }

            // URL para baixar o arquivo raw do GitHub
            String downloadUrl = "https://raw.githubusercontent.com/" + repo + "/" + branch + "/" + jarPath;

            String shortSha = sha.length() > 7 ? sha.substring(0, 7) : sha;
            String version = shortSha + (message != null ? " - " + truncate(message, 50) : "");

            UpdateInfo info = new UpdateInfo(version, downloadUrl);
            info.setSha(sha);
            info.setCommitMessage(message);
            info.setCommitDate(date);

            return info;

        } catch (Exception ex) {
            plugin.getLogger().warning("Erro ao verificar update: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Baixa o JAR atualizado para plugins/update/
     */
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

            plugin.getLogger().info("Baixando update de: " + info.downloadUrl());

            HttpRequest request = HttpRequest.newBuilder(URI.create(info.downloadUrl()))
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                plugin.getLogger().warning("Falha no download: HTTP " + response.statusCode());
                return false;
            }

            try (InputStream in = response.body(); FileOutputStream out = new FileOutputStream(outFile)) {
                long bytes = in.transferTo(out);
                plugin.getLogger().info("Download completo: " + bytes + " bytes");
            }

            if (!Files.exists(outFile.toPath()) || Files.size(outFile.toPath()) < 1000) {
                plugin.getLogger().warning("Arquivo baixado parece inválido ou muito pequeno");
                return false;
            }

            // Salva o SHA do commit baixado
            if (info.getSha() != null) {
                saveLastCommit(info.getSha());
            }

            plugin.getLogger().info("Update baixado para: " + outFile.getAbsolutePath());
            return true;

        } catch (Exception ex) {
            plugin.getLogger().warning("Erro ao baixar update: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Desliga o servidor para aplicar a atualização
     */
    public void shutdownServer() {
        Bukkit.getScheduler().runTask(plugin, Bukkit::shutdown);
    }

    public String getLastKnownCommit() {
        return lastKnownCommit;
    }

    private String extractJsonValue(String json, String key) {
        String token = "\"" + key + "\"";
        int idx = json.indexOf(token);
        if (idx == -1)
            return null;

        // Pular até o :
        int colonIdx = json.indexOf(':', idx + token.length());
        if (colonIdx == -1)
            return null;

        // Encontrar o valor (pode ser string ou outro)
        int start = -1;
        for (int i = colonIdx + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                start = i + 1;
                break;
            } else if (!Character.isWhitespace(c)) {
                break;
            }
        }

        if (start == -1)
            return null;

        int end = json.indexOf('"', start);
        if (end == -1)
            return null;

        return json.substring(start, end);
    }

    private String extractCommitMessage(String json) {
        // Procura "message" dentro de "commit"
        int commitIdx = json.indexOf("\"commit\"");
        if (commitIdx == -1)
            return null;

        int messageIdx = json.indexOf("\"message\"", commitIdx);
        if (messageIdx == -1)
            return null;

        int colonIdx = json.indexOf(':', messageIdx);
        if (colonIdx == -1)
            return null;

        int start = json.indexOf('"', colonIdx + 1);
        if (start == -1)
            return null;

        int end = start + 1;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '"' && json.charAt(end - 1) != '\\') {
                break;
            }
            end++;
        }

        if (end >= json.length())
            return null;

        String message = json.substring(start + 1, end);
        // Pegar só a primeira linha
        int newline = message.indexOf("\\n");
        if (newline != -1) {
            message = message.substring(0, newline);
        }
        return message.replace("\\\"", "\"");
    }

    private String extractCommitDate(String json) {
        // Procura "date" dentro de "committer" dentro de "commit"
        int commitIdx = json.indexOf("\"commit\"");
        if (commitIdx == -1)
            return null;

        int committerIdx = json.indexOf("\"committer\"", commitIdx);
        if (committerIdx == -1)
            return null;

        int dateIdx = json.indexOf("\"date\"", committerIdx);
        if (dateIdx == -1)
            return null;

        return extractJsonValue(json.substring(dateIdx - 10), "date");
    }

    private String truncate(String str, int maxLen) {
        if (str == null)
            return "";
        if (str.length() <= maxLen)
            return str;
        return str.substring(0, maxLen - 3) + "...";
    }
}

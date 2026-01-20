package com.example.economia.features.update;

/**
 * Informações sobre uma atualização disponível.
 */
public final class UpdateInfo {

    private final String version;
    private final String downloadUrl;
    private String sha;
    private String commitMessage;
    private String commitDate;

    public UpdateInfo(String version, String downloadUrl) {
        this.version = version;
        this.downloadUrl = downloadUrl;
    }

    public String version() {
        return version;
    }

    public String downloadUrl() {
        return downloadUrl;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }
}

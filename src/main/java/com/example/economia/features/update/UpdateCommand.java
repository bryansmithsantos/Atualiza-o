package com.example.economia.features.update;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class UpdateCommand implements CommandExecutor {

    private final UpdateService updateService;

    public UpdateCommand(UpdateService updateService) {
        this.updateService = updateService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("blinded.admin")) {
            sender.sendMessage(ChatColor.RED + "Sem permissão.");
            return true;
        }

        String sub = args.length > 0 ? args[0].toLowerCase() : "help";

        switch (sub) {
            case "check" -> {
                sender.sendMessage(ChatColor.YELLOW + "Verificando atualizações...");
                UpdateInfo info = updateService.checkForUpdate();
                if (info == null) {
                    sender.sendMessage(ChatColor.GREEN + "✓ Plugin está atualizado!");
                    String lastCommit = updateService.getLastKnownCommit();
                    if (lastCommit != null && !lastCommit.isEmpty()) {
                        sender.sendMessage(ChatColor.GRAY + "Último commit: "
                                + lastCommit.substring(0, Math.min(7, lastCommit.length())));
                    }
                    return true;
                }
                sender.sendMessage("");
                sender.sendMessage(ChatColor.AQUA + "═══ Nova Atualização Disponível ═══");
                sender.sendMessage(ChatColor.WHITE + "Commit: " + ChatColor.YELLOW + info.version());
                if (info.getCommitMessage() != null) {
                    sender.sendMessage(ChatColor.WHITE + "Mensagem: " + ChatColor.GRAY + info.getCommitMessage());
                }
                if (info.getCommitDate() != null) {
                    sender.sendMessage(ChatColor.WHITE + "Data: " + ChatColor.GRAY + info.getCommitDate());
                }
                sender.sendMessage("");
                sender.sendMessage(ChatColor.GREEN + "Use /blinded download para atualizar!");
                sender.sendMessage(ChatColor.AQUA + "═══════════════════════════════════");
                return true;
            }

            case "download", "update" -> {
                sender.sendMessage(ChatColor.YELLOW + "Verificando atualizações...");
                UpdateInfo info = updateService.checkForUpdate();
                if (info == null) {
                    sender.sendMessage(ChatColor.GREEN + "✓ Plugin já está na versão mais recente!");
                    return true;
                }

                sender.sendMessage(ChatColor.YELLOW + "Baixando atualização: " + info.version());

                if (!updateService.download(info)) {
                    sender.sendMessage(ChatColor.RED + "✗ Falha ao baixar atualização.");
                    sender.sendMessage(ChatColor.RED + "Verifique se o JAR existe em dist/ no repositório.");
                    return true;
                }

                sender.sendMessage(ChatColor.GREEN + "✓ Update baixado com sucesso!");
                sender.sendMessage(ChatColor.YELLOW + "Reiniciando servidor para aplicar...");
                updateService.shutdownServer();
                return true;
            }

            case "force" -> {
                sender.sendMessage(ChatColor.YELLOW + "Forçando download da última versão...");

                // Criar info manualmente para forçar download
                String repo = "bryansmithsantos/Atualiza-o";
                String downloadUrl = "https://raw.githubusercontent.com/" + repo + "/main/dist/Blinded.jar";
                UpdateInfo info = new UpdateInfo("force", downloadUrl);

                if (!updateService.download(info)) {
                    sender.sendMessage(ChatColor.RED + "✗ Falha ao baixar.");
                    return true;
                }

                sender.sendMessage(ChatColor.GREEN + "✓ Download forçado concluído!");
                sender.sendMessage(ChatColor.YELLOW + "Reiniciando servidor...");
                updateService.shutdownServer();
                return true;
            }

            default -> {
                sender.sendMessage("");
                sender.sendMessage(ChatColor.AQUA + "═══ Blinded - Sistema de Atualização ═══");
                sender.sendMessage(
                        ChatColor.YELLOW + "/blinded check " + ChatColor.WHITE + "- Verifica se há atualizações");
                sender.sendMessage(
                        ChatColor.YELLOW + "/blinded download " + ChatColor.WHITE + "- Baixa e aplica atualização");
                sender.sendMessage(
                        ChatColor.YELLOW + "/blinded force " + ChatColor.WHITE + "- Força download (ignora cache)");
                sender.sendMessage(ChatColor.AQUA + "════════════════════════════════════════");
                return true;
            }
        }
    }
}

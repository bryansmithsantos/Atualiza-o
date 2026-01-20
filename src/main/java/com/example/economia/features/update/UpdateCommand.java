package com.example.economia.features.update;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class UpdateCommand implements CommandExecutor {

    private final UpdateService updateService;

    public UpdateCommand(UpdateService updateService) {
        this.updateService = updateService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("blinded.admin")) {
            sender.sendMessage(Component.text("Sem permissão.").color(NamedTextColor.RED));
            return true;
        }

        String sub = args.length > 0 ? args[0].toLowerCase() : "help";

        switch (sub) {
            case "check" -> {
                sender.sendMessage(Component.text("Verificando atualizações...").color(NamedTextColor.YELLOW));
                UpdateInfo info = updateService.checkForUpdate();
                if (info == null) {
                    sender.sendMessage(Component.text("✓ Plugin está atualizado!").color(NamedTextColor.GREEN));
                    String lastCommit = updateService.getLastKnownCommit();
                    if (lastCommit != null && !lastCommit.isEmpty()) {
                        sender.sendMessage(Component
                                .text("Último commit: " + lastCommit.substring(0, Math.min(7, lastCommit.length())))
                                .color(NamedTextColor.GRAY));
                    }
                    return true;
                }
                sender.sendMessage(Component.empty());
                sender.sendMessage(Component.text("═══ Nova Atualização Disponível ═══").color(NamedTextColor.AQUA)
                        .decorate(TextDecoration.BOLD));
                sender.sendMessage(Component.text("Commit: ").color(NamedTextColor.WHITE)
                        .append(Component.text(info.version()).color(NamedTextColor.YELLOW)));
                if (info.getCommitMessage() != null) {
                    sender.sendMessage(Component.text("Mensagem: ").color(NamedTextColor.WHITE)
                            .append(Component.text(info.getCommitMessage()).color(NamedTextColor.GRAY)));
                }
                if (info.getCommitDate() != null) {
                    sender.sendMessage(Component.text("Data: ").color(NamedTextColor.WHITE)
                            .append(Component.text(info.getCommitDate()).color(NamedTextColor.GRAY)));
                }
                sender.sendMessage(Component.empty());
                sender.sendMessage(Component.text("Use /blinded download para atualizar!").color(NamedTextColor.GREEN));
                sender.sendMessage(Component.text("═══════════════════════════════════").color(NamedTextColor.AQUA));
                return true;
            }

            case "download", "update" -> {
                sender.sendMessage(Component.text("Verificando atualizações...").color(NamedTextColor.YELLOW));
                UpdateInfo info = updateService.checkForUpdate();
                if (info == null) {
                    sender.sendMessage(
                            Component.text("✓ Plugin já está na versão mais recente!").color(NamedTextColor.GREEN));
                    return true;
                }

                sender.sendMessage(
                        Component.text("Baixando atualização: " + info.version()).color(NamedTextColor.YELLOW));

                if (!updateService.download(info)) {
                    sender.sendMessage(Component.text("✗ Falha ao baixar atualização.").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("Verifique se o JAR existe em dist/ no repositório.")
                            .color(NamedTextColor.RED));
                    return true;
                }

                sender.sendMessage(Component.text("✓ Update baixado com sucesso!").color(NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Reiniciando servidor para aplicar...").color(NamedTextColor.YELLOW));
                updateService.restartServer();
                return true;
            }

            case "force" -> {
                sender.sendMessage(
                        Component.text("Forçando download da última versão...").color(NamedTextColor.YELLOW));

                // Criar info manualmente para forçar download
                String repo = "bryansmithsantos/Atualiza-o";
                String downloadUrl = "https://raw.githubusercontent.com/" + repo + "/main/dist/Blinded.jar";
                UpdateInfo info = new UpdateInfo("force", downloadUrl);

                if (!updateService.download(info)) {
                    sender.sendMessage(Component.text("✗ Falha ao baixar.").color(NamedTextColor.RED));
                    return true;
                }

                sender.sendMessage(Component.text("✓ Download forçado concluído!").color(NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Reiniciando servidor...").color(NamedTextColor.YELLOW));
                updateService.restartServer();
                return true;
            }

            default -> {
                sender.sendMessage(Component.empty());
                sender.sendMessage(Component.text("═══ Blinded - Sistema de Atualização ═══").color(NamedTextColor.AQUA)
                        .decorate(TextDecoration.BOLD));
                sender.sendMessage(Component.text("/blinded check ").color(NamedTextColor.YELLOW)
                        .append(Component.text("- Verifica se há atualizações").color(NamedTextColor.WHITE)));
                sender.sendMessage(Component.text("/blinded download ").color(NamedTextColor.YELLOW)
                        .append(Component.text("- Baixa e aplica atualização").color(NamedTextColor.WHITE)));
                sender.sendMessage(Component.text("/blinded force ").color(NamedTextColor.YELLOW)
                        .append(Component.text("- Força download (ignora cache)").color(NamedTextColor.WHITE)));
                sender.sendMessage(
                        Component.text("════════════════════════════════════════").color(NamedTextColor.AQUA));
                return true;
            }
        }
    }
}

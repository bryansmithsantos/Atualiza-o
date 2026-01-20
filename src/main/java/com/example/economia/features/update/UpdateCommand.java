package com.example.economia.features.update;

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
            sender.sendMessage("Sem permissão.");
            return true;
        }
        String sub = args.length > 0 ? args[0].toLowerCase() : "download";
        if (sub.equals("check")) {
            UpdateInfo info = updateService.checkForUpdate();
            if (info == null) {
                sender.sendMessage("Nenhuma atualização encontrada.");
                return true;
            }
            sender.sendMessage("Update disponível: " + info.version());
            return true;
        }
        UpdateInfo info = updateService.checkForUpdate();
        if (info == null) {
            sender.sendMessage("Nenhuma atualização encontrada.");
            return true;
        }
        if (!updateService.download(info)) {
            sender.sendMessage("Falha ao baixar atualização.");
            return true;
        }
        sender.sendMessage("Update baixado. Reiniciando servidor...");
        updateService.shutdownServer();
        return true;
    }
}

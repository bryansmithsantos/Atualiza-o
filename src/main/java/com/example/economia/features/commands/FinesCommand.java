package com.example.economia.features.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.economia.features.fines.FinesService;
import com.example.economia.features.logs.LogService;

public final class FinesCommand implements CommandExecutor {

    private final FinesService finesService;
    private final LogService logService;

    public FinesCommand(FinesService finesService, LogService logService) {
        this.finesService = finesService;
        this.logService = logService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("blinded.admin")) {
            sender.sendMessage("Sem permissão.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("Use: /" + label + " add <jogador> <valor> <motivo>");
            return true;
        }
        if (!args[0].equalsIgnoreCase("add")) {
            sender.sendMessage("Use: /" + label + " add <jogador> <valor> <motivo>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Jogador não encontrado.");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[2].replace(',', '.'));
        } catch (NumberFormatException ex) {
            sender.sendMessage("Valor inválido.");
            return true;
        }
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
        if (reason.isBlank()) {
            reason = "Multa";
        }
        finesService.addFine(target.getUniqueId(), reason, amount);
        logService.add(target.getUniqueId(), "Multa aplicada: " + reason + " -" + amount);
        sender.sendMessage("Multa aplicada.");
        target.sendMessage("Você recebeu uma multa: " + reason + " (" + amount + ")");
        return true;
    }
}

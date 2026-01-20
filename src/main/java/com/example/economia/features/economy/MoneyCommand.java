package com.example.economia.features.economy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.example.economia.features.auth.AuthService;
import com.example.economia.features.logs.LogService;
import com.example.economia.features.tax.TaxService;

public final class MoneyCommand implements CommandExecutor, TabCompleter {

    private final EconomyService economyService;
    private final AuthService authService;
    private final TaxService taxService;
    private final LogService logService;

    public MoneyCommand(EconomyService economyService, AuthService authService, TaxService taxService, LogService logService) {
        this.economyService = economyService;
        this.authService = authService;
        this.taxService = taxService;
        this.logService = logService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !authService.isLoggedIn(player.getUniqueId())) {
            sender.sendMessage("Você precisa fazer login.");
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Use: /" + label + " <saldo|pay|set|add|take>");
                return true;
            }
            sender.sendMessage("Saldo: " + economyService.formatBalance(player.getUniqueId()));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "saldo":
            case "balance":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Somente jogadores podem ver o saldo próprio.");
                    return true;
                }
                sender.sendMessage("Saldo: " + economyService.formatBalance(player.getUniqueId()));
                return true;
            case "pay":
                if (!(sender instanceof Player playerPay)) {
                    sender.sendMessage("Somente jogadores podem usar /" + label + " pay.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("Use: /" + label + " pay <jogador> <valor>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage("Jogador não encontrado.");
                    return true;
                }
                double amountPay = parseAmount(args[2]);
                if (amountPay <= 0) {
                    sender.sendMessage("Valor inválido.");
                    return true;
                }
                if (!economyService.removeBalance(playerPay.getUniqueId(), amountPay)) {
                    sender.sendMessage("Saldo insuficiente.");
                    return true;
                }
                double net = taxService.applyTax(amountPay, "tax.pay");
                economyService.addBalance(target.getUniqueId(), net);
                sender.sendMessage("Você enviou " + economyService.getCurrencySymbol() + String.format("%.2f", amountPay)
                    + " para " + target.getName() + ".");
                target.sendMessage("Você recebeu " + economyService.getCurrencySymbol() + String.format("%.2f", net)
                    + " de " + playerPay.getName() + ".");
                logService.add(playerPay.getUniqueId(), "Transferência -" + economyService.getCurrencySymbol() + String.format("%.2f", amountPay));
                logService.add(target.getUniqueId(), "Recebido +" + economyService.getCurrencySymbol() + String.format("%.2f", net));
                return true;
            case "set":
            case "add":
            case "take":
                if (!sender.hasPermission("blinded.admin")) {
                    sender.sendMessage("Sem permissão.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("Use: /" + label + " " + sub + " <jogador> <valor>");
                    return true;
                }
                Player adminTarget = Bukkit.getPlayerExact(args[1]);
                if (adminTarget == null) {
                    sender.sendMessage("Jogador não encontrado.");
                    return true;
                }
                double amountAdmin = parseAmount(args[2]);
                if (amountAdmin < 0) {
                    sender.sendMessage("Valor inválido.");
                    return true;
                }
                if (sub.equals("set")) {
                    economyService.setBalance(adminTarget.getUniqueId(), amountAdmin);
                } else if (sub.equals("add")) {
                    economyService.addBalance(adminTarget.getUniqueId(), amountAdmin);
                } else if (sub.equals("take")) {
                    economyService.removeBalance(adminTarget.getUniqueId(), amountAdmin);
                }
                sender.sendMessage("Saldo de " + adminTarget.getName() + " atualizado.");
                return true;
            default:
                sender.sendMessage("Use: /" + label + " <saldo|pay|set|add|take>");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("saldo", "pay", "set", "add", "take"), args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("pay") || sub.equals("set") || sub.equals("add") || sub.equals("take")) {
                List<String> names = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    names.add(player.getName());
                }
                return filter(names, args[1]);
            }
        }
        return List.of();
    }

    private List<String> filter(List<String> base, String token) {
        String lower = token.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String item : base) {
            if (item.toLowerCase().startsWith(lower)) {
                out.add(item);
            }
        }
        return out;
    }

    private double parseAmount(String raw) {
        try {
            return Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}

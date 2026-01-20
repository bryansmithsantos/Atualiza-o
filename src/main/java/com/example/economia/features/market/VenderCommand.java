package com.example.economia.features.market;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.example.economia.features.messages.Messages;

public class VenderCommand implements CommandExecutor {

    private final MarketService marketService;

    public VenderCommand(MarketService marketService) {
        this.marketService = marketService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        if (args.length < 1) {
            Messages.error(player, "Use: /vender <preço>");
            Messages.info(player, "Segure o item que deseja vender na mão.");
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            Messages.error(player, "Preço inválido! Use um número.");
            return true;
        }

        if (price <= 0) {
            Messages.error(player, "O preço deve ser maior que zero.");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null || hand.getType().isAir()) {
            Messages.error(player, "Você não está segurando nenhum item!");
            return true;
        }

        // Adicionar ao mercado
        marketService.addListing(player.getUniqueId(), hand.getType(), hand.getAmount(), price);
        marketService.save();

        // Remover item da mão
        player.getInventory().setItemInMainHand(null);

        // Feedback bonito
        Messages.box(player, "ITEM LISTADO!",
                "📦 " + hand.getAmount() + "x " + formatMaterial(hand.getType().name()),
                "💰 Preço: $" + String.format("%.2f", price),
                "",
                "Seu item está no Mercado!",
                "Use /mercado para ver.");

        return true;
    }

    private String formatMaterial(String name) {
        return name.toLowerCase().replace("_", " ");
    }
}

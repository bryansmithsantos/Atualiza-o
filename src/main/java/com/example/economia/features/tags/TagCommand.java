package com.example.economia.features.tags;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.economia.features.messages.Messages;

public class TagCommand implements CommandExecutor {

    private final TagService tagService;

    public TagCommand(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        if (!player.hasPermission("blinded.admin")) {
            Messages.error(player, "Sem permissão.");
            return true;
        }

        if (args.length < 2) {
            Messages.info(player, "Uso: /tag <player> <tag>");
            Messages.info(player, "Tags: VIP, MVP, ELITE, LEGEND, ADMIN, MOD, HELPER, BUILDER, YOUTUBER");
            Messages.info(player, "Use /tag <player> limpar para remover");
            return true;
        }

        Player target = player.getServer().getPlayer(args[0]);
        if (target == null) {
            Messages.error(player, "Jogador não encontrado.");
            return true;
        }

        String tagName = args[1].toUpperCase();

        if (tagName.equals("LIMPAR") || tagName.equals("CLEAR") || tagName.equals("REMOVE")) {
            tagService.removeTag(target.getUniqueId());
            Messages.success(player, "Tag removida de " + target.getName());
            return true;
        }

        String tag = switch (tagName) {
            case "VIP" -> TagService.TAG_VIP;
            case "MVP" -> TagService.TAG_MVP;
            case "ELITE" -> TagService.TAG_ELITE;
            case "LEGEND", "LENDA" -> TagService.TAG_LEGEND;
            case "OWNER", "DONO" -> TagService.TAG_OWNER;
            case "ADMIN" -> TagService.TAG_ADMIN;
            case "MOD" -> TagService.TAG_MOD;
            case "HELPER" -> TagService.TAG_HELPER;
            case "BUILDER" -> TagService.TAG_BUILDER;
            case "YOUTUBER", "YT" -> TagService.TAG_YOUTUBER;
            default -> "§7[" + tagName + "]";
        };

        tagService.setTag(target.getUniqueId(), tag);
        Messages.success(player, "Tag " + tag + " §adefinida para " + target.getName());
        Messages.success(target, "Você recebeu a tag " + tag);

        return true;
    }
}

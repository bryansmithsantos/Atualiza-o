package com.example.economia.features.dungeon;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.economia.features.messages.Messages;

public class DungeonCommand implements CommandExecutor {

    private final DungeonService dungeonService;

    public DungeonCommand(DungeonService dungeonService) {
        this.dungeonService = dungeonService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("entrar")) {
            // Check if natural dungeon is available
            DungeonSession natural = dungeonService.getNaturalDungeon();
            if (natural != null) {
                dungeonService.joinNaturalDungeon(player);
                return true;
            }

            if (args.length < 2) {
                player.sendMessage("§cNenhuma dungeon natural disponível no momento.");
                player.sendMessage("§7Aguarde o spawn automático ou especifique um nível:");
                showDifficulties(player);
                return true;
            }

            String target = args[1].toLowerCase();

            // Try to find difficulty
            DungeonDifficulty difficulty = parseDifficulty(target);
            if (difficulty != null) {
                dungeonService.startDungeon(player, difficulty);
                return true;
            }

            // Try to find player
            Player owner = player.getServer().getPlayer(args[1]);
            if (owner != null) {
                dungeonService.joinDungeon(player, owner);
                return true;
            }

            Messages.error(player, "Dificuldade ou jogador não encontrado.");
            showDifficulties(player);
            return true;
        }

        if (sub.equals("sair")) {
            DungeonSession session = dungeonService.getSession(player);
            if (session == null) {
                Messages.error(player, "Você não está em uma dungeon.");
                return true;
            }
            session.defeat();
            dungeonService.endSession(session);
            Messages.info(player, "Você saiu da dungeon.");
            return true;
        }

        if (sub.equals("lista") || sub.equals("ativos")) {
            showActiveDungeons(player);
            return true;
        }

        showHelp(player);
        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("§8§m                                    ");
        player.sendMessage("§6§l⚔ DUNGEONS §8- §7Comandos");
        player.sendMessage("");
        player.sendMessage("§e/dungeon entrar §8- §7Entrar na dungeon ativa");
        player.sendMessage("§e/dungeon entrar <nivel> §8- §7Criar dungeon privada");
        player.sendMessage("§e/dungeon entrar <jogador> §8- §7Ajudar alguém");
        player.sendMessage("§e/dungeon sair §8- §7Sair da dungeon atual");
        player.sendMessage("§e/dungeon lista §8- §7Ver dungeons ativas");
        player.sendMessage("");
        player.sendMessage("§a⚡ §7Dungeons aparecem a cada §e5 minutos§7!");
        player.sendMessage("§8§m                                    ");
    }

    private void showDifficulties(Player player) {
        player.sendMessage("§8§m                                    ");
        player.sendMessage("§6§l⚔ DUNGEONS §8- §7Níveis Disponíveis");
        player.sendMessage("");

        for (DungeonDifficulty d : DungeonDifficulty.values()) {
            String name = d.name().toLowerCase().replace("_", "");
            player.sendMessage(d.getColor() + "§l" + d.getName());
            player.sendMessage("  §7Entrada: §e$" + formatMoney(d.getEntryCost()) +
                    " §8| §7Recompensa: §a$" + formatMoney(d.getMaxReward()));
            player.sendMessage("  §7Ondas: §f" + d.getWaves() + " §8| §7Boss: §c" + d.getBossHP() + " HP");
            player.sendMessage("  §7Comando: §e/dungeon entrar " + name);
            player.sendMessage("");
        }

        player.sendMessage("§8§m                                    ");
    }

    private void showActiveDungeons(Player player) {
        player.sendMessage("§8§m                                    ");
        player.sendMessage("§6§l⚔ DUNGEONS ATIVAS");
        player.sendMessage("");

        // Natural dungeon
        DungeonSession natural = dungeonService.getNaturalDungeon();
        if (natural != null) {
            player.sendMessage(
                    "§a⚡ DUNGEON NATURAL: " + natural.getDifficulty().getColor() + natural.getDifficulty().getName());
            player.sendMessage("  §7Use §e/dungeon entrar §7para participar!");
            player.sendMessage("");
        }

        var sessions = dungeonService.getActiveSessions();
        if (sessions.isEmpty() && natural == null) {
            player.sendMessage("§7Nenhuma dungeon ativa no momento.");
            player.sendMessage("§7Aguarde 5 minutos para a próxima spawn!");
        } else {
            for (var entry : sessions.entrySet()) {
                DungeonSession session = entry.getValue();
                if (session.getOwner() != null && entry.getKey().equals(session.getOwner())) {
                    Player owner = player.getServer().getPlayer(entry.getKey());
                    if (owner != null) {
                        player.sendMessage(session.getDifficulty().getColor() + session.getDifficulty().getName() +
                                " §8- §7Dono: §f" + owner.getName() +
                                " §8| §7Onda: §e" + session.getCurrentWave() + "/"
                                + session.getDifficulty().getWaves());
                        player.sendMessage("  §7Para ajudar: §e/dungeon entrar " + owner.getName());
                    }
                }
            }
        }

        player.sendMessage("§8§m                                    ");
    }

    private DungeonDifficulty parseDifficulty(String input) {
        return switch (input.toLowerCase()) {
            case "nivel1", "nivel_1", "1", "cripta" -> DungeonDifficulty.NIVEL_1;
            case "nivel2", "nivel_2", "2", "caverna" -> DungeonDifficulty.NIVEL_2;
            case "nivel3", "nivel_3", "3", "fortaleza" -> DungeonDifficulty.NIVEL_3;
            case "nivel4", "nivel_4", "4", "abismo" -> DungeonDifficulty.NIVEL_4;
            case "nivel5", "nivel_5", "5", "portal", "caos" -> DungeonDifficulty.NIVEL_5;
            case "nivel6", "nivel_6", "6", "vazio" -> DungeonDifficulty.NIVEL_6;
            case "nivel7", "nivel_7", "7", "esquecida" -> DungeonDifficulty.NIVEL_7;
            case "nivel8", "nivel_8", "8", "pesadelo" -> DungeonDifficulty.NIVEL_8;
            case "nivel9", "nivel_9", "9", "soberano" -> DungeonDifficulty.NIVEL_9;
            case "nivel10", "nivel_10", "10", "fim" -> DungeonDifficulty.NIVEL_10;
            default -> null;
        };
    }

    private String formatMoney(double amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.0fK", amount / 1000);
        }
        return String.format("%.0f", amount);
    }
}

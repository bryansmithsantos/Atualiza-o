package com.example.economia.features.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.messages.Messages;
import com.example.economia.features.missions.MissionsService;

public class VendaCommand implements CommandExecutor {

    private final EconomyService economyService;
    private final MissionsService missionsService;

    public VendaCommand(EconomyService economyService, MissionsService missionsService) {
        this.economyService = economyService;
        this.missionsService = missionsService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            Messages.error(player, "Segure um item na mão para vender.");
            return true;
        }

        // Calculate price based on item type
        double pricePerUnit = getPrice(hand.getType());
        if (pricePerUnit <= 0) {
            Messages.error(player, "Este item não pode ser vendido.");
            return true;
        }

        int quantity = hand.getAmount();
        double totalPrice = pricePerUnit * quantity;

        // Remove item from hand
        player.getInventory().setItemInMainHand(null);

        // Add balance
        economyService.addBalance(player.getUniqueId(), totalPrice);
        missionsService.recordSell(player.getUniqueId(), quantity);
        missionsService.recordEarn(player.getUniqueId(), totalPrice);

        Messages.success(player, "Você vendeu §e" + quantity + "x " + formatName(hand.getType()) +
                " §apor §6" + economyService.getCurrencySymbol() + String.format("%.2f", totalPrice));

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);

        return true;
    }

    private double getPrice(Material mat) {
        String name = mat.name();

        // Logs = $5
        if (name.endsWith("_LOG") || name.endsWith("_WOOD"))
            return 5.0;

        // Planks = $2
        if (name.endsWith("_PLANKS"))
            return 2.0;

        // Ores (raw)
        if (name.equals("RAW_IRON"))
            return 15.0;
        if (name.equals("RAW_GOLD"))
            return 25.0;
        if (name.equals("RAW_COPPER"))
            return 8.0;
        if (name.equals("DIAMOND"))
            return 100.0;
        if (name.equals("EMERALD"))
            return 80.0;
        if (name.equals("COAL"))
            return 3.0;
        if (name.equals("REDSTONE"))
            return 4.0;
        if (name.equals("LAPIS_LAZULI"))
            return 5.0;
        if (name.equals("AMETHYST_SHARD"))
            return 10.0;
        if (name.equals("ANCIENT_DEBRIS"))
            return 500.0;
        if (name.equals("NETHERITE_SCRAP"))
            return 250.0;
        if (name.equals("NETHERITE_INGOT"))
            return 1000.0;

        // Ingots
        if (name.equals("IRON_INGOT"))
            return 20.0;
        if (name.equals("GOLD_INGOT"))
            return 35.0;
        if (name.equals("COPPER_INGOT"))
            return 12.0;

        // Blocks
        if (name.equals("IRON_BLOCK"))
            return 180.0;
        if (name.equals("GOLD_BLOCK"))
            return 315.0;
        if (name.equals("DIAMOND_BLOCK"))
            return 900.0;
        if (name.equals("EMERALD_BLOCK"))
            return 720.0;
        if (name.equals("COAL_BLOCK"))
            return 27.0;
        if (name.equals("REDSTONE_BLOCK"))
            return 36.0;
        if (name.equals("LAPIS_BLOCK"))
            return 45.0;

        // Stone types
        if (name.equals("COBBLESTONE"))
            return 1.0;
        if (name.equals("STONE"))
            return 2.0;
        if (name.equals("DEEPSLATE"))
            return 3.0;
        if (name.equals("COBBLED_DEEPSLATE"))
            return 2.0;

        // Food
        if (name.equals("WHEAT"))
            return 2.0;
        if (name.equals("CARROT"))
            return 2.0;
        if (name.equals("POTATO"))
            return 2.0;
        if (name.equals("BEETROOT"))
            return 2.0;
        if (name.equals("MELON_SLICE"))
            return 1.0;
        if (name.equals("PUMPKIN"))
            return 5.0;
        if (name.equals("SUGAR_CANE"))
            return 3.0;
        if (name.equals("CACTUS"))
            return 2.0;
        if (name.equals("BAMBOO"))
            return 1.0;

        // Animals
        if (name.equals("BEEF") || name.equals("COOKED_BEEF"))
            return 5.0;
        if (name.equals("PORKCHOP") || name.equals("COOKED_PORKCHOP"))
            return 5.0;
        if (name.equals("MUTTON") || name.equals("COOKED_MUTTON"))
            return 4.0;
        if (name.equals("CHICKEN") || name.equals("COOKED_CHICKEN"))
            return 3.0;
        if (name.equals("RABBIT") || name.equals("COOKED_RABBIT"))
            return 3.0;
        if (name.equals("COD") || name.equals("COOKED_COD"))
            return 3.0;
        if (name.equals("SALMON") || name.equals("COOKED_SALMON"))
            return 4.0;
        if (name.equals("LEATHER"))
            return 8.0;
        if (name.equals("FEATHER"))
            return 2.0;
        if (name.equals("EGG"))
            return 1.0;
        if (name.equals("BONE"))
            return 2.0;
        if (name.equals("STRING"))
            return 3.0;
        if (name.equals("ROTTEN_FLESH"))
            return 1.0;
        if (name.equals("SPIDER_EYE"))
            return 4.0;
        if (name.equals("GUNPOWDER"))
            return 5.0;
        if (name.equals("ENDER_PEARL"))
            return 25.0;
        if (name.equals("BLAZE_ROD"))
            return 30.0;
        if (name.equals("GHAST_TEAR"))
            return 50.0;
        if (name.equals("SLIME_BALL"))
            return 8.0;

        // Flowers/Dyes
        if (name.endsWith("_DYE"))
            return 2.0;
        if (name.endsWith("_FLOWER") || name.endsWith("_TULIP") || name.endsWith("_ORCHID"))
            return 1.0;

        // Misc
        if (name.equals("STICK"))
            return 0.5;
        if (name.equals("FLINT"))
            return 2.0;
        if (name.equals("CLAY_BALL"))
            return 2.0;
        if (name.equals("BRICK"))
            return 3.0;
        if (name.equals("PAPER"))
            return 1.0;
        if (name.equals("BOOK"))
            return 5.0;
        if (name.equals("INK_SAC") || name.equals("GLOW_INK_SAC"))
            return 4.0;
        if (name.equals("GLOWSTONE_DUST"))
            return 5.0;
        if (name.equals("NETHER_WART"))
            return 3.0;

        // Default: anything else = $1
        return 1.0;
    }

    private String formatName(Material mat) {
        String name = mat.name().replace("_", " ").toLowerCase();
        // Capitalize first letter of each word
        StringBuilder result = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}

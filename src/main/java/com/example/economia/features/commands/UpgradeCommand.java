package com.example.economia.features.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.economia.features.messages.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class UpgradeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            Messages.error(player, "Segure um item na mão para evoluir.");
            return true;
        }

        // Check if item is upgradeable
        if (!isUpgradeable(hand.getType())) {
            Messages.error(player, "Este item não pode ser evoluído.");
            return true;
        }

        // Calculate upgrade level and XP cost
        int currentLevel = getUpgradeLevel(hand);
        int nextLevel = currentLevel + 1;
        int xpCost = getXpCost(nextLevel);

        if (player.getTotalExperience() < xpCost) {
            Messages.error(player, "XP insuficiente! Você precisa de " + xpCost + " XP.");
            return true;
        }

        // Remove XP
        player.giveExp(-xpCost);

        // Apply upgrade
        applyUpgrade(hand, nextLevel);

        Messages.success(player, "Item evoluído para §6Nível " + nextLevel + "§a! (-" + xpCost + " XP)");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

        return true;
    }

    private boolean isUpgradeable(Material mat) {
        String name = mat.name();
        // Weapons
        if (name.endsWith("_SWORD") || name.endsWith("_AXE"))
            return true;
        // Tools
        if (name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE"))
            return true;
        // Armor
        if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS"))
            return true;
        // Bow/Crossbow
        if (mat == Material.BOW || mat == Material.CROSSBOW || mat == Material.TRIDENT)
            return true;
        return false;
    }

    private int getUpgradeLevel(ItemStack item) {
        if (item.getItemMeta() == null || item.getItemMeta().lore() == null)
            return 0;
        for (Component line : item.getItemMeta().lore()) {
            String text = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                    .serialize(line);
            if (text.startsWith("Nível: ")) {
                try {
                    return Integer.parseInt(text.replace("Nível: ", "").trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private int getXpCost(int level) {
        // Exponential cost: 10, 25, 50, 100, 200, 400...
        return (int) (10 * Math.pow(2, level - 1));
    }

    private void applyUpgrade(ItemStack item, int level) {
        ItemMeta meta = item.getItemMeta();

        // Update lore with level
        List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();

        // Remove old level line if exists
        lore.removeIf(line -> {
            String text = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                    .serialize(line);
            return text.startsWith("Nível:");
        });

        // Add new level
        lore.add(0, Component.text("Nível: " + level, NamedTextColor.GOLD));
        lore.add(1, Component.text("★".repeat(Math.min(level, 10)), NamedTextColor.YELLOW));
        meta.lore(lore);

        // Apply enchantment boosts based on item type
        String name = item.getType().name();

        if (name.endsWith("_SWORD")) {
            addOrIncreaseEnchant(meta, Enchantment.SHARPNESS, level);
            if (level >= 3)
                addOrIncreaseEnchant(meta, Enchantment.FIRE_ASPECT, (level - 2) / 2);
            if (level >= 5)
                addOrIncreaseEnchant(meta, Enchantment.LOOTING, (level - 4) / 2);
        } else if (name.endsWith("_AXE")) {
            addOrIncreaseEnchant(meta, Enchantment.EFFICIENCY, level);
            addOrIncreaseEnchant(meta, Enchantment.SHARPNESS, level / 2);
        } else if (name.endsWith("_PICKAXE")) {
            addOrIncreaseEnchant(meta, Enchantment.EFFICIENCY, level);
            if (level >= 3)
                addOrIncreaseEnchant(meta, Enchantment.FORTUNE, (level - 2) / 2);
        } else if (name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
            addOrIncreaseEnchant(meta, Enchantment.EFFICIENCY, level);
        } else if (name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
                name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")) {
            addOrIncreaseEnchant(meta, Enchantment.PROTECTION, level / 2 + 1);
            addOrIncreaseEnchant(meta, Enchantment.UNBREAKING, level / 3 + 1);
            if (name.endsWith("_BOOTS") && level >= 3) {
                addOrIncreaseEnchant(meta, Enchantment.FEATHER_FALLING, (level - 2) / 2);
            }
        } else if (item.getType() == Material.BOW) {
            addOrIncreaseEnchant(meta, Enchantment.POWER, level);
            if (level >= 5)
                addOrIncreaseEnchant(meta, Enchantment.FLAME, 1);
        } else if (item.getType() == Material.CROSSBOW) {
            addOrIncreaseEnchant(meta, Enchantment.QUICK_CHARGE, Math.min(level, 3));
            if (level >= 3)
                addOrIncreaseEnchant(meta, Enchantment.PIERCING, (level - 2));
        } else if (item.getType() == Material.TRIDENT) {
            addOrIncreaseEnchant(meta, Enchantment.LOYALTY, Math.min(level, 3));
            if (level >= 3)
                addOrIncreaseEnchant(meta, Enchantment.RIPTIDE, Math.min((level - 2), 3));
        }

        // Always add unbreaking
        addOrIncreaseEnchant(meta, Enchantment.UNBREAKING, level / 2 + 1);

        item.setItemMeta(meta);
    }

    private void addOrIncreaseEnchant(ItemMeta meta, Enchantment enchant, int level) {
        if (level <= 0)
            return;
        int current = meta.getEnchantLevel(enchant);
        meta.addEnchant(enchant, Math.max(current, level), true);
    }
}

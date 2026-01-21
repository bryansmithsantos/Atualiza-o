package com.example.economia.features.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.economia.features.messages.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;

public class EvolveCommand implements CommandExecutor {

    // Diamond -> Netherite mappings
    private static final Map<Material, Material> EVOLUTION_MAP = new HashMap<>();
    static {
        EVOLUTION_MAP.put(Material.DIAMOND_SWORD, Material.NETHERITE_SWORD);
        EVOLUTION_MAP.put(Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE);
        EVOLUTION_MAP.put(Material.DIAMOND_AXE, Material.NETHERITE_AXE);
        EVOLUTION_MAP.put(Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL);
        EVOLUTION_MAP.put(Material.DIAMOND_HOE, Material.NETHERITE_HOE);
        EVOLUTION_MAP.put(Material.DIAMOND_HELMET, Material.NETHERITE_HELMET);
        EVOLUTION_MAP.put(Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE);
        EVOLUTION_MAP.put(Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS);
        EVOLUTION_MAP.put(Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS);
    }

    private static final int XP_COST = 1; // 1 level de XP

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            Messages.error(player, "Segure um item de diamante na mão para evoluir.");
            return true;
        }

        Material evolved = EVOLUTION_MAP.get(hand.getType());
        if (evolved == null) {
            Messages.error(player, "Este item não pode evoluir para Netherite.");
            Messages.info(player, "Itens válidos: Espada, Picareta, Machado, Pá, Enxada, Armaduras de Diamante.");
            return true;
        }

        if (player.getLevel() < XP_COST) {
            Messages.error(player, "Você precisa de pelo menos " + XP_COST + " nível de XP.");
            return true;
        }

        // Remove XP level
        player.setLevel(player.getLevel() - XP_COST);

        // Preserve enchantments and meta
        ItemMeta oldMeta = hand.getItemMeta();

        // Create new item
        ItemStack newItem = new ItemStack(evolved);
        ItemMeta newMeta = newItem.getItemMeta();

        // Copy enchantments
        if (oldMeta != null) {
            oldMeta.getEnchants().forEach((ench, level) -> newMeta.addEnchant(ench, level, true));

            // Copy display name if custom
            if (oldMeta.hasDisplayName()) {
                newMeta.displayName(oldMeta.displayName());
            }

            // Copy lore
            if (oldMeta.hasLore()) {
                newMeta.lore(oldMeta.lore());
            }
        }

        // Add evolution tag
        java.util.List<Component> lore = newMeta.lore() != null ? new java.util.ArrayList<>(newMeta.lore())
                : new java.util.ArrayList<>();
        lore.add(Component.text("✦ Evoluído para Netherite ✦", NamedTextColor.DARK_PURPLE));
        newMeta.lore(lore);

        newItem.setItemMeta(newMeta);

        // Replace item
        player.getInventory().setItemInMainHand(newItem);

        Messages.success(player, "Item evoluído para §5Netherite§a! (-" + XP_COST + " nível de XP)");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.5f);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);

        // Particle effect
        player.getWorld().spawnParticle(org.bukkit.Particle.DRAGON_BREATH,
                player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.02);

        return true;
    }
}

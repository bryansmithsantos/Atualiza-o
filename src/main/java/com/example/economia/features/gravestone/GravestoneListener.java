package com.example.economia.features.gravestone;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.example.economia.features.messages.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GravestoneListener implements Listener {

    private final Plugin plugin;
    private final Map<Location, UUID> gravestones = new HashMap<>();

    public GravestoneListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (event.getDrops().isEmpty())
            return;

        // Find safe location for chest
        Location deathLoc = player.getLocation();
        Block block = findSafeBlock(deathLoc);

        if (block == null) {
            // Can't place gravestone, items drop normally
            return;
        }

        // Clear drops (we're putting them in chest)
        java.util.List<ItemStack> items = new java.util.ArrayList<>(event.getDrops());
        event.getDrops().clear();

        // Place chest
        block.setType(Material.CHEST);

        if (block.getState() instanceof Chest chest) {
            Inventory inv = chest.getInventory();

            // Add items to chest
            for (ItemStack item : items) {
                if (item != null && item.getType() != Material.AIR) {
                    inv.addItem(item);
                }
            }

            // Track gravestone
            gravestones.put(block.getLocation(), player.getUniqueId());

            // Schedule removal after 10 minutes
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (gravestones.containsKey(block.getLocation())) {
                    removeGravestone(block.getLocation());
                }
            }, 20 * 60 * 10); // 10 minutes

            // Notify player
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            event.deathMessage(Component.text("")
                    .append(Component.text("☠ ", NamedTextColor.RED))
                    .append(Component.text(player.getName(), NamedTextColor.GOLD))
                    .append(Component.text(" morreu! Lápide em ", NamedTextColor.GRAY))
                    .append(Component.text(x + ", " + y + ", " + z, NamedTextColor.YELLOW)));

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    Messages.warning(player, "Seus itens estão em um baú (lápide) em: §e" + x + ", " + y + ", " + z);
                    Messages.info(player, "§7A lápide desaparece em 10 minutos!");
                }
            }, 20L);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();

        if (gravestones.containsKey(loc)) {
            UUID owner = gravestones.get(loc);
            Player player = event.getPlayer();

            if (!player.getUniqueId().equals(owner) && !player.hasPermission("blinded.admin")) {
                event.setCancelled(true);
                Messages.error(player, "Esta lápide pertence a outro jogador!");
                return;
            }

            // Remove from tracking
            gravestones.remove(loc);
        }
    }

    private Block findSafeBlock(Location loc) {
        // Try current location
        Block block = loc.getBlock();
        if (isSafe(block))
            return block;

        // Try above
        for (int y = 0; y < 5; y++) {
            block = loc.clone().add(0, y, 0).getBlock();
            if (isSafe(block))
                return block;
        }

        // Try around
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                block = loc.clone().add(x, 0, z).getBlock();
                if (isSafe(block))
                    return block;
            }
        }

        return null;
    }

    private boolean isSafe(Block block) {
        return block.getType() == Material.AIR ||
                block.getType() == Material.CAVE_AIR ||
                block.getType() == Material.WATER;
    }

    private void removeGravestone(Location loc) {
        Block block = loc.getBlock();
        if (block.getType() == Material.CHEST) {
            if (block.getState() instanceof Chest chest) {
                // Drop remaining items
                for (ItemStack item : chest.getInventory().getContents()) {
                    if (item != null && item.getType() != Material.AIR) {
                        block.getWorld().dropItemNaturally(loc, item);
                    }
                }
                chest.getInventory().clear();
            }
            block.setType(Material.AIR);
        }
        gravestones.remove(loc);
    }
}

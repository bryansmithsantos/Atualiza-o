package com.example.economia.features.gui;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class GuiUtils {

    private GuiUtils() {
    }

    public static ItemStack item(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, NamedTextColor.WHITE));
            if (lore.length > 0) {
                meta.lore(Arrays.stream(lore)
                        .map(line -> Component.text(line, NamedTextColor.GRAY))
                        .toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack head(java.util.UUID owner, String name, String... lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(org.bukkit.Bukkit.getOfflinePlayer(owner));
            meta.displayName(Component.text(name, NamedTextColor.WHITE));
            if (lore.length > 0) {
                meta.lore(Arrays.stream(lore)
                        .map(line -> Component.text(line, NamedTextColor.GRAY))
                        .toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}

package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.messages.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class ServerShopGui implements Listener {

    private final EconomyService economyService;

    public ServerShopGui(EconomyService economyService) {
        this.economyService = economyService;
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GuiTitles.SERVER_SHOP);

        // === DECORAÇÃO ===
        int[] border = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53 };
        for (int slot : border) {
            inv.setItem(slot, GuiUtils.item(Material.BLUE_STAINED_GLASS_PANE, " ", " "));
        }

        // === ARMAS ESPECIAIS ===
        inv.setItem(10, createShopItem(Material.NETHERITE_SWORD, "§c§lEspada do Caos", 500000,
                "§7Espada ultra poderosa!", "§4+15 de Dano", "§7Sharpness V, Fire II"));
        inv.setItem(11, createShopItem(Material.NETHERITE_AXE, "§6§lMachado Destruidor", 350000,
                "§7Derruba tudo!", "§c+13 de Dano", "§7Efficiency V, Sharpness IV"));
        inv.setItem(12, createShopItem(Material.BOW, "§e§lArco Celestial", 200000,
                "§7Flechas infinitas!", "§b+∞ Flechas", "§7Infinity, Power V"));
        inv.setItem(13, createShopItem(Material.TRIDENT, "§b§lTridente de Poseidon", 750000,
                "§7Domínio dos mares!", "§9Riptide III", "§7Loyalty III, Channeling"));

        // === ARMADURAS ESPECIAIS ===
        inv.setItem(19, createShopItem(Material.NETHERITE_HELMET, "§5§lCapacete Divino", 400000,
                "§7Proteção máxima!", "§d+8 Proteção", "§7Protection IV, Unbreaking III"));
        inv.setItem(20, createShopItem(Material.NETHERITE_CHESTPLATE, "§5§lPeitoral Divino", 600000,
                "§7Invencibilidade!", "§d+12 Proteção", "§7Protection IV, Thorns III"));
        inv.setItem(21, createShopItem(Material.NETHERITE_LEGGINGS, "§5§lCalças Divinas", 450000,
                "§7Velocidade extra!", "§d+10 Proteção", "§7Protection IV, Swift Sneak III"));
        inv.setItem(22, createShopItem(Material.NETHERITE_BOOTS, "§5§lBotas Divinas", 350000,
                "§7Queda suave!", "§d+6 Proteção", "§7Feather Falling IV, Depth Strider III"));

        // === FERRAMENTAS ESPECIAIS ===
        inv.setItem(28, createShopItem(Material.NETHERITE_PICKAXE, "§a§lPicareta Infinita", 450000,
                "§7Mineração turbo!", "§bEfficiency VI", "§7Fortune III, Unbreaking III"));
        inv.setItem(29, createShopItem(Material.ELYTRA, "§f§lÉlitras Perfeitas", 1000000,
                "§7Voe para sempre!", "§dMending", "§7Unbreaking III"));
        inv.setItem(30, createShopItem(Material.SHULKER_BOX, "§d§lMochila Expansiva", 250000,
                "§7Guarde mais itens!", "§727 slots extras"));

        // === ITENS ESPECIAIS ===
        inv.setItem(32, createShopItem(Material.ENCHANTED_GOLDEN_APPLE, "§6§lMaçã Encantada x5", 100000,
                "§7Regeneração extrema!", "§a5 unidades"));
        inv.setItem(33, createShopItem(Material.TOTEM_OF_UNDYING, "§e§lTotem da Vida x3", 150000,
                "§7Segunda chance!", "§a3 unidades"));
        inv.setItem(34, createShopItem(Material.BEACON, "§b§lBeacon Completo", 800000,
                "§7Com todos os efeitos!", "§7Pronto para usar"));

        // === VOLTAR ===
        inv.setItem(49, GuiUtils.item(Material.BARRIER, "§cVoltar", "§7Retornar ao painel"));

        player.openInventory(inv);
    }

    private static ItemStack createShopItem(Material mat, String name, int price, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));

        List<Component> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        loreList.add(Component.empty());
        loreList.add(Component.text("§a§lPREÇO: §f$" + String.format("%,d", price)).decoration(TextDecoration.ITALIC,
                false));
        loreList.add(Component.text("§eClique para comprar!").decoration(TextDecoration.ITALIC, false));
        meta.lore(loreList);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals(GuiTitles.SERVER_SHOP_TEXT))
            return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.BLUE_STAINED_GLASS_PANE)
            return;
        if (item.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Extrair preço do lore
        if (item.getItemMeta() == null || item.getItemMeta().lore() == null)
            return;

        int price = extractPrice(item);
        if (price <= 0)
            return;

        double balance = economyService.getBalance(player.getUniqueId());
        if (balance < price) {
            Messages.error(player, "Saldo insuficiente! Você precisa de $" + String.format("%,d", price));
            return;
        }

        // Comprar
        economyService.removeBalance(player.getUniqueId(), price);

        // Dar item real (com enchants)
        ItemStack bought = createRealItem(item.getType());
        player.getInventory().addItem(bought);

        Messages.success(player, "Você comprou "
                + PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName()) + "!");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
    }

    private int extractPrice(ItemStack item) {
        if (item.getItemMeta() == null || item.getItemMeta().lore() == null)
            return 0;
        for (Component line : item.getItemMeta().lore()) {
            String text = PlainTextComponentSerializer.plainText().serialize(line);
            if (text.contains("PREÇO:")) {
                String priceStr = text.replaceAll("[^0-9]", "");
                try {
                    return Integer.parseInt(priceStr);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private ItemStack createRealItem(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        switch (mat) {
            case NETHERITE_SWORD -> {
                meta.addEnchant(Enchantment.SHARPNESS, 5, true);
                meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
                meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                meta.displayName(Component.text("§c§lEspada do Caos"));
            }
            case NETHERITE_AXE -> {
                meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
                meta.addEnchant(Enchantment.SHARPNESS, 4, true);
                meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                meta.displayName(Component.text("§6§lMachado Destruidor"));
            }
            case BOW -> {
                meta.addEnchant(Enchantment.INFINITY, 1, true);
                meta.addEnchant(Enchantment.POWER, 5, true);
                meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                meta.displayName(Component.text("§e§lArco Celestial"));
            }
            case TRIDENT -> {
                meta.addEnchant(Enchantment.RIPTIDE, 3, true);
                meta.addEnchant(Enchantment.LOYALTY, 3, true);
                meta.addEnchant(Enchantment.CHANNELING, 1, true);
                meta.displayName(Component.text("§b§lTridente de Poseidon"));
            }
            case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> {
                meta.addEnchant(Enchantment.PROTECTION, 4, true);
                meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                meta.addEnchant(Enchantment.THORNS, 3, true);
                meta.displayName(Component.text("§5§lArmadura Divina"));
            }
            case NETHERITE_PICKAXE -> {
                meta.addEnchant(Enchantment.EFFICIENCY, 6, true);
                meta.addEnchant(Enchantment.FORTUNE, 3, true);
                meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                meta.displayName(Component.text("§a§lPicareta Infinita"));
            }
            case ELYTRA -> {
                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.addEnchant(Enchantment.UNBREAKING, 3, true);
                meta.displayName(Component.text("§f§lÉlitras Perfeitas"));
            }
            case ENCHANTED_GOLDEN_APPLE -> {
                item.setAmount(5);
            }
            case TOTEM_OF_UNDYING -> {
                item.setAmount(3);
            }
            default -> {
            }
        }

        item.setItemMeta(meta);
        return item;
    }
}

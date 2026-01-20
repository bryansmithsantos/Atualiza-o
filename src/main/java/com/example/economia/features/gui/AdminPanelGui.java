package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.example.economia.features.messages.Messages;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class AdminPanelGui implements Listener {

    public AdminPanelGui() {
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GuiTitles.ADMIN_PANEL);

        // === DECORAÇÃO ===
        // Bordas com vidro colorido
        int[] border = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53 };
        for (int slot : border) {
            inv.setItem(slot, GuiUtils.item(Material.PURPLE_STAINED_GLASS_PANE, " ", " "));
        }
        // Preenchimento interno
        int[] inner = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39,
                40, 41, 42, 43 };
        for (int slot : inner) {
            inv.setItem(slot, GuiUtils.item(Material.GRAY_STAINED_GLASS_PANE, " ", " "));
        }

        // === SERVER CONTROLS (Row 2) ===
        inv.setItem(10, GuiUtils.item(Material.REDSTONE_BLOCK, "§4⚠ Reiniciar Servidor",
                "§7Reinicia com contagem regressiva.", "§c(Cuidado!)"));
        inv.setItem(11, GuiUtils.item(Material.COMMAND_BLOCK, "§eAnúncio Global",
                "§7Broadcast para todos.", "§aUse: /anuncio"));
        inv.setItem(12, GuiUtils.item(Material.CLOCK, "§bMudar Horário",
                "§7Dia/Noite rapidamente.", "§eLBotões: §fDia §8| §7Noite"));

        // === PLAYER MANAGEMENT (Row 2 cont.) ===
        inv.setItem(14, GuiUtils.item(Material.PLAYER_HEAD, "§6Gerenciar Players",
                "§7Ban, Kick, Mute, Warn", "§eUse comandos Essentials"));
        inv.setItem(15, GuiUtils.item(Material.GOLDEN_APPLE, "§bVanish",
                "§7Ficar invisível.", "§aClique para alternar"));
        inv.setItem(16, GuiUtils.item(Material.ENDER_PEARL, "§dTeleport",
                "§7TP para jogador.", "§eUse: /tp <player>"));

        // === ECONOMY & WORLD (Row 3) ===
        inv.setItem(19, GuiUtils.item(Material.EMERALD, "§aDar Dinheiro",
                "§7/money give <player> <qtd>"));
        inv.setItem(20, GuiUtils.item(Material.DIAMOND, "§bDar Item",
                "§7/give <player> <item> <qtd>"));
        inv.setItem(21, GuiUtils.item(Material.SUNFLOWER, "§eMudar Clima",
                "§7Sol / Chuva / Tempestade"));

        // === MARKET & MISC (Row 3 cont.) ===
        inv.setItem(23, GuiUtils.item(Material.CHEST, "§dMercado Admin",
                "§7Adicionar itens ao mercado.", "§7Segure o item + clique"));
        inv.setItem(24, GuiUtils.item(Material.EXPERIENCE_BOTTLE, "§5Dar XP",
                "§7/xp give <player> <amount>"));
        inv.setItem(25, GuiUtils.item(Material.IRON_SWORD, "§cGameMode",
                "§7Alternar Survival/Creative"));

        // === UTILITIES (Row 4) ===
        inv.setItem(28, GuiUtils.item(Material.COMPASS, "§fWorld Spawn",
                "§7Teleportar ao spawn."));
        inv.setItem(29, GuiUtils.item(Material.TOTEM_OF_UNDYING, "§aHeal/Feed",
                "§7Curar e alimentar a si."));
        inv.setItem(30, GuiUtils.item(Material.ELYTRA, "§bFly",
                "§7Ativar/Desativar voo."));

        // === CLOSE ===
        inv.setItem(49, GuiUtils.item(Material.BARRIER, "§cFechar", "§7Sair do painel"));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals(GuiTitles.ADMIN_PANEL_TEXT))
            return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        ItemStack item = event.getCurrentItem();
        if (item == null)
            return;

        switch (item.getType()) {
            case REDSTONE_BLOCK -> {
                player.closeInventory();
                Messages.warning(player, "Reiniciando servidor em 5 segundos...");
                Bukkit.broadcast(net.kyori.adventure.text.Component.text("§c⚠ Servidor reiniciando em 5 segundos!"));
                Bukkit.getScheduler().runTaskLater(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
                        () -> Bukkit.spigot().restart(), 100L);
            }
            case COMMAND_BLOCK -> {
                player.closeInventory();
                Messages.info(player, "Use: /anuncio <mensagem>");
            }
            case CLOCK -> {
                long time = player.getWorld().getTime();
                if (time < 12000) {
                    player.getWorld().setTime(18000); // Noite
                    Messages.success(player, "Hora definida para §7Noite.");
                } else {
                    player.getWorld().setTime(6000); // Dia
                    Messages.success(player, "Hora definida para §eDia.");
                }
            }
            case PLAYER_HEAD -> Messages.info(player, "Use: /ban, /kick, /mute, /warn");
            case GOLDEN_APPLE -> {
                if (player.isInvisible()) {
                    player.setInvisible(false);
                    Messages.success(player, "Vanish §cDESATIVADO.");
                } else {
                    player.setInvisible(true);
                    Messages.success(player, "Vanish §aATIVADO.");
                }
            }
            case ENDER_PEARL -> {
                player.closeInventory();
                Messages.info(player, "Use: /tp <player> ou /tphere <player>");
            }
            case EMERALD -> {
                player.closeInventory();
                Messages.info(player, "Use: /money give <player> <valor>");
            }
            case DIAMOND -> {
                player.closeInventory();
                Messages.info(player, "Use: /give <player> <item> <quantidade>");
            }
            case SUNFLOWER -> {
                if (player.getWorld().hasStorm()) {
                    player.getWorld().setStorm(false);
                    player.getWorld().setThundering(false);
                    Messages.success(player, "Clima: §eSol.");
                } else {
                    player.getWorld().setStorm(true);
                    Messages.success(player, "Clima: §bChuva.");
                }
            }
            case CHEST -> {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    Messages.error(player, "Segure um item para adicionar ao mercado.");
                } else {
                    Messages.success(player, "Item adicionado ao mercado admin! (Feature em dev)");
                }
            }
            case EXPERIENCE_BOTTLE -> {
                player.closeInventory();
                Messages.info(player, "Use: /xp give <player> <amount>");
            }
            case IRON_SWORD -> {
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    player.setGameMode(GameMode.CREATIVE);
                    Messages.success(player, "Modo: §bCreative.");
                } else {
                    player.setGameMode(GameMode.SURVIVAL);
                    Messages.success(player, "Modo: §aSurvival.");
                }
            }
            case COMPASS -> {
                player.teleport(player.getWorld().getSpawnLocation());
                Messages.success(player, "Teleportado ao §fSpawn§a.");
            }
            case TOTEM_OF_UNDYING -> {
                player.setHealth(20);
                player.setFoodLevel(20);
                Messages.success(player, "Vida e fome restauradas!");
            }
            case ELYTRA -> {
                if (player.getAllowFlight()) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    Messages.success(player, "Voo §cDESATIVADO.");
                } else {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    Messages.success(player, "Voo §aATIVADO.");
                }
            }
            case BARRIER -> player.closeInventory();
            default -> {
            }
        }
    }
}

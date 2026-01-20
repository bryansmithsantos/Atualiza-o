package com.example.economia.features.gui;

import org.bukkit.Bukkit;
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

        // Decor
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, GuiUtils.item(Material.BLACK_STAINED_GLASS_PANE, " ", " "));
        }

        // --- SECTION: SERVER ---
        inv.setItem(10, GuiUtils.item(Material.REDSTONE_BLOCK, "§4Reiniciar Servidor",
                "§7Reinicia o servidor com aviso prévio.", "§c⚠ Cuidado!"));

        inv.setItem(11, GuiUtils.item(Material.COMMAND_BLOCK, "§eAnúncio Global",
                "§7Enviar mensagem para todos.", "§7(Clique para usar)"));

        // --- SECTION: PLAYER MANAGEMENT ---
        inv.setItem(13, GuiUtils.item(Material.PLAYER_HEAD, "§6Gerenciar Jogadores",
                "§7Banir, Kickar, Mutar", "§e(Em breve menu dedicado)"));

        inv.setItem(14, GuiUtils.item(Material.GOLDEN_APPLE, "§bVanish (Invisibilidade)",
                "§7Ficar invisível para outros jogadores.", "§aClique para alternar"));

        // --- SECTION: ECONOMY ---
        inv.setItem(15, GuiUtils.item(Material.EMERALD, "§aDar Dinheiro",
                "§7Adicionar saldo a um jogador.", "§eUse: /money give <player> <amount>"));

        // --- SECTION: MARKET ---
        inv.setItem(16, GuiUtils.item(Material.CHEST, "§dMercado Admin",
                "§7Enviar itens para o mercado global.", "§7Segure o item e clique."));

        // Back
        inv.setItem(49, GuiUtils.item(Material.BARRIER, "§cFechar", "§7Sair do painel admin"));

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
            case REDSTONE_BLOCK:
                player.closeInventory();
                Messages.warning(player, "Reiniciando servidor em 5 segundos...");
                Bukkit.broadcast(net.kyori.adventure.text.Component.text("§c⚠ Servidor reiniciando em 5 segundos!"));
                Bukkit.getScheduler().runTaskLater(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
                        () -> {
                            Bukkit.spigot().restart();
                        }, 100L);
                break;

            case COMMAND_BLOCK:
                player.closeInventory();
                Messages.info(player, "Use: /say <mensagem> ou /broadcast <mensagem>");
                break;

            case PLAYER_HEAD:
                Messages.info(player, "Use comandos: /ban, /kick, /mute (Essentials/LiteBans)");
                break;

            case GOLDEN_APPLE: // Vanish logic
                if (player.isInvisible()) {
                    player.setInvisible(false);
                    Messages.success(player, "Vanish DESATIVADO.");
                } else {
                    player.setInvisible(true);
                    Messages.success(player, "Vanish ATIVADO.");
                }
                break;

            case EMERALD:
                player.closeInventory();
                Messages.info(player, "Use: /money give <player> <quantia>");
                break;

            case CHEST:
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    Messages.error(player, "Segure um item para enviar ao mercado.");
                } else {
                    Messages.success(player, "Item enviado ao mercado admin (Feature em dev)");
                    // Logica de mercado precisa ser chamada aqui se existir metodo "addItemAdmin"
                }
                break;

            case BARRIER:
                player.closeInventory();
                break;
            default:
                break;
        }
    }
}

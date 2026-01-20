package com.example.economia.features.clans;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.example.economia.features.gui.GuiTitles;
import com.example.economia.features.gui.GuiUtils;
import com.example.economia.features.messages.Messages;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ClanGui implements Listener {

    private final ClanService clanService;

    public ClanGui(ClanService clanService) {
        this.clanService = clanService;
    }

    public static void open(Player player, ClanService clanService) {
        Clan clan = clanService.getClan(player.getUniqueId());

        if (clan == null) {
            openNoClan(player);
        } else {
            openDashboard(player, clan);
        }
    }

    private static void openNoClan(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.CLAN);

        // Decoração
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, GuiUtils.item(Material.BLACK_STAINED_GLASS_PANE, " ", " "));
        }

        inv.setItem(13, GuiUtils.item(Material.EMERALD_BLOCK, "§aCriar Clan",
                "§7Custo: §a$" + String.format("%,.0f", ClanService.CREATION_PRICE),
                "§7Requisitos: Tag (3-5 letras)",
                "",
                "§eClique para criar!"));

        inv.setItem(26, GuiUtils.item(Material.BARRIER, "§cVoltar", "§7Voltar ao menu"));

        player.openInventory(inv);
    }

    private static void openDashboard(Player player, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 54, GuiTitles.CLAN);

        // Info Header (Using WHITE_BANNER instead of non-existent BANNER_PATTERN)
        inv.setItem(4, GuiUtils.item(Material.WHITE_BANNER, "§6Clan " + clan.getName(),
                "§7Tag: §f" + clan.getTag(),
                "§7Dono: §f" + getName(clan.getOwner()),
                "§7Membros: §f" + clan.getMembers().size(),
                "§7KDR: §e" + clan.getKdr(),
                "§7Banco: §a$" + String.format("%,.2f", clan.getBankBalance())));

        // Settings
        inv.setItem(0, GuiUtils.item(Material.IRON_SWORD, "§cFogo Amigo: " + (clan.isFriendlyFire() ? "§aON" : "§cOFF"),
                "§7Clique para alternar"));

        inv.setItem(8, GuiUtils.item(Material.GOLD_BLOCK, "§eDepositar", "§7Clique para depositar $1000"));

        // Members list starts at 18
        int slot = 18;
        for (UUID memberId : clan.getMembers()) {
            if (slot > 53)
                break;
            String role = memberId.equals(clan.getOwner()) ? "§c[DONO]"
                    : (clan.isModerator(memberId) ? "§6[MOD]" : "§7[MEMBRO]");
            inv.setItem(slot, GuiUtils.head(memberId, "§e" + getName(memberId),
                    role,
                    "",
                    "§7Clique para gerenciar"));
            slot++;
        }

        // Actions
        if (clan.getOwner().equals(player.getUniqueId())) {
            inv.setItem(53, GuiUtils.item(Material.TNT, "§cDesfazer Clan", "§7Clique para apagar permanentemente"));
        } else {
            inv.setItem(53, GuiUtils.item(Material.RED_BED, "§cSair do Clan", "§7Clique para sair"));
        }

        inv.setItem(49, GuiUtils.item(Material.BARRIER, "§cVoltar", "§7Voltar ao menu"));

        // Convite
        if (clan.isModerator(player.getUniqueId())) {
            inv.setItem(6,
                    GuiUtils.item(Material.PAPER, "§aConvidar Jogador", "§7Clique para convidar alguém próximo"));
        }

        player.openInventory(inv);
    }

    private static String getName(UUID uuid) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        return op.getName() != null ? op.getName() : "Unknown";
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals(GuiTitles.CLAN_TEXT))
            return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        ItemStack item = event.getCurrentItem();
        if (item == null)
            return;

        Clan clan = clanService.getClan(player.getUniqueId());

        if (clan == null) {
            // No clan menu
            if (item.getType() == Material.EMERALD_BLOCK) {
                player.closeInventory();
                Messages.info(player, "Digite a TAG do clan no chat (3-5 letras):");
                Messages.warning(player, "Sistema de chat em desenvolvimento. Use: /clan create <tag> <nome>");
            } else if (item.getType() == Material.BARRIER) {
                player.closeInventory();
                player.performCommand("painel");
            }
            return;
        }

        // Dashboard menu
        if (item.getType() == Material.IRON_SWORD) {
            if (clan.getOwner().equals(player.getUniqueId())) {
                clan.setFriendlyFire(!clan.isFriendlyFire());
                clanService.save();
                openDashboard(player, clan);
            }
        } else if (item.getType() == Material.GOLD_BLOCK) {
            clanService.deposit(player, 1000);
            openDashboard(player, clan);
        } else if (item.getType() == Material.TNT) {
            clanService.disbandClan(player);
            player.closeInventory();
        } else if (item.getType() == Material.RED_BED) {
            clanService.leave(player);
            player.closeInventory();
        } else if (item.getType() == Material.BARRIER) {
            player.closeInventory();
            player.performCommand("painel");
        }
    }
}

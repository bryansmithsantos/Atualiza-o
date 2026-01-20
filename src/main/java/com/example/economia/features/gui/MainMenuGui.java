package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.jobs.JobsService;
import com.example.economia.features.jobs.WorkService;

public final class MainMenuGui {

    private MainMenuGui() {
    }

    public static void open(Player player, EconomyService economyService, JobsService jobsService,
            WorkService workService) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.MAIN);
        String balance = economyService.formatBalance(player.getUniqueId());
        String jobName = jobsService.getCurrentJob(player).displayName();

        inv.setItem(10, GuiUtils.item(Material.EMERALD, "Banco", "Saldo: " + balance));
        inv.setItem(11, GuiUtils.item(Material.CHEST, "Cofre", "Guardar moedas"));
        inv.setItem(12, GuiUtils.item(Material.IRON_PICKAXE, "Trabalhar", "Emprego atual: " + jobName));
        inv.setItem(13, GuiUtils.item(Material.BOOK, "Empregos", "Escolha seu trabalho"));
        inv.setItem(14, GuiUtils.item(Material.CHEST_MINECART, "Loja", "Comprar e vender"));
        inv.setItem(15, GuiUtils.item(Material.COMPASS, "Mercado", "Trocas entre jogadores"));
        inv.setItem(16, GuiUtils.item(Material.NAME_TAG, "Empresa", "Criar e gerenciar"));
        inv.setItem(19, GuiUtils.item(Material.ENCHANTED_BOOK, "Upgrades", "Melhorias"));
        inv.setItem(20, GuiUtils.item(Material.WRITABLE_BOOK, "Missões", "Diárias"));
        inv.setItem(21, GuiUtils.item(Material.PAPER, "Multas", "Pagar pendências"));
        inv.setItem(22, GuiUtils.item(Material.CLOCK, "Rankings", "Top jogadores"));
        inv.setItem(23, GuiUtils.item(Material.BOOKSHELF, "Histórico", "Movimentações"));
        inv.setItem(24, GuiUtils.item(Material.FURNACE, "Geradores", "Comprar geradores de minérios"));
        inv.setItem(26, GuiUtils.item(Material.BARRIER, "Sair", "Fechar painel"));

        player.openInventory(inv);
    }
}

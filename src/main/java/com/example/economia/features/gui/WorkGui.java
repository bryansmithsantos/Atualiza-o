package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.jobs.Job;
import com.example.economia.features.jobs.JobsService;
import com.example.economia.features.jobs.WorkService;

public final class WorkGui {

    private WorkGui() {
    }

    public static void open(Player player, JobsService jobsService, WorkService workService) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.WORK);
        Job job = jobsService.getCurrentJob(player);
        double reward = workService.getReward(player, job);
        long cooldown = workService.getCooldownSeconds(player);

        inv.setItem(11, GuiUtils.item(Material.BOOK, "Emprego", job.displayName(), "Pagamento base: " + reward));
        inv.setItem(13, GuiUtils.item(Material.GOLD_INGOT, "Trabalhar agora", "Cooldown: " + cooldown + "s"));
        inv.setItem(15, GuiUtils.item(Material.CHEST, "Empregos", "Trocar trabalho"));
        inv.setItem(22, GuiUtils.item(Material.BARRIER, "Voltar", "Painel"));

        player.openInventory(inv);
    }
}

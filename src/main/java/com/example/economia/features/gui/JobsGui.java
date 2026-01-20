package com.example.economia.features.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.example.economia.features.jobs.Job;
import com.example.economia.features.jobs.JobsService;
import com.example.economia.features.licenses.LicenseService;
import com.example.economia.features.economy.EconomyService;

public final class JobsGui {

    private JobsGui() {
    }

    public static void open(Player player, JobsService jobsService, LicenseService licenseService, EconomyService economyService) {
        Inventory inv = Bukkit.createInventory(null, 54, GuiTitles.JOBS);
        String current = jobsService.getCurrentJob(player).id();
        int slot = 0;
        for (Job job : jobsService.getJobs()) {
            boolean locked = job.licenseId() != null && !licenseService.has(player.getUniqueId(), job.licenseId());
            String level = "Nível: " + jobsService.getLevel(player, job.id());
            String pay = "Pagamento base: " + job.basePay();
            String license = locked ? "Licença: " + economyService.getCurrencySymbol() + job.licensePrice() : "Licença: OK";
            ItemStack item = GuiUtils.item(job.icon(), job.displayName(), level, pay, license);
            if (job.id().equals(current)) {
                item = GuiUtils.item(job.icon(), job.displayName(), "Selecionado", level, pay, license);
            }
            inv.setItem(slot++, item);
            if (slot >= inv.getSize()) {
                break;
            }
        }
        player.openInventory(inv);
    }
}

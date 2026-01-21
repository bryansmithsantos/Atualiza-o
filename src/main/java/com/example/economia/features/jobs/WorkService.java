package com.example.economia.features.jobs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.example.economia.features.company.Company;
import com.example.economia.features.company.CompanyService;
import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.logs.LogService;
import com.example.economia.features.missions.MissionsService;
import com.example.economia.features.tax.TaxService;
import com.example.economia.features.upgrades.UpgradeType;
import com.example.economia.features.upgrades.UpgradesService;

public final class WorkService {

    private final Plugin plugin;
    private final JobsService jobsService;
    private final EconomyService economyService;
    private final UpgradesService upgradesService;
    private final MissionsService missionsService;
    private final TaxService taxService;
    private final CompanyService companyService;
    private final LogService logService;
    private final Map<UUID, Long> lastWork = new HashMap<>();

    public WorkService(Plugin plugin, JobsService jobsService, EconomyService economyService,
            UpgradesService upgradesService, MissionsService missionsService, TaxService taxService,
            CompanyService companyService, LogService logService) {
        this.plugin = plugin;
        this.jobsService = jobsService;
        this.economyService = economyService;
        this.upgradesService = upgradesService;
        this.missionsService = missionsService;
        this.taxService = taxService;
        this.companyService = companyService;
        this.logService = logService;
    }

    public WorkResult tryWork(Player player) {
        long now = System.currentTimeMillis();
        long cooldownMillis = getCooldownSeconds(player) * 1000L;
        long last = lastWork.getOrDefault(player.getUniqueId(), 0L);
        long elapsed = now - last;
        if (elapsed < cooldownMillis) {
            long remaining = (cooldownMillis - elapsed + 999) / 1000;
            return new WorkResult(false, remaining, 0.0);
        }
        Job job = jobsService.getCurrentJob(player);
        double reward = getReward(player, job);
        double netReward = taxService.applyTax(reward, "tax.work");
        Company company = companyService.getCompany(player.getUniqueId());
        double share = getCompanyShare();
        double companyCut = company != null ? netReward * share : 0.0;
        double playerCut = netReward - companyCut;
        if (company != null && companyCut > 0) {
            companyService.depositVault(company, companyCut);
        }
        economyService.addBalance(player.getUniqueId(), playerCut);
        missionsService.recordWork(player.getUniqueId());
        missionsService.recordEarn(player.getUniqueId(), playerCut);
        jobsService.addXp(player, job.id(), getXpPerWork());
        logService.add(player.getUniqueId(),
                "Trabalho +" + economyService.getCurrencySymbol() + String.format("%.2f", playerCut));
        lastWork.put(player.getUniqueId(), now);
        return new WorkResult(true, 0, reward);
    }

    public double getReward(Player player, Job job) {
        double base = job.basePay() * getMultiplier();
        double salaryBonus = upgradesService.getMultiplier(player.getUniqueId(), UpgradeType.SALARY); // Now returns
                                                                                                      // flat bonus
        double bonusMult = upgradesService.getMultiplier(player.getUniqueId(), UpgradeType.JOB_BONUS);
        // Base + salary bonus, then apply job bonus multiplier
        return Math.round((base + salaryBonus) * bonusMult * 100.0) / 100.0;
    }

    public long getCooldownSeconds(Player player) {
        long base = plugin.getConfig().getLong("jobs.cooldown-seconds", 120L);
        double mult = upgradesService.getMultiplier(player.getUniqueId(), UpgradeType.COOLDOWN);
        return Math.max(10L, (long) Math.ceil(base / mult));
    }

    public double getMultiplier() {
        return plugin.getConfig().getDouble("jobs.multiplier", 1.0);
    }

    public int getXpPerWork() {
        return plugin.getConfig().getInt("jobs.xp-per-work", 10);
    }

    public double getCompanyShare() {
        return plugin.getConfig().getDouble("company.share", 0.1);
    }

    public String getCurrencySymbol() {
        return economyService.getCurrencySymbol();
    }
}

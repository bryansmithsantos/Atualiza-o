package com.example.economia.features.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.economia.features.auth.AuthService;
import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.gui.AuthGui;
import com.example.economia.features.gui.MainMenuGui;
import com.example.economia.features.jobs.JobsService;
import com.example.economia.features.jobs.WorkService;

public final class MenuCommand implements CommandExecutor {

    private final AuthService authService;
    private final EconomyService economyService;
    private final JobsService jobsService;
    private final WorkService workService;

    public MenuCommand(AuthService authService, EconomyService economyService, JobsService jobsService, WorkService workService) {
        this.authService = authService;
        this.economyService = economyService;
        this.jobsService = jobsService;
        this.workService = workService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }
        if (!authService.isLoggedIn(player.getUniqueId())) {
            if (authService.isRegistered(player.getUniqueId())) {
                AuthGui.openLogin(player);
            } else {
                AuthGui.openRegister(player);
            }
            return true;
        }
        MainMenuGui.open(player, economyService, jobsService, workService);
        return true;
    }
}

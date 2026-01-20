package com.example.economia;

import org.bukkit.plugin.java.JavaPlugin;

import com.example.economia.features.auth.AuthInputListener;
import com.example.economia.features.auth.AuthListener;
import com.example.economia.features.auth.AuthService;
import com.example.economia.features.bank.BankService;
import com.example.economia.features.bedrock.BedrockSupport;
import com.example.economia.features.commands.AuthCommand;
import com.example.economia.features.commands.FinesCommand;
import com.example.economia.features.commands.MenuCommand;
import com.example.economia.features.commands.ShopCommand;
import com.example.economia.features.company.CompanyService;
import com.example.economia.features.economy.EconomyListener;
import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.economy.MoneyCommand;
import com.example.economia.features.fines.FinesService;
import com.example.economia.features.gui.GuiListener;
import com.example.economia.features.jobs.JobsService;
import com.example.economia.features.jobs.WorkService;
import com.example.economia.features.licenses.LicenseService;
import com.example.economia.features.logs.LogService;
import com.example.economia.features.market.MarketService;
import com.example.economia.features.missions.MissionsService;
import com.example.economia.features.scoreboard.ScoreboardService;
import com.example.economia.features.shop.ShopService;
import com.example.economia.features.tax.TaxService;
import com.example.economia.features.update.UpdateCommand;
import com.example.economia.features.update.UpdateService;
import com.example.economia.features.upgrades.UpgradesService;
import com.example.economia.features.vault.VaultService;
import com.example.economia.features.generators.GeneratorService;
import com.example.economia.features.generators.GeneratorListener;
import com.example.economia.features.generators.GeneratorsGui;
import com.example.economia.features.clans.ClanService;
import com.example.economia.features.clans.ClanListener;
import com.example.economia.features.clans.ClanGui;
import com.example.economia.features.gui.AdminPanelGui;
import com.example.economia.features.homes.HomeService;
import com.example.economia.features.homes.HomeCommand;

public final class EconomiaPlugin extends JavaPlugin {

    private ScoreboardService scoreboardService;
    private BedrockSupport bedrockSupport;
    private EconomyService economyService;
    private AuthService authService;
    private JobsService jobsService;
    private WorkService workService;
    private ShopService shopService;
    private BankService bankService;
    private VaultService vaultService;
    private UpgradesService upgradesService;
    private MissionsService missionsService;
    private LicenseService licenseService;
    private MarketService marketService;
    private CompanyService companyService;
    private FinesService finesService;
    private LogService logService;
    private TaxService taxService;
    private UpdateService updateService;
    private GeneratorService generatorService;
    private ClanService clanService;
    private HomeService homeService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        bedrockSupport = new BedrockSupport(this);
        economyService = new EconomyService(this);
        economyService.load();
        authService = new AuthService(this);
        authService.load();
        jobsService = new JobsService(this);
        jobsService.load();
        bankService = new BankService(this);
        bankService.load();
        vaultService = new VaultService(this);
        vaultService.load();
        upgradesService = new UpgradesService(this);
        upgradesService.load();
        missionsService = new MissionsService(this);
        missionsService.load();
        licenseService = new LicenseService(this);
        licenseService.load();
        marketService = new MarketService(this);
        marketService.load();
        companyService = new CompanyService(this);
        companyService.load();
        finesService = new FinesService(this);
        finesService.load();
        logService = new LogService(this);
        logService.load();
        taxService = new TaxService(this);
        updateService = new UpdateService(this);
        generatorService = new GeneratorService(this);
        generatorService.load();
        clanService = new ClanService(this, economyService);
        clanService.load();
        homeService = new HomeService(this);
        homeService.load();

        workService = new WorkService(this, jobsService, economyService, upgradesService, missionsService, taxService,
                companyService, logService);
        shopService = new ShopService(this);
        shopService.load();

        scoreboardService = new ScoreboardService(this, bedrockSupport, economyService);
        scoreboardService.start();
        getServer().getPluginManager().registerEvents(scoreboardService.getPlayerListener(), this);
        getServer().getPluginManager().registerEvents(new EconomyListener(economyService), this);
        getServer().getPluginManager().registerEvents(new AuthListener(authService), this);
        getServer().getPluginManager()
                .registerEvents(new AuthInputListener(authService, economyService, jobsService, workService), this);
        getServer().getPluginManager()
                .registerEvents(new GuiListener(authService, economyService, jobsService, workService, shopService,
                        bankService, vaultService, upgradesService, missionsService, licenseService, marketService,
                        companyService, finesService,
                        logService, taxService, clanService), this);
        getServer().getPluginManager().registerEvents(
                new com.example.economia.features.treefeller.TreeFellerListener(jobsService, economyService),
                this);
        getServer().getPluginManager().registerEvents(new GeneratorListener(generatorService, this), this);
        getServer().getPluginManager().registerEvents(new GeneratorsGui(generatorService, economyService), this);
        getServer().getPluginManager().registerEvents(new ClanListener(clanService), this);
        getServer().getPluginManager().registerEvents(new ClanGui(clanService), this);
        getServer().getPluginManager().registerEvents(new AdminPanelGui(), this);
        if (getCommand("money") != null) {
            MoneyCommand moneyCommand = new MoneyCommand(economyService, authService, taxService, logService);
            getCommand("money").setExecutor(moneyCommand);
            getCommand("money").setTabCompleter(moneyCommand);
        }
        if (getCommand("painel") != null) {
            getCommand("painel").setExecutor(new MenuCommand(authService, economyService, jobsService, workService));
        }
        if (getCommand("login") != null) {
            getCommand("login").setExecutor(new AuthCommand(authService, false));
        }
        if (getCommand("register") != null) {
            getCommand("register").setExecutor(new AuthCommand(authService, true));
        }
        if (getCommand("loja") != null) {
            getCommand("loja")
                    .setExecutor(new ShopCommand(authService, shopService, economyService.getCurrencySymbol()));
        }
        if (getCommand("multa") != null) {
            getCommand("multa").setExecutor(new FinesCommand(finesService, logService));
        }
        if (getCommand("blinded") != null) {
            getCommand("blinded").setExecutor(new UpdateCommand(updateService));
        }
        if (getCommand("painel_admin") != null) {
            getCommand("painel_admin").setExecutor((sender, command, label, args) -> {
                if (sender instanceof org.bukkit.entity.Player player && player.hasPermission("blinded.admin")) {
                    AdminPanelGui.open(player);
                } else {
                    sender.sendMessage("§cSem permissão.");
                }
                return true;
            });
        }
        if (getCommand("ping") != null) {
            getCommand("ping").setExecutor(new com.example.economia.features.commands.PingCommand());
        }
        if (getCommand("anuncio") != null) {
            getCommand("anuncio").setExecutor(new com.example.economia.features.commands.AnuncioCommand());
        }

        if (getCommand("sethome") != null) {
            HomeCommand homeCmd = new HomeCommand(homeService);
            getCommand("sethome").setExecutor(homeCmd);
            getCommand("home").setExecutor(homeCmd);
            getCommand("delhome").setExecutor(homeCmd);
            getCommand("homes").setExecutor(homeCmd);
        }
        getLogger().info("EconomiaPlugin habilitado.");
    }

    @Override
    public void onDisable() {
        if (scoreboardService != null) {
            scoreboardService.stop();
        }
        if (economyService != null) {
            economyService.save();
        }
        if (authService != null) {
            authService.save();
        }
        if (jobsService != null) {
            jobsService.save();
        }
        if (shopService != null) {
            shopService.save();
        }
        if (bankService != null) {
            bankService.save();
        }
        if (vaultService != null) {
            vaultService.save();
        }
        if (upgradesService != null) {
            upgradesService.save();
        }
        if (missionsService != null) {
            missionsService.save();
        }
        if (licenseService != null) {
            licenseService.save();
        }
        if (marketService != null) {
            marketService.save();
        }
        if (companyService != null) {
            companyService.save();
        }
        if (finesService != null) {
            finesService.save();
        }
        if (logService != null) {
            logService.save();
        }
        if (generatorService != null) {
            generatorService.save();
        }
        if (clanService != null) {
            clanService.save();
        }
        if (homeService != null) {
            homeService.save();
        }
        getLogger().info("EconomiaPlugin desabilitado.");
    }
}

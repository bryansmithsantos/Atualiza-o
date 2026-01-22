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
import com.example.economia.features.tags.TagService;
import com.example.economia.features.tags.TagCommand;
import com.example.economia.features.gravestone.GravestoneListener;

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
                .registerEvents(new com.example.economia.features.missions.MissionsListener(missionsService), this);
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
        ClanListener clanListener = new ClanListener(clanService);
        getServer().getPluginManager().registerEvents(clanListener, this);
        getServer().getPluginManager().registerEvents(new ClanGui(clanService), this);
        getServer().getPluginManager().registerEvents(new AdminPanelGui(), this);
        getServer().getPluginManager().registerEvents(new GravestoneListener(this), this);

        // Tag Service
        TagService tagService = new TagService(this);
        tagService.load();
        clanListener.setTagService(tagService);
        clanListener.setPlugin(this); // For glow updates

        // Auto Announcements
        com.example.economia.features.announcements.AnnouncementService announcementService = new com.example.economia.features.announcements.AnnouncementService(
                this);
        announcementService.start();

        getServer().getPluginManager()
                .registerEvents(new com.example.economia.features.gui.ServerShopGui(economyService), this);
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
        if (getCommand("vender") != null) {
            getCommand("vender").setExecutor(new com.example.economia.features.market.VenderCommand(marketService));
        }
        if (getCommand("shopvip") != null) {
            getCommand("shopvip").setExecutor((sender, cmd, label, args) -> {
                if (sender instanceof org.bukkit.entity.Player player) {
                    com.example.economia.features.gui.ServerShopGui.open(player);
                }
                return true;
            });
        }
        if (getCommand("venda") != null) {
            getCommand("venda").setExecutor(
                    new com.example.economia.features.commands.VendaCommand(economyService, missionsService));
        }
        if (getCommand("upgrade") != null) {
            getCommand("upgrade").setExecutor(new com.example.economia.features.commands.UpgradeCommand());
        }
        if (getCommand("evoluir") != null) {
            getCommand("evoluir").setExecutor(new com.example.economia.features.commands.EvolveCommand());
        }
        if (getCommand("tag") != null) {
            getCommand("tag").setExecutor(new TagCommand(tagService));
        }

        // Dungeon System
        com.example.economia.features.dungeon.DungeonService dungeonService = new com.example.economia.features.dungeon.DungeonService(
                this, economyService);
        dungeonService.setClanService(clanService);
        dungeonService.startNaturalSpawnCycle();
        getServer().getPluginManager().registerEvents(
                new com.example.economia.features.dungeon.DungeonListener(dungeonService), this);
        if (getCommand("dungeon") != null) {
            getCommand("dungeon").setExecutor(new com.example.economia.features.dungeon.DungeonCommand(dungeonService));
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

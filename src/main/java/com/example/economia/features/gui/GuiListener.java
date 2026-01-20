package com.example.economia.features.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.example.economia.features.auth.AuthRequest;
import com.example.economia.features.auth.AuthService;
import com.example.economia.features.bank.BankService;
import com.example.economia.features.company.Company;
import com.example.economia.features.company.CompanyService;
import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.fines.FinesService;
import com.example.economia.features.jobs.Job;
import com.example.economia.features.jobs.JobsService;
import com.example.economia.features.jobs.WorkResult;
import com.example.economia.features.jobs.WorkService;
import com.example.economia.features.licenses.LicenseService;
import com.example.economia.features.logs.LogService;
import com.example.economia.features.market.MarketListing;
import com.example.economia.features.market.MarketService;
import com.example.economia.features.missions.Mission;
import com.example.economia.features.missions.MissionsService;
import com.example.economia.features.shop.ShopItem;
import com.example.economia.features.shop.ShopService;
import com.example.economia.features.tax.TaxService;
import com.example.economia.features.upgrades.UpgradeType;
import com.example.economia.features.upgrades.UpgradesService;
import com.example.economia.features.vault.VaultService;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class GuiListener implements Listener {

    private final AuthService authService;
    private final EconomyService economyService;
    private final JobsService jobsService;
    private final WorkService workService;
    private final ShopService shopService;
    private final BankService bankService;
    private final VaultService vaultService;
    private final UpgradesService upgradesService;
    private final MissionsService missionsService;
    private final LicenseService licenseService;
    private final MarketService marketService;
    private final CompanyService companyService;
    private final FinesService finesService;
    private final LogService logService;
    private final TaxService taxService;

    public GuiListener(AuthService authService, EconomyService economyService, JobsService jobsService, WorkService workService,
            ShopService shopService, BankService bankService, VaultService vaultService, UpgradesService upgradesService,
            MissionsService missionsService, LicenseService licenseService, MarketService marketService,
            CompanyService companyService, FinesService finesService, LogService logService, TaxService taxService) {
        this.authService = authService;
        this.economyService = economyService;
        this.jobsService = jobsService;
        this.workService = workService;
        this.shopService = shopService;
        this.bankService = bankService;
        this.vaultService = vaultService;
        this.upgradesService = upgradesService;
        this.missionsService = missionsService;
        this.licenseService = licenseService;
        this.marketService = marketService;
        this.companyService = companyService;
        this.finesService = finesService;
        this.logService = logService;
        this.taxService = taxService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals(GuiTitles.AUTH_TEXT) && !title.equals(GuiTitles.MAIN_TEXT)
            && !title.equals(GuiTitles.JOBS_TEXT) && !title.equals(GuiTitles.WORK_TEXT)
            && !title.equals(GuiTitles.SHOP_TEXT) && !title.equals(GuiTitles.BANK_TEXT)
            && !title.equals(GuiTitles.VAULT_TEXT) && !title.equals(GuiTitles.UPGRADES_TEXT)
            && !title.equals(GuiTitles.MISSIONS_TEXT) && !title.equals(GuiTitles.MARKET_TEXT)
            && !title.equals(GuiTitles.MARKET_PRICE_TEXT) && !title.equals(GuiTitles.COMPANY_TEXT)
            && !title.equals(GuiTitles.COMPANY_INVITE_TEXT) && !title.equals(GuiTitles.FINES_TEXT)
            && !title.equals(GuiTitles.LEADERBOARDS_TEXT) && !title.equals(GuiTitles.LOGS_TEXT)) {
            return;
        }
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (title.equals(GuiTitles.AUTH_TEXT)) {
            handleAuthClick(player, item);
            return;
        }
        if (!authService.isLoggedIn(player.getUniqueId())) {
            player.sendMessage("Você precisa fazer login.");
            return;
        }
        if (title.equals(GuiTitles.MAIN_TEXT)) {
            handleMainClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.JOBS_TEXT)) {
            handleJobsClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.WORK_TEXT)) {
            handleWorkClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.SHOP_TEXT)) {
            handleShopClick(player, item, event);
            return;
        }
        if (title.equals(GuiTitles.BANK_TEXT)) {
            handleBankClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.VAULT_TEXT)) {
            handleVaultClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.UPGRADES_TEXT)) {
            handleUpgradesClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.MISSIONS_TEXT)) {
            handleMissionsClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.MARKET_TEXT)) {
            handleMarketClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.MARKET_PRICE_TEXT)) {
            handleMarketPriceClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.COMPANY_TEXT)) {
            handleCompanyClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.COMPANY_INVITE_TEXT)) {
            handleCompanyInvite(player, item);
            return;
        }
        if (title.equals(GuiTitles.FINES_TEXT)) {
            handleFinesClick(player, item);
            return;
        }
        if (title.equals(GuiTitles.LEADERBOARDS_TEXT)) {
            MainMenuGui.open(player, economyService, jobsService, workService);
            return;
        }
        if (title.equals(GuiTitles.LOGS_TEXT)) {
            MainMenuGui.open(player, economyService, jobsService, workService);
        }
    }

    private void handleAuthClick(Player player, ItemStack item) {
        Material type = item.getType();
        if (type == Material.EMERALD) {
            authService.requestInput(player, AuthRequest.REGISTER);
            player.closeInventory();
            player.sendMessage("Digite sua senha no chat para registrar.");
        } else if (type == Material.PAPER) {
            authService.requestInput(player, AuthRequest.LOGIN);
            player.closeInventory();
            player.sendMessage("Digite sua senha no chat para login.");
        }
    }

    private void handleMainClick(Player player, ItemStack item) {
        Material type = item.getType();
        if (type == Material.IRON_PICKAXE) {
            WorkGui.open(player, jobsService, workService);
        } else if (type == Material.BOOK) {
            JobsGui.open(player, jobsService, licenseService, economyService);
        } else if (type == Material.EMERALD) {
            BankGui.open(player, economyService, bankService);
        } else if (type == Material.CHEST) {
            VaultGui.open(player, economyService, vaultService);
        } else if (type == Material.CHEST_MINECART) {
            ShopGui.open(player, shopService, economyService.getCurrencySymbol());
        } else if (type == Material.COMPASS) {
            MarketGui.open(player, marketService, economyService.getCurrencySymbol());
        } else if (type == Material.NAME_TAG) {
            CompanyGui.open(player, economyService, companyService);
        } else if (type == Material.ENCHANTED_BOOK) {
            UpgradesGui.open(player, economyService, upgradesService);
        } else if (type == Material.WRITABLE_BOOK) {
            MissionsGui.open(player, economyService, missionsService);
        } else if (type == Material.PAPER) {
            FinesGui.open(player, economyService, finesService);
        } else if (type == Material.CLOCK) {
            LeaderboardsGui.open(player, economyService, jobsService, missionsService);
        } else if (type == Material.BOOKSHELF) {
            LogsGui.open(player, logService);
        } else if (type == Material.BARRIER) {
            player.closeInventory();
        }
    }

    private void handleJobsClick(Player player, ItemStack item) {
        for (Job job : jobsService.getJobs()) {
            if (job.icon() == item.getType()) {
                if (job.licenseId() != null && !licenseService.has(player.getUniqueId(), job.licenseId())) {
                    if (economyService.removeBalance(player.getUniqueId(), job.licensePrice())) {
                        licenseService.grant(player.getUniqueId(), job.licenseId());
                        player.sendMessage("Licença comprada: " + job.displayName());
                        logService.add(player.getUniqueId(), "Licença comprada: " + job.displayName());
                    } else {
                        player.sendMessage("Saldo insuficiente para licença.");
                        return;
                    }
                }
                jobsService.setJob(player, job.id());
                player.sendMessage("Emprego atualizado para: " + job.displayName());
                JobsGui.open(player, jobsService, licenseService, economyService);
                return;
            }
        }
    }

    private void handleWorkClick(Player player, ItemStack item) {
        Material type = item.getType();
        if (type == Material.GOLD_INGOT) {
            WorkResult result = workService.tryWork(player);
            if (!result.success()) {
                player.sendMessage("Aguarde " + result.remainingSeconds() + "s para trabalhar novamente.");
                return;
            }
            player.sendMessage("Você recebeu " + workService.getCurrencySymbol() + String.format("%.2f", result.reward()) + ".");
            WorkGui.open(player, jobsService, workService);
            return;
        }
        if (type == Material.CHEST) {
            JobsGui.open(player, jobsService, licenseService, economyService);
            return;
        }
        if (type == Material.BARRIER) {
            MainMenuGui.open(player, economyService, jobsService, workService);
        }
    }

    private void handleShopClick(Player player, ItemStack item, InventoryClickEvent event) {
        Material type = item.getType();
        if (type == Material.BARRIER) {
            MainMenuGui.open(player, economyService, jobsService, workService);
            return;
        }
        ShopItem shopItem = shopService.getByMaterial(type);
        if (shopItem == null) {
            return;
        }
        boolean rightClick = event.getClick().isRightClick();
        if (rightClick) {
            if (shopItem.sellPrice() <= 0) {
                player.sendMessage("Este item não pode ser vendido.");
                return;
            }
            if (!player.getInventory().containsAtLeast(new ItemStack(type), 1)) {
                player.sendMessage("Você não possui este item.");
                return;
            }
            player.getInventory().removeItem(new ItemStack(type, 1));
            double net = taxService.applyTax(shopItem.sellPrice(), "tax.shop.sell");
            economyService.addBalance(player.getUniqueId(), net);
            missionsService.recordSell(player.getUniqueId(), 1);
            logService.add(player.getUniqueId(), "Venda loja +" + economyService.getCurrencySymbol() + String.format("%.2f", net));
            player.sendMessage("Você vendeu 1x " + shopItem.displayName() + " por "
                    + economyService.getCurrencySymbol() + String.format("%.2f", net));
            return;
        }
        if (shopItem.buyPrice() <= 0) {
            player.sendMessage("Este item não pode ser comprado.");
            return;
        }
        double price = taxService.applyTax(shopItem.buyPrice(), "tax.shop.buy");
        if (!economyService.removeBalance(player.getUniqueId(), price)) {
            player.sendMessage("Saldo insuficiente.");
            return;
        }
        player.getInventory().addItem(new ItemStack(type, 1));
        player.sendMessage("Você comprou 1x " + shopItem.displayName() + " por "
                + economyService.getCurrencySymbol() + String.format("%.2f", price));
        logService.add(player.getUniqueId(), "Compra loja -" + economyService.getCurrencySymbol() + String.format("%.2f", price));
    }

    private void handleBankClick(Player player, ItemStack item) {
        Material type = item.getType();
        if (type == Material.GOLD_INGOT) {
            if (!economyService.removeBalance(player.getUniqueId(), 50)) {
                player.sendMessage("Saldo insuficiente.");
                return;
            }
            if (!bankService.deposit(player.getUniqueId(), 50)) {
                economyService.addBalance(player.getUniqueId(), 50);
                player.sendMessage("Limite diário excedido.");
                return;
            }
            logService.add(player.getUniqueId(), "Depósito banco 50");
            BankGui.open(player, economyService, bankService);
        } else if (type == Material.GOLD_BLOCK) {
            if (!economyService.removeBalance(player.getUniqueId(), 200)) {
                player.sendMessage("Saldo insuficiente.");
                return;
            }
            if (!bankService.deposit(player.getUniqueId(), 200)) {
                economyService.addBalance(player.getUniqueId(), 200);
                player.sendMessage("Limite diário excedido.");
                return;
            }
            logService.add(player.getUniqueId(), "Depósito banco 200");
            BankGui.open(player, economyService, bankService);
        } else if (type == Material.GOLD_NUGGET) {
            if (!bankService.withdraw(player.getUniqueId(), 50)) {
                player.sendMessage("Saldo bancário insuficiente.");
                return;
            }
            economyService.addBalance(player.getUniqueId(), 50);
            logService.add(player.getUniqueId(), "Saque banco 50");
            BankGui.open(player, economyService, bankService);
        } else if (type == Material.BARRIER) {
            MainMenuGui.open(player, economyService, jobsService, workService);
        }
    }

    private void handleVaultClick(Player player, ItemStack item) {
        Material type = item.getType();
        if (type == Material.CHEST) {
            if (!economyService.removeBalance(player.getUniqueId(), 50)) {
                player.sendMessage("Saldo insuficiente.");
                return;
            }
            vaultService.deposit(player.getUniqueId(), 50);
            logService.add(player.getUniqueId(), "Guardar cofre 50");
            VaultGui.open(player, economyService, vaultService);
        } else if (type == Material.CHEST_MINECART) {
            if (!vaultService.withdraw(player.getUniqueId(), 50)) {
                player.sendMessage("Cofre insuficiente.");
                return;
            }
            economyService.addBalance(player.getUniqueId(), 50);
            logService.add(player.getUniqueId(), "Retirar cofre 50");
            VaultGui.open(player, economyService, vaultService);
        } else if (type == Material.BARRIER) {
            MainMenuGui.open(player, economyService, jobsService, workService);
        }
    }

    private void handleUpgradesClick(Player player, ItemStack item) {
        Material type = item.getType();
        UpgradeType upgradeType = null;
        if (type == Material.EMERALD) {
            upgradeType = UpgradeType.SALARY;
        } else if (type == Material.CLOCK) {
            upgradeType = UpgradeType.COOLDOWN;
        } else if (type == Material.ENCHANTED_BOOK) {
            upgradeType = UpgradeType.JOB_BONUS;
        } else if (type == Material.BARRIER) {
            MainMenuGui.open(player, economyService, jobsService, workService);
            return;
        }
        if (upgradeType == null) {
            return;
        }
        double cost = upgradesService.getCost(player.getUniqueId(), upgradeType);
        if (!economyService.removeBalance(player.getUniqueId(), cost)) {
            player.sendMessage("Saldo insuficiente.");
            return;
        }
        upgradesService.increase(player.getUniqueId(), upgradeType);
        logService.add(player.getUniqueId(), "Upgrade comprado: " + upgradeType.name());
        UpgradesGui.open(player, economyService, upgradesService);
    }

    private void handleMissionsClick(Player player, ItemStack item) {
        if (item.getType() == Material.BARRIER) {
            MainMenuGui.open(player, economyService, jobsService, workService);
            return;
        }
        String title = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
        for (Mission mission : missionsService.getMissions()) {
            if (mission.title().equalsIgnoreCase(title)) {
                if (!missionsService.claim(player.getUniqueId(), mission)) {
                    player.sendMessage("Missão incompleta.");
                    return;
                }
                economyService.addBalance(player.getUniqueId(), mission.reward());
                logService.add(player.getUniqueId(), "Missão concluída +" + economyService.getCurrencySymbol() + mission.reward());
                MissionsGui.open(player, economyService, missionsService);
                return;
            }
        }
    }

    private void handleMarketClick(Player player, ItemStack item) {
        Material type = item.getType();
        if (type == Material.PAPER) {
            MarketPriceGui.open(player, economyService.getCurrencySymbol());
            return;
        }
        if (type == Material.BARRIER) {
            MainMenuGui.open(player, economyService, jobsService, workService);
            return;
        }
        String listingId = null;
        if (item.getItemMeta() != null && item.getItemMeta().lore() != null) {
            for (net.kyori.adventure.text.Component line : item.getItemMeta().lore()) {
                String plain = PlainTextComponentSerializer.plainText().serialize(line);
                if (plain.startsWith("ID: ")) {
                    listingId = plain.substring(4);
                    break;
                }
            }
        }
        if (listingId == null) {
            return;
        }
        MarketListing listing = marketService.getListing(listingId);
        if (listing == null) {
            return;
        }
        double price = taxService.applyTax(listing.price(), "tax.market.buy");
        if (!economyService.removeBalance(player.getUniqueId(), price)) {
            player.sendMessage("Saldo insuficiente.");
            return;
        }
        player.getInventory().addItem(new ItemStack(listing.material(), listing.amount()));
        double sellerNet = taxService.applyTax(listing.price(), "tax.market.sell");
        economyService.addBalance(listing.seller(), sellerNet);
        marketService.removeListing(listing.id());
        logService.add(player.getUniqueId(), "Compra mercado -" + economyService.getCurrencySymbol() + price);
        logService.add(listing.seller(), "Venda mercado +" + economyService.getCurrencySymbol() + sellerNet);
        MarketGui.open(player, marketService, economyService.getCurrencySymbol());
    }

    private void handleMarketPriceClick(Player player, ItemStack item) {
        Material type = item.getType();
        if (type == Material.BARRIER) {
            MarketGui.open(player, marketService, economyService.getCurrencySymbol());
            return;
        }
        double price = 0;
        if (type == Material.GOLD_NUGGET) {
            price = 50;
        } else if (type == Material.GOLD_INGOT) {
            price = 200;
        } else if (type == Material.GOLD_BLOCK) {
            price = 500;
        }
        if (price <= 0) {
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            player.sendMessage("Segure um item na mão.");
            return;
        }
        marketService.addListing(player.getUniqueId(), hand.getType(), 1, price);
        hand.setAmount(hand.getAmount() - 1);
        logService.add(player.getUniqueId(), "Item listado no mercado: " + hand.getType().name());
        MarketGui.open(player, marketService, economyService.getCurrencySymbol());
    }

    private void handleCompanyClick(Player player, ItemStack item) {
        Material type = item.getType();
        if (type == Material.NAME_TAG) {
            Company company = companyService.createCompany(player.getUniqueId(), "Empresa de " + player.getName());
            if (company == null) {
                player.sendMessage("Você já possui empresa.");
                return;
            }
            player.sendMessage("Empresa criada: " + company.name());
            CompanyGui.open(player, economyService, companyService);
            return;
        }
        if (type == Material.CHEST) {
            Company company = companyService.getCompany(player.getUniqueId());
            if (company == null) {
                return;
            }
            if (!economyService.removeBalance(player.getUniqueId(), 100)) {
                player.sendMessage("Saldo insuficiente.");
                return;
            }
            companyService.depositVault(company, 100);
            logService.add(player.getUniqueId(), "Depósito empresa 100");
            CompanyGui.open(player, economyService, companyService);
            return;
        }
        if (type == Material.PLAYER_HEAD) {
            player.sendMessage("Membros: " + companyService.getCompany(player.getUniqueId()).members().size());
            return;
        }
        if (type == Material.PAPER) {
            player.sendMessage("Convites enviados para jogadores online.");
            for (Player online : player.getServer().getOnlinePlayers()) {
                if (!online.equals(player)) {
                    companyService.invite(player.getUniqueId(), online.getUniqueId());
                    CompanyInviteGui.open(online);
                }
            }
            return;
        }
        if (type == Material.BARRIER) {
            MainMenuGui.open(player, economyService, jobsService, workService);
        }
    }

    private void handleCompanyInvite(Player player, ItemStack item) {
        Material type = item.getType();
        if (type == Material.EMERALD) {
            if (companyService.acceptInvite(player.getUniqueId())) {
                player.sendMessage("Convite aceito.");
            } else {
                player.sendMessage("Sem convite.");
            }
            player.closeInventory();
            return;
        }
        if (type == Material.BARRIER) {
            player.closeInventory();
        }
    }

    private void handleFinesClick(Player player, ItemStack item) {
        if (item.getType() == Material.BARRIER) {
            MainMenuGui.open(player, economyService, jobsService, workService);
            return;
        }
        String fineId = null;
        if (item.getItemMeta() != null && item.getItemMeta().lore() != null) {
            for (net.kyori.adventure.text.Component line : item.getItemMeta().lore()) {
                String plain = PlainTextComponentSerializer.plainText().serialize(line);
                if (plain.startsWith("ID: ")) {
                    fineId = plain.substring(4);
                    break;
                }
            }
        }
        if (fineId == null) {
            return;
        }
        double payment = 50;
        if (!economyService.removeBalance(player.getUniqueId(), payment)) {
            player.sendMessage("Saldo insuficiente.");
            return;
        }
        if (!finesService.payFine(player.getUniqueId(), fineId, payment)) {
            player.sendMessage("Multa não encontrada.");
            economyService.addBalance(player.getUniqueId(), payment);
            return;
        }
        logService.add(player.getUniqueId(), "Parcela de multa -" + economyService.getCurrencySymbol() + payment);
        FinesGui.open(player, economyService, finesService);
    }
}

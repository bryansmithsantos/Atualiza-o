package com.example.economia.features.company;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class CompanyService {

    private final Plugin plugin;
    private final Map<String, Company> companies = new HashMap<>();
    private final Map<UUID, String> playerCompany = new HashMap<>();
    private final Map<UUID, String> invites = new HashMap<>();
    private File companyFile;
    private FileConfiguration companyConfig;

    public CompanyService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        companyFile = new File(plugin.getDataFolder(), "companies.yml");
        if (!companyFile.exists()) {
            companyFile.getParentFile().mkdirs();
            companyConfig = new YamlConfiguration();
            save();
        }
        companyConfig = YamlConfiguration.loadConfiguration(companyFile);
        if (companyConfig.isConfigurationSection("companies")) {
            for (String id : companyConfig.getConfigurationSection("companies").getKeys(false)) {
                String path = "companies." + id + ".";
                String name = companyConfig.getString(path + "name", "Empresa");
                UUID owner = UUID.fromString(companyConfig.getString(path + "owner"));
                Company company = new Company(id, name, owner);
                company.setVault(companyConfig.getDouble(path + "vault", 0.0));
                Set<String> members = new HashSet<>(companyConfig.getStringList(path + "members"));
                for (String member : members) {
                    company.members().add(UUID.fromString(member));
                    playerCompany.put(UUID.fromString(member), id);
                }
                companies.put(id, company);
            }
        }
    }

    public void save() {
        if (companyConfig == null) {
            companyConfig = new YamlConfiguration();
        }
        companyConfig.set("companies", null);
        for (Company company : companies.values()) {
            String path = "companies." + company.id() + ".";
            companyConfig.set(path + "name", company.name());
            companyConfig.set(path + "owner", company.owner().toString());
            companyConfig.set(path + "vault", company.vault());
            Set<String> list = new HashSet<>();
            for (UUID member : company.members()) {
                list.add(member.toString());
            }
            companyConfig.set(path + "members", list.stream().toList());
        }
        try {
            companyConfig.save(companyFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Falha ao salvar companies.yml: " + ex.getMessage());
        }
    }

    public Company createCompany(UUID owner, String name) {
        if (playerCompany.containsKey(owner)) {
            return null;
        }
        String id = "company_" + System.currentTimeMillis();
        Company company = new Company(id, name, owner);
        companies.put(id, company);
        playerCompany.put(owner, id);
        return company;
    }

    public Company getCompany(UUID uuid) {
        String id = playerCompany.get(uuid);
        return id == null ? null : companies.get(id);
    }

    public void invite(UUID owner, UUID target) {
        invites.put(target, playerCompany.get(owner));
    }

    public boolean acceptInvite(UUID target) {
        String id = invites.remove(target);
        if (id == null) {
            return false;
        }
        Company company = companies.get(id);
        if (company == null) {
            return false;
        }
        company.members().add(target);
        playerCompany.put(target, id);
        return true;
    }

    public void depositVault(Company company, double amount) {
        company.setVault(company.vault() + amount);
    }

    public boolean withdrawVault(Company company, double amount) {
        if (company.vault() < amount) {
            return false;
        }
        company.setVault(company.vault() - amount);
        return true;
    }
}

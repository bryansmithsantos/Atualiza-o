package com.example.economia.features.clans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.messages.Messages;

public class ClanService {

    private final Plugin plugin;
    private final EconomyService economyService;
    private final Map<String, Clan> clans = new HashMap<>(); // ID -> Clan
    private final Map<UUID, String> playerClanMap = new HashMap<>(); // Player UUID -> Clan ID
    private final Map<UUID, String> invites = new HashMap<>(); // Player UUID -> Clan ID (Invite)

    private final File file;
    private FileConfiguration config;

    public static final double CREATION_PRICE = 10000.0;
    public static final int TAG_MIN_LENGTH = 3;
    public static final int TAG_MAX_LENGTH = 5;

    public ClanService(Plugin plugin, EconomyService economyService) {
        this.plugin = plugin;
        this.economyService = economyService;
        this.file = new File(plugin.getDataFolder(), "clans.yml");
    }

    public void load() {
        if (!file.exists())
            return;

        config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("clans")) {
            for (String id : config.getConfigurationSection("clans").getKeys(false)) {
                String path = "clans." + id;
                String tag = config.getString(path + ".tag");
                String name = config.getString(path + ".name");
                UUID owner = UUID.fromString(config.getString(path + ".owner"));

                Clan clan = new Clan(id, tag, name, owner);
                clan.setBankBalance(config.getDouble(path + ".bank", 0.0));
                clan.setKills(config.getInt(path + ".kills", 0));
                clan.setDeaths(config.getInt(path + ".deaths", 0));
                clan.setFriendlyFire(config.getBoolean(path + ".ff", false));

                for (String memberId : config.getStringList(path + ".members")) {
                    UUID uuid = UUID.fromString(memberId);
                    clan.addMember(uuid);
                    playerClanMap.put(uuid, id);
                }

                for (String modId : config.getStringList(path + ".moderators")) {
                    clan.addModerator(UUID.fromString(modId));
                }

                clans.put(id, clan);
            }
        }
    }

    public void save() {
        if (config == null)
            config = new YamlConfiguration();
        config.set("clans", null);

        for (Clan clan : clans.values()) {
            String path = "clans." + clan.getId();
            config.set(path + ".tag", clan.getTag());
            config.set(path + ".name", clan.getName());
            config.set(path + ".owner", clan.getOwner().toString());
            config.set(path + ".bank", clan.getBankBalance());
            config.set(path + ".kills", clan.getKills());
            config.set(path + ".deaths", clan.getDeaths());
            config.set(path + ".ff", clan.isFriendlyFire());

            List<String> members = new ArrayList<>();
            for (UUID uuid : clan.getMembers())
                members.add(uuid.toString());
            config.set(path + ".members", members);

            List<String> mods = new ArrayList<>();
            for (UUID uuid : clan.getModerators())
                mods.add(uuid.toString());
            config.set(path + ".moderators", mods);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Clan createClan(Player player, String tag, String name) {
        if (getClan(player.getUniqueId()) != null) {
            Messages.error(player, "Você já está em um clan.");
            return null;
        }
        if (tag.length() < TAG_MIN_LENGTH || tag.length() > TAG_MAX_LENGTH) {
            Messages.error(player, "A tag deve ter entre 3 e 5 letras.");
            return null;
        }
        if (getClanByTag(tag) != null) {
            Messages.error(player, "Esta tag já está em uso.");
            return null;
        }
        if (!economyService.removeBalance(player.getUniqueId(), CREATION_PRICE)) {
            Messages.error(player, "Dinheiro insuficiente. Custo: $" + CREATION_PRICE);
            return null;
        }

        String id = UUID.randomUUID().toString();
        Clan clan = new Clan(id, tag.toUpperCase(), name, player.getUniqueId());

        clans.put(id, clan);
        playerClanMap.put(player.getUniqueId(), id);

        Messages.success(player, "Clan " + name + " [" + tag.toUpperCase() + "] criado com sucesso!");
        save();
        return clan;
    }

    public void disbandClan(Player player) {
        Clan clan = getClan(player.getUniqueId());
        if (clan == null || !clan.getOwner().equals(player.getUniqueId()))
            return;

        for (UUID member : clan.getMembers()) {
            playerClanMap.remove(member);
            Player p = plugin.getServer().getPlayer(member);
            if (p != null)
                Messages.warning(p, "O clan foi desfeito.");
        }

        clans.remove(clan.getId());
        save();
        Messages.success(player, "Clan desfeito.");
    }

    public Clan getClan(UUID playerUuid) {
        String id = playerClanMap.get(playerUuid);
        return id != null ? clans.get(id) : null;
    }

    public Clan getClanByTag(String tag) {
        for (Clan c : clans.values()) {
            if (c.getTag().equalsIgnoreCase(tag))
                return c;
        }
        return null;
    }

    public void invite(Player requester, Player target) {
        Clan clan = getClan(requester.getUniqueId());
        if (clan == null || !clan.isModerator(requester.getUniqueId())) {
            Messages.error(requester, "Você não tem permissão.");
            return;
        }
        if (getClan(target.getUniqueId()) != null) {
            Messages.error(requester, "Jogador já tem clan.");
            return;
        }

        invites.put(target.getUniqueId(), clan.getId());
        Messages.success(requester, "Convite enviado para " + target.getName());
        Messages.info(target, "Você foi convidado para o clan " + clan.getName() + " [" + clan.getTag() + "]");
        Messages.info(target, "Use /clan accept ou aceite no menu.");
    }

    public void acceptInvite(Player player) {
        String clanId = invites.remove(player.getUniqueId());
        if (clanId == null) {
            Messages.error(player, "Nenhum convite pendente.");
            return;
        }

        Clan clan = clans.get(clanId);
        if (clan == null)
            return;

        clan.addMember(player.getUniqueId());
        playerClanMap.put(player.getUniqueId(), clanId);

        Messages.success(player, "Bem-vindo ao clan " + clan.getTag() + "!");
        for (UUID member : clan.getMembers()) {
            Player p = plugin.getServer().getPlayer(member);
            if (p != null && !p.equals(player)) {
                Messages.info(p, player.getName() + " entrou no clan!");
            }
        }
        save();
    }

    public void kick(Player requester, UUID target) {
        Clan clan = getClan(requester.getUniqueId());
        if (clan == null || !clan.isModerator(requester.getUniqueId()))
            return;

        if (clan.getOwner().equals(target)) {
            Messages.error(requester, "Você não pode expulsar o dono.");
            return;
        }

        if (clan.getMembers().contains(target)) {
            clan.removeMember(target);
            playerClanMap.remove(target);
            Messages.success(requester, "Jogador expulso.");
            save();
        }
    }

    public void leave(Player player) {
        Clan clan = getClan(player.getUniqueId());
        if (clan == null)
            return;

        if (clan.getOwner().equals(player.getUniqueId())) {
            Messages.error(player, "Dono não pode sair. Use disband.");
            return;
        }

        clan.removeMember(player.getUniqueId());
        playerClanMap.remove(player.getUniqueId());
        Messages.success(player, "Você saiu do clan.");
        save();
    }

    public void deposit(Player player, double amount) {
        Clan clan = getClan(player.getUniqueId());
        if (clan == null)
            return;

        if (economyService.removeBalance(player.getUniqueId(), amount)) {
            clan.setBankBalance(clan.getBankBalance() + amount);
            Messages.success(player, "Depositado: $" + amount);
            save();
        } else {
            Messages.error(player, "Saldo insuficiente.");
        }
    }
}

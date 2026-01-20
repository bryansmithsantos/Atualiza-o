package com.example.economia.features.clans;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Clan {

    private final String id;
    private String tag;
    private String name;
    private UUID owner;
    private final Set<UUID> members;
    private final Set<UUID> moderators;
    private double bankBalance;
    private int kills;
    private int deaths;
    private boolean friendlyFire;
    private final long creationDate;

    public Clan(String id, String tag, String name, UUID owner) {
        this.id = id;
        this.tag = tag;
        this.name = name;
        this.owner = owner;
        this.members = new HashSet<>();
        this.members.add(owner);
        this.moderators = new HashSet<>();
        this.bankBalance = 0.0;
        this.kills = 0;
        this.deaths = 0;
        this.friendlyFire = false;
        this.creationDate = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getModerators() {
        return moderators;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        moderators.remove(uuid);
    }

    public void addModerator(UUID uuid) {
        if (members.contains(uuid)) {
            moderators.add(uuid);
        }
    }

    public void removeModerator(UUID uuid) {
        moderators.remove(uuid);
    }

    public boolean isModerator(UUID uuid) {
        return moderators.contains(uuid) || owner.equals(uuid);
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public String getKdr() {
        if (deaths == 0)
            return String.valueOf(kills);
        return String.format("%.2f", (double) kills / deaths);
    }
}

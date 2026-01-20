package com.example.economia.features.company;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Company {

    private final String id;
    private final String name;
    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private double vault;

    public Company(String id, String name, UUID owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.members.add(owner);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public UUID owner() {
        return owner;
    }

    public Set<UUID> members() {
        return members;
    }

    public double vault() {
        return vault;
    }

    public void setVault(double vault) {
        this.vault = vault;
    }
}

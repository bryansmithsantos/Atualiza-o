package com.example.economia.features.dungeon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

public class DungeonSession {

    private final Plugin plugin;
    private final DungeonDifficulty difficulty;
    private final Set<UUID> players = new HashSet<>();
    private final UUID owner;
    private Location arenaCenter;
    private int currentWave = 0;
    private int mobsRemaining = 0;
    private boolean active = false;
    private boolean bossPhase = false;
    private IronGolem boss;
    private int bossTaskId = -1;
    private double totalReward = 0;
    private final List<Location> arenaBlocks = new ArrayList<>();

    public DungeonSession(Plugin plugin, Player owner, DungeonDifficulty difficulty) {
        this.plugin = plugin;
        this.owner = owner != null ? owner.getUniqueId() : null;
        this.difficulty = difficulty;
        if (owner != null) {
            this.players.add(owner.getUniqueId());
        }
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public boolean hasPlayer(UUID uuid) {
        return players.contains(uuid);
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public UUID getOwner() {
        return owner;
    }

    public DungeonDifficulty getDifficulty() {
        return difficulty;
    }

    public boolean isActive() {
        return active;
    }

    // Generate arena without starting (for natural spawns)
    public void generateArenaOnly() {
        generateArena();
    }

    // Cleanup arena without rewards (for timeout)
    public void cleanup() {
        if (arenaCenter != null) {
            for (Entity entity : arenaCenter.getWorld().getEntities()) {
                if (entity.getLocation().distance(arenaCenter) < 60 && !(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }
        for (Location loc : arenaBlocks) {
            loc.getBlock().setType(Material.AIR);
        }
        arenaBlocks.clear();
    }

    public void start() {
        active = true;
        generateArena();
        teleportPlayers();

        // Countdown
        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown > 0) {
                    broadcastTitle("§e" + countdown, "§7Prepare-se!");
                    playSound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f);
                    countdown--;
                } else {
                    cancel();
                    broadcastTitle("§c§lCOMEÇOU!", difficulty.getColor() + difficulty.getName());
                    playSound(org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f);
                    startNextWave();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void generateArena() {
        World world = Bukkit.getWorlds().get(0);
        // Arena at Y=300 (sky)
        int baseX = (int) (Math.random() * 10000) - 5000;
        int baseZ = (int) (Math.random() * 10000) - 5000;
        int baseY = 300;

        arenaCenter = new Location(world, baseX + 25, baseY + 1, baseZ + 25);

        // Generate 50x50 arena
        for (int x = 0; x < 50; x++) {
            for (int z = 0; z < 50; z++) {
                // Floor
                Block floor = world.getBlockAt(baseX + x, baseY, baseZ + z);
                floor.setType(Material.DEEPSLATE_BRICKS);
                arenaBlocks.add(floor.getLocation());

                // Walls (edges)
                if (x == 0 || x == 49 || z == 0 || z == 49) {
                    for (int y = 1; y <= 5; y++) {
                        Block wall = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
                        wall.setType(Material.DEEPSLATE_BRICK_WALL);
                        arenaBlocks.add(wall.getLocation());
                    }
                }
            }
        }

        // Torches for lighting
        for (int x = 5; x < 50; x += 10) {
            for (int z = 5; z < 50; z += 10) {
                Block torch = world.getBlockAt(baseX + x, baseY + 1, baseZ + z);
                torch.setType(Material.LANTERN);
                arenaBlocks.add(torch.getLocation());
            }
        }
    }

    private void teleportPlayers() {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.teleport(arenaCenter);
                Component enterMsg = Component.text("★ ", NamedTextColor.GOLD)
                        .append(Component.text("Você entrou na dungeon: ", NamedTextColor.GREEN))
                        .append(LegacyComponentSerializer.legacySection()
                                .deserialize(difficulty.getColor() + difficulty.getName()));
                p.sendMessage(enterMsg);
            }
        }
    }

    public void startNextWave() {
        currentWave++;

        if (currentWave > difficulty.getWaves()) {
            // Victory!
            victory();
            return;
        }

        // Check if boss wave
        if (currentWave == difficulty.getWaves()) {
            bossPhase = true;
            spawnBoss();
            return;
        }

        // Normal wave
        int mobCount = difficulty.getMobsPerWave(currentWave);
        mobsRemaining = mobCount;

        broadcastTitle("§6Onda " + currentWave, "§7" + mobCount + " inimigos!");
        broadcastMessage("§e⚔ Onda " + currentWave + "/" + difficulty.getWaves() + " - " + mobCount + " mobs!");

        // Spawn mobs
        spawnMobs(mobCount);
    }

    private void spawnMobs(int count) {
        EntityType[] mobTypes = getMobTypes();

        for (int i = 0; i < count; i++) {
            Location spawnLoc = getRandomArenaLocation();
            EntityType type = mobTypes[(int) (Math.random() * mobTypes.length)];

            Entity entity = arenaCenter.getWorld().spawnEntity(spawnLoc, type);
            if (entity instanceof LivingEntity living) {
                living.setRemoveWhenFarAway(false);
                if (living instanceof Mob mob) {
                    mob.setPersistent(true);
                    Player target = getRandomPlayer();
                    if (target != null) {
                        mob.setTarget(target);
                    }
                }

                // Prevent burning with helmet (Button)
                if (living.getEquipment() != null) {
                    living.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.STONE_BUTTON));
                    // Armor scaling for high levels
                    if (difficulty.ordinal() >= 5) {
                        living.getEquipment()
                                .setChestplate(new org.bukkit.inventory.ItemStack(Material.IRON_CHESTPLATE));
                    }
                    if (difficulty.ordinal() >= 8) {
                        living.getEquipment()
                                .setChestplate(new org.bukkit.inventory.ItemStack(Material.NETHERITE_CHESTPLATE));
                        living.getEquipment()
                                .setLeggings(new org.bukkit.inventory.ItemStack(Material.NETHERITE_LEGGINGS));
                    }
                }
            }
        }
    }

    private EntityType[] getMobTypes() {
        return switch (difficulty) {
            case NIVEL_1 -> new EntityType[] { EntityType.ZOMBIE, EntityType.SKELETON };
            case NIVEL_2 ->
                new EntityType[] { EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CAVE_SPIDER };
            case NIVEL_3 -> new EntityType[] { EntityType.ZOMBIE, EntityType.SKELETON, EntityType.BLAZE,
                    EntityType.WITHER_SKELETON };
            case NIVEL_4 -> new EntityType[] { EntityType.WITHER_SKELETON, EntityType.BLAZE, EntityType.PIGLIN_BRUTE,
                    EntityType.VINDICATOR };
            case NIVEL_5 -> new EntityType[] { EntityType.WITHER_SKELETON, EntityType.BLAZE, EntityType.EVOKER,
                    EntityType.RAVAGER };
            case NIVEL_6, NIVEL_7 -> new EntityType[] { EntityType.WITHER_SKELETON, EntityType.VINDICATOR,
                    EntityType.EVOKER, EntityType.ELDER_GUARDIAN };
            case NIVEL_8, NIVEL_9 ->
                new EntityType[] { EntityType.WITHER_SKELETON, EntityType.RAVAGER, EntityType.VEX, EntityType.WARDEN };
            case NIVEL_10 ->
                new EntityType[] { EntityType.WARDEN, EntityType.WITHER, EntityType.EVOKER, EntityType.RAVAGER };
        };
    }

    private void spawnBoss() {
        broadcastTitle("§4§lBOSS!", "§c" + getBossName());
        broadcastMessage("§4☠ " + getBossName() + " §capareceu!");
        playSound(org.bukkit.Sound.ENTITY_WITHER_SPAWN, 1f);

        // Boss message
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            broadcastMessage("§c§l" + getBossName() + "§c: §7\"Vocês ousam invadir meu domínio?!\"");
        }, 40L);

        Location bossLoc = arenaCenter.clone().add(0, 0, 10);
        boss = (IronGolem) arenaCenter.getWorld().spawnEntity(bossLoc, EntityType.IRON_GOLEM);
        boss.customName(LegacyComponentSerializer.legacySection().deserialize("§c§l" + getBossName()));
        boss.setCustomNameVisible(true);
        boss.setRemoveWhenFarAway(false);

        // Set HP using Attribute
        org.bukkit.attribute.AttributeInstance maxHealthAttr = boss
                .getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(difficulty.getBossHP());
        }
        boss.setHealth(difficulty.getBossHP());

        // Make hostile
        Player target = getRandomPlayer();
        if (target != null) {
            boss.setTarget(target);
        }

        // Start boss powers
        startBossPowers();
    }

    private String getBossName() {
        return switch (difficulty) {
            case NIVEL_1 -> "Golem Corrompido";
            case NIVEL_2 -> "Golem de Fogo";
            case NIVEL_3 -> "Comandante das Trevas";
            case NIVEL_4 -> "Senhor da Morte";
            case NIVEL_5 -> "Destruidor Final";
            case NIVEL_6 -> "Fantasma do Vazio";
            case NIVEL_7 -> "Anomalia Espacial";
            case NIVEL_8 -> "Titã de Sangue";
            case NIVEL_9 -> "Deus da Peste";
            case NIVEL_10 -> "O Próprio Caos";
        };
    }

    private void startBossPowers() {
        bossTaskId = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!active || boss == null || boss.isDead()) {
                    cancel();
                    return;
                }

                tick++;

                // Different powers based on difficulty and timing
                if (tick % 200 == 0) { // Every 10 seconds
                    useBossPower();
                }

                // Boss health messages
                org.bukkit.attribute.AttributeInstance maxHp = boss
                        .getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                double maxHealth = maxHp != null ? maxHp.getValue() : 100;
                double healthPercent = boss.getHealth() / maxHealth;
                if (healthPercent < 0.5 && tick == 200) {
                    broadcastMessage("§c§l" + getBossName() + "§c: §7\"Isso é só o começo!\"");
                }
                if (healthPercent < 0.25 && tick == 400) {
                    broadcastMessage("§c§l" + getBossName() + "§c: §7\"VOCÊS VÃO PAGAR!\"");
                    // Enrage - extra power
                    useBossPower();
                    useBossPower();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L).getTaskId();
    }

    private void useBossPower() {
        if (boss == null || !active)
            return;

        int powerCount = difficulty.ordinal() >= 5 ? 2 : 1;
        for (int i = 0; i < powerCount; i++) {
            int power = (int) (Math.random() * 5);
            switch (power) {
                case 0 -> fireballAttack();
                case 1 -> summonMinions();
                case 2 -> blindnessAttack();
                case 3 -> lightningAttack();
                case 4 -> fireRain();
            }
        }
    }

    private void fireballAttack() {
        broadcastMessage("§6" + getBossName() + " §elançou bolas de fogo!");
        playSound(org.bukkit.Sound.ENTITY_BLAZE_SHOOT, 1f);

        for (int i = 0; i < 3; i++) {
            Player target = getRandomPlayer();
            if (target != null && boss != null) {
                org.bukkit.util.Vector dir = target.getLocation().subtract(boss.getEyeLocation()).toVector()
                        .normalize();
                boss.getWorld().spawn(boss.getEyeLocation(), org.bukkit.entity.Fireball.class, fb -> {
                    fb.setDirection(dir);
                    fb.setYield(1.5f);
                    fb.setIsIncendiary(false);
                });
            }
        }
    }

    private void summonMinions() {
        int count = 3 + difficulty.ordinal() * 2;
        broadcastMessage("§5" + getBossName() + " §dinvocou " + count + " soldados!");
        playSound(org.bukkit.Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1f);

        for (int i = 0; i < count; i++) {
            Location loc = getRandomArenaLocation();
            EntityType type = Math.random() > 0.5 ? EntityType.ZOMBIE : EntityType.SKELETON;
            Entity entity = arenaCenter.getWorld().spawnEntity(loc, type);
            if (entity instanceof Mob mob) {
                Player target = getRandomPlayer();
                if (target != null)
                    mob.setTarget(target);
            }
        }
    }

    private void blindnessAttack() {
        broadcastMessage("§8" + getBossName() + " §7lançou escuridão!");
        playSound(org.bukkit.Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f);

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            }
        }
    }

    private void lightningAttack() {
        broadcastMessage("§b" + getBossName() + " §3invocou raios!");
        playSound(org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f);

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.getWorld().strikeLightningEffect(p.getLocation());
                p.damage(4.0); // 2 hearts
            }
        }
    }

    private void fireRain() {
        broadcastMessage("§c" + getBossName() + " §6invocou chuva de fogo!");
        playSound(org.bukkit.Sound.ENTITY_GHAST_SHOOT, 1f);

        for (int i = 0; i < 8; i++) {
            Location target = getRandomArenaLocation();
            Location sky = target.clone().add(0, 15, 0);
            arenaCenter.getWorld().spawn(sky, org.bukkit.entity.Fireball.class, fb -> {
                fb.setDirection(new org.bukkit.util.Vector(0, -1, 0));
                fb.setYield(1f);
                fb.setIsIncendiary(false);
            });
        }
    }

    public void onMobKill() {
        mobsRemaining--;
        if (mobsRemaining <= 0 && !bossPhase) {
            waveComplete();
        }
    }

    public void onBossKill() {
        if (bossTaskId != -1) {
            Bukkit.getScheduler().cancelTask(bossTaskId);
        }
        broadcastMessage("§a§l" + getBossName() + " §afoi derrotado!");
        broadcastMessage("§c§l" + getBossName() + "§c: §7\"Isso... não é... possível...\"");
        totalReward += difficulty.getMaxReward() * 0.5; // Boss bonus
        victory();
    }

    private void waveComplete() {
        totalReward += difficulty.getRewardPerWave();
        broadcastMessage("§a✓ Onda " + currentWave + " completa! §e+" + formatMoney(difficulty.getRewardPerWave()));
        playSound(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.5f);

        // Global announcement for progress
        if (currentWave > 1) {
            String ownerName = Bukkit.getPlayer(owner) != null ? Bukkit.getPlayer(owner).getName() : "Equipe";
            Bukkit.getServer().sendMessage(Component.text("⚔ ", NamedTextColor.GOLD)
                    .append(Component.text(ownerName + " e sua equipe", NamedTextColor.YELLOW))
                    .append(Component.text(" completaram a onda " + currentWave + " da dungeon!",
                            NamedTextColor.GRAY)));
        }

        // 5 second break
        broadcastTitle("§a§lOnda Completa!", "§7Próxima em 5s...");
        Bukkit.getScheduler().runTaskLater(plugin, this::startNextWave, 100L);
    }

    private void victory() {
        active = false;
        cleanupArena();

        broadcastTitle("§a§lVITÓRIA!", "§e+" + formatMoney(totalReward));
        playSound(org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f);

        // Teleport back and give rewards
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                // Give reward (split among players)
                double share = totalReward / players.size();
                p.sendMessage(Component.text("★ ", NamedTextColor.GOLD)
                        .append(Component.text("DUNGEON COMPLETA! ", NamedTextColor.GREEN))
                        .append(Component.text("+" + formatMoney(share), NamedTextColor.YELLOW)));
            }
        }
    }

    public void defeat() {
        active = false;
        cleanupArena();

        // Partial reward
        double partial = totalReward * 0.5;

        broadcastTitle("§c§lDERROTA!", "§7Você morreu...");

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                double share = partial / players.size();
                if (share > 0) {
                    p.sendMessage(Component.text("☠ ", NamedTextColor.RED)
                            .append(Component.text("Dungeon falhou! ", NamedTextColor.RED))
                            .append(Component.text("Recompensa parcial: " + formatMoney(share),
                                    NamedTextColor.YELLOW)));
                }
            }
        }
    }

    private void cleanupArena() {
        // Remove all mobs in arena
        if (arenaCenter != null) {
            for (Entity entity : arenaCenter.getWorld().getEntities()) {
                if (entity.getLocation().distance(arenaCenter) < 60 && !(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }

        // Remove arena blocks
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Location loc : arenaBlocks) {
                loc.getBlock().setType(Material.AIR);
            }
            arenaBlocks.clear();
        }, 60L);
    }

    private Location getRandomArenaLocation() {
        double offsetX = Math.random() * 40 - 20;
        double offsetZ = Math.random() * 40 - 20;
        return arenaCenter.clone().add(offsetX, 0, offsetZ);
    }

    private Player getRandomPlayer() {
        List<UUID> online = new ArrayList<>();
        for (UUID uuid : players) {
            if (Bukkit.getPlayer(uuid) != null) {
                online.add(uuid);
            }
        }
        if (online.isEmpty())
            return null;
        return Bukkit.getPlayer(online.get((int) (Math.random() * online.size())));
    }

    private void broadcastTitle(String title, String subtitle) {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.showTitle(Title.title(
                        LegacyComponentSerializer.legacySection().deserialize(title),
                        LegacyComponentSerializer.legacySection().deserialize(subtitle)));
            }
        }
    }

    private void broadcastMessage(String message) {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(message);
            }
        }
    }

    private void playSound(org.bukkit.Sound sound, float pitch) {
        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.playSound(p.getLocation(), sound, 1f, pitch);
            }
        }
    }

    private String formatMoney(double amount) {
        if (amount >= 1000000) {
            return String.format("$%.2fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("$%.1fK", amount / 1000);
        }
        return String.format("$%.0f", amount);
    }

    public Location getArenaCenter() {
        return arenaCenter;
    }

    public IronGolem getBoss() {
        return boss;
    }

    public int getCurrentWave() {
        return currentWave;
    }
}

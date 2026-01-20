package com.example.economia.features.generators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.example.economia.features.messages.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GeneratorService {

    private final Plugin plugin;
    private final Map<Location, Generator> generators = new HashMap<>();
    private final File file;
    private FileConfiguration config;

    public GeneratorService(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "generators.yml");
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("generators")) {
            for (String key : config.getConfigurationSection("generators").getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    UUID ownerId = UUID.fromString(config.getString("generators." + key + ".owner"));
                    Location loc = config.getLocation("generators." + key + ".location");
                    GeneratorType type = GeneratorType.valueOf(config.getString("generators." + key + ".type"));

                    Generator generator = new Generator(id, ownerId, loc, type);
                    generators.put(loc, generator);
                } catch (Exception e) {
                    plugin.getLogger().warning("Erro ao carregar gerador " + key);
                }
            }
        }

        // Iniciar task de geração
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void save() {
        if (config == null) {
            config = new YamlConfiguration();
        }
        config.set("generators", null); // Limpar anterior

        for (Generator gen : generators.values()) {
            String path = "generators." + gen.getId().toString();
            config.set(path + ".owner", gen.getOwnerId().toString());
            config.set(path + ".location", gen.getLocation());
            config.set(path + ".type", gen.getType().name());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tick() {
        long now = System.currentTimeMillis();

        for (Generator gen : generators.values()) {
            // Verificar intervalo
            if (now - gen.getLastGenerationTime() < gen.getType().getIntervalSeconds() * 1000L) {
                continue;
            }

            // Tentar gerar
            if (generateItem(gen)) {
                gen.setLastGenerationTime(now);
                playEffect(gen.getLocation());
            }
        }
    }

    private boolean generateItem(Generator gen) {
        Block block = gen.getLocation().getBlock();
        if (block.getType() != gen.getType().getBlockMaterial()) {
            // Bloco foi quebrado ou mudado mas não removido da lista?
            return false;
        }

        // Procurar baú adjacente
        Chest chest = findAttachedChest(block);
        if (chest == null) {
            return false;
        }

        Inventory inv = chest.getInventory();
        ItemStack item = new ItemStack(gen.getType().getDropMaterial());

        if (inv.firstEmpty() == -1) {
            // Inventário cheio, verificar se dá pra stackar
            for (ItemStack is : inv.getContents()) {
                if (is != null && is.isSimilar(item) && is.getAmount() < is.getMaxStackSize()) {
                    inv.addItem(item);
                    return true;
                }
            }
            return false;
        }

        inv.addItem(item);
        return true;
    }

    private Chest findAttachedChest(Block generatorBlock) {
        Block[] disconnected = {
                generatorBlock.getRelative(1, 0, 0),
                generatorBlock.getRelative(-1, 0, 0),
                generatorBlock.getRelative(0, 0, 1),
                generatorBlock.getRelative(0, 0, -1)
        };

        for (Block b : disconnected) {
            if (b.getState() instanceof Chest chest) {
                return chest;
            }
        }
        return null;
    }

    private void playEffect(Location loc) {
        // Partículas leves para não lagar
        loc.getWorld().spawnParticle(org.bukkit.Particle.COMPOSTER, loc.clone().add(0.5, 1, 0.5), 3, 0.2, 0.2, 0.2, 0);
    }

    public void createGenerator(Player player, GeneratorType type) {
        // Dar o item para o jogador colocar
        ItemStack item = new ItemStack(type.getBlockMaterial());
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.displayName(
                type.getDisplayName().decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Gerador de Minérios", NamedTextColor.GRAY));
        lore.add(Component.text("Coloque ao lado de um baú!", NamedTextColor.YELLOW));
        lore.add(Component.text("Gera: ", NamedTextColor.GRAY)
                .append(Component.text(type.getDropMaterial().name(), type.getColor())));
        // Guardar o tipo no NBT/Lore escondida para identificar ao colocar
        // (simplificado via lore aqui)
        // Para uma solução robusta usaria PersistentDataContainer, mas Lore serve por
        // enquanto e é visível

        meta.lore(lore);
        // Marcador para identificar que é um gerador
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "generator_type"),
                org.bukkit.persistence.PersistentDataType.STRING,
                type.name());

        item.setItemMeta(meta);

        HashMap<Integer, ItemStack> left = player.getInventory().addItem(item);
        if (!left.isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), left.get(0));
        }

        Messages.success(player, "Você recebeu um " + type.getName() + "!");
    }

    public void registerPlacedGenerator(Player player, Location loc, GeneratorType type) {
        Generator gen = new Generator(UUID.randomUUID(), player.getUniqueId(), loc, type);
        generators.put(loc, gen);
        Messages.success(player, "Gerador posicionado! Coloque um baú ao lado.");
        save();
    }

    public void removeGenerator(Location loc) {
        if (generators.containsKey(loc)) {
            generators.remove(loc);
            save();
        }
    }

    public Generator getGeneratorAt(Location loc) {
        return generators.get(loc);
    }

    public boolean isGenerator(Location loc) {
        return generators.containsKey(loc);
    }
}

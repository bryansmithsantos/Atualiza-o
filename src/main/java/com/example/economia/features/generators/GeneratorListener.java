package com.example.economia.features.generators;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.example.economia.features.messages.Messages;

public class GeneratorListener implements Listener {

    private final GeneratorService generatorService;
    private final Plugin plugin;

    public GeneratorListener(GeneratorService generatorService, Plugin plugin) {
        this.generatorService = generatorService;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!item.hasItemMeta())
            return;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "generator_type");

        if (container.has(key, PersistentDataType.STRING)) {
            String typeName = container.get(key, PersistentDataType.STRING);
            try {
                GeneratorType type = GeneratorType.valueOf(typeName);
                generatorService.registerPlacedGenerator(event.getPlayer(), event.getBlock().getLocation(), type);
            } catch (IllegalArgumentException e) {
                // Tipo inválido
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (generatorService.isGenerator(block.getLocation())) {
            Player player = event.getPlayer();
            Generator gen = generatorService.getGeneratorAt(block.getLocation());

            // Verificar se é dono ou admin? Por enquanto deixa quebrar

            generatorService.removeGenerator(block.getLocation());
            Messages.warning(player, "Você removeu um " + gen.getType().getName());

            // Dropar o item do gerador customizado
            event.setDropItems(false); // Não dropar o bloco vanilla

            // Recriar item para dropar
            generatorService.createGenerator(player, gen.getType()); // Dá direto pro inventário (ou dropa se cheio)
        }
    }
}

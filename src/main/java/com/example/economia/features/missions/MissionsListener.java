package com.example.economia.features.missions;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

import io.papermc.paper.event.player.AsyncChatEvent;

public class MissionsListener implements Listener {

    private final MissionsService missionsService;

    private static final Set<Material> ORES = Set.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS);

    private static final Set<Material> LOGS = Set.of(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM);

    private static final Set<Material> STONES = Set.of(
            Material.STONE, Material.COBBLESTONE, Material.DEEPSLATE,
            Material.COBBLED_DEEPSLATE, Material.GRANITE, Material.DIORITE,
            Material.ANDESITE, Material.TUFF, Material.CALCITE,
            Material.NETHERRACK, Material.BASALT, Material.BLACKSTONE);

    private static final Set<Material> CROPS = Set.of(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.MELON, Material.PUMPKIN,
            Material.NETHER_WART, Material.COCOA, Material.SWEET_BERRY_BUSH);

    public MissionsListener(MissionsService missionsService) {
        this.missionsService = missionsService;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material type = event.getBlock().getType();

        if (STONES.contains(type)) {
            missionsService.increment(player.getUniqueId(), MissionType.BREAK_STONE, 1);
        }
        if (ORES.contains(type)) {
            missionsService.increment(player.getUniqueId(), MissionType.BREAK_ORE, 1);
        }
        if (LOGS.contains(type)) {
            missionsService.increment(player.getUniqueId(), MissionType.BREAK_LOG, 1);
        }
        if (CROPS.contains(type)) {
            missionsService.increment(player.getUniqueId(), MissionType.HARVEST_CROP, 1);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null)
            return;

        if (event.getEntity() instanceof Monster) {
            missionsService.increment(killer.getUniqueId(), MissionType.KILL_MOB, 1);
        }
        if (event.getEntity() instanceof Player) {
            missionsService.increment(killer.getUniqueId(), MissionType.KILL_PLAYER, 1);
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == State.CAUGHT_FISH) {
            missionsService.increment(event.getPlayer().getUniqueId(), MissionType.FISH_CATCH, 1);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            missionsService.increment(player.getUniqueId(), MissionType.CRAFT_ITEM, 1);
        }
    }

    @EventHandler
    public void onSmelt(FurnaceExtractEvent event) {
        missionsService.increment(event.getPlayer().getUniqueId(), MissionType.SMELT_ITEM, event.getItemAmount());
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        missionsService.increment(event.getEnchanter().getUniqueId(), MissionType.ENCHANT_ITEM, 1);
    }

    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player player) {
            missionsService.increment(player.getUniqueId(), MissionType.BREED_ANIMAL, 1);
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        missionsService.increment(event.getPlayer().getUniqueId(), MissionType.CHAT_MESSAGE, 1);
    }
}

package com.example.economia.features.treefeller;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.jobs.JobsService;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

/**
 * TreeFeller - Sistema profissional de corte de árvore inteira.
 * 
 * Ativação: Segurar SHIFT + Machado ao quebrar madeira.
 * Funcionalidades:
 * - Corta toda a árvore conectada
 * - Quebra folhas automaticamente
 * - Danifica o machado proporcionalmente
 * - Exibe título animado com estatísticas
 * - Limite de blocos para evitar lag
 */
public final class TreeFellerListener implements Listener {

    private final JobsService jobsService;
    private final EconomyService economyService;

    public TreeFellerListener(JobsService jobsService, EconomyService economyService) {
        this.jobsService = jobsService;
        this.economyService = economyService;
    }

    private static final int MAX_LOGS = 128;
    private static final int MAX_LEAVES = 256;

    private static final Set<Material> LOGS = Set.of(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG,
            Material.CRIMSON_STEM, Material.WARPED_STEM,
            Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG,
            Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
            Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG,
            Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM);

    private static final Set<Material> LEAVES = Set.of(
            Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
            Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES,
            Material.AZALEA_LEAVES, Material.FLOWERING_AZALEA_LEAVES,
            Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK);

    private static final Set<Material> AXES = Set.of(
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE);

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block origin = event.getBlock();

        // Validações iniciais
        if (!isLog(origin.getType()))
            return;

        // Debug
        // System.out.println("DEBUG: TreeFeller check - Player: " + player.getName());
        // System.out.println("DEBUG: isSneaking: " + player.isSneaking());
        // System.out.println("DEBUG: hasAxe: " + hasAxeInHand(player));
        // System.out.println("DEBUG: hasPermission: " +
        // player.hasPermission("blinded.treefeller"));

        if (!player.isSneaking()) {
            if (hasAxeInHand(player)) {
                player.sendActionBar(
                        Component.text("💡 Dica: Agache para cortar a árvore inteira!").color(NamedTextColor.YELLOW));
            }
            return;
        }
        if (!hasAxeInHand(player)) {
            return;
        }
        if (!player.hasPermission("blinded.treefeller")) {
            // player.sendMessage("§cDebug: Sem permissão blinded.treefeller");
            return;
        }

        ItemStack axe = player.getInventory().getItemInMainHand();

        // Buscar árvore usando BFS (mais eficiente que recursão)
        TreeResult tree = findTree(origin);

        if (tree.logs.size() <= 1)
            return;

        // Verificar durabilidade
        int durabilityNeeded = tree.logs.size();
        int durabilityRemaining = getDurability(axe);

        if (durabilityRemaining < durabilityNeeded) {
            sendError(player, "Seu machado não aguenta! Precisa de " + durabilityNeeded + " de durabilidade.");
            return;
        }

        // Cancelar evento e processar
        event.setCancelled(true);

        // Quebrar madeiras
        for (Block log : tree.logs) {
            log.breakNaturally(axe);
        }

        // Quebrar folhas
        for (Block leaf : tree.leaves) {
            leaf.breakNaturally();
        }

        // Danificar machado
        applyDamage(axe, durabilityNeeded);

        // Verificar se quebrou
        if (getDurability(axe) <= 0) {
            player.getInventory().setItemInMainHand(null);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        }

        // Calcular recompensas se for Lenhador
        double moneyEarned = 0;
        int xpEarned = 0;

        if (jobsService.getCurrentJob(player).id().equals("lenhador")) {
            double pricePerLog = 5.0; // Valor fixo por tronco
            int xpPerLog = 2;

            moneyEarned = tree.logs.size() * pricePerLog;
            xpEarned = tree.logs.size() * xpPerLog;

            if (moneyEarned > 0) {
                economyService.addBalance(player.getUniqueId(), moneyEarned);
                jobsService.addXp(player, "lenhador", xpEarned);
            }
        }

        // Feedback visual
        showSuccess(player, tree.logs.size(), tree.leaves.size(), moneyEarned, xpEarned);
    }

    /**
     * Busca todos os blocos da árvore usando BFS iterativo.
     * Mais seguro que recursão para árvores grandes.
     */
    private TreeResult findTree(Block origin) {
        Set<Block> logs = new HashSet<>();
        Set<Block> leaves = new HashSet<>();
        Set<Block> visited = new HashSet<>();

        Deque<Block> queue = new ArrayDeque<>();
        queue.add(origin);

        while (!queue.isEmpty() && logs.size() < MAX_LOGS) {
            Block current = queue.poll();

            if (visited.contains(current))
                continue;
            visited.add(current);

            Material type = current.getType();

            if (isLog(type)) {
                logs.add(current);
                // Adicionar vizinhos (26-conectividade)
                addNeighbors(queue, current, visited);
            } else if (isLeaf(type) && leaves.size() < MAX_LEAVES) {
                leaves.add(current);
                // Folhas só conectam com outras folhas
                addLeafNeighbors(queue, current, visited, leaves);
            }
        }

        return new TreeResult(logs, leaves);
    }

    private void addNeighbors(Deque<Block> queue, Block block, Set<Block> visited) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0)
                        continue;
                    Block neighbor = block.getRelative(dx, dy, dz);
                    if (!visited.contains(neighbor) && (isLog(neighbor.getType()) || isLeaf(neighbor.getType()))) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private void addLeafNeighbors(Deque<Block> queue, Block block, Set<Block> visited, Set<Block> leaves) {
        if (leaves.size() >= MAX_LEAVES)
            return;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0)
                        continue;
                    Block neighbor = block.getRelative(dx, dy, dz);
                    if (!visited.contains(neighbor) && isLeaf(neighbor.getType())) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private boolean isLog(Material mat) {
        return LOGS.contains(mat);
    }

    private boolean isLeaf(Material mat) {
        return LEAVES.contains(mat);
    }

    private boolean hasAxeInHand(Player player) {
        return AXES.contains(player.getInventory().getItemInMainHand().getType());
    }

    private int getDurability(ItemStack item) {
        if (item.getItemMeta() instanceof Damageable meta) {
            return item.getType().getMaxDurability() - meta.getDamage();
        }
        return 0;
    }

    private void applyDamage(ItemStack item, int amount) {
        if (item.getItemMeta() instanceof Damageable meta) {
            meta.setDamage(meta.getDamage() + amount);
            item.setItemMeta(meta);
        }
    }

    private void sendError(Player player, String message) {
        player.sendMessage(Component.text("⚠ " + message).color(NamedTextColor.RED));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    private void showSuccess(Player player, int logs, int leaves, double money, int xp) {
        Title title = Title.title(
                Component.text("🌲 Árvore Derrubada!").color(NamedTextColor.GREEN),
                Component.text("+" + logs + " madeiras" + (money > 0 ? " • §a+$" + money : ""))
                        .color(NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(1500), Duration.ofMillis(300)));
        player.showTitle(title);

        String actionMsg = "✓ TreeFeller ativo!";
        if (xp > 0)
            actionMsg += " §b+" + xp + " XP Lenhador";

        player.sendActionBar(Component.text(actionMsg).color(NamedTextColor.GRAY));

        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
    }

    /**
     * Resultado da busca de árvore.
     */
    private record TreeResult(Set<Block> logs, Set<Block> leaves) {
    }
}

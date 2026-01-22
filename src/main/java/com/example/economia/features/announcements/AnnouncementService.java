package com.example.economia.features.announcements;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class AnnouncementService {

    private final Plugin plugin;
    private final List<Component> announcements = new ArrayList<>();
    private int currentIndex = 0;
    private int taskId = -1;

    public AnnouncementService(Plugin plugin) {
        this.plugin = plugin;
        registerAnnouncements();
    }

    private void registerAnnouncements() {
        add("§6§l⚡ NOVIDADE! §r§7Use §e/upgrade §7para evoluir seus itens com XP!");
        add("§b§l✦ DICA! §r§7Use §e/evoluir §7para transformar diamante em netherite!");
        add("§d§l★ ECONOMIA! §r§7Venda qualquer item com §e/venda §7- segure na mão!");
        add("§a§l💰 BANCO! §r§7Clique em §e'Depositar Tudo' §7no banco para depositar rápido!");
        add("§c§l☠ MORTE! §r§7Seus itens ficam em uma §elápide/baú §7quando você morre!");
        add("§5§l⬆ MELHORIAS! §r§7Aumente seu §esalário §7em +$100 por nível!");
        add("§e§l🏪 LOJA VIP! §r§7Compre §cspawners§7, §avillagers§7 e itens OP em §e/lojavip§7!");
        add("§6§l🎯 MISSÕES! §r§7Complete missões diárias para ganhar recompensas!");
        add("§b§l📍 COORDENADAS! §r§7Veja sua posição X, Y, Z no scoreboard!");
        add("§a§l🏠 CASAS! §r§7Use §e/sethome §7para salvar e §e/home §7para teleportar!");
        add("§d§l⚔ CLÃS! §r§7Crie seu clã com §e/clan criar <nome>§7!");
        add("§c§l🔥 GERADORES! §r§7Compre geradores de minério no §e/painel§7!");
        add("§6§l💎 RANKS! §r§7Evolua seu rank no servidor para mais benefícios!");
        add("§b§l📢 DISCORD! §r§7Entre no nosso Discord para novidades e suporte!");
    }

    private void add(String message) {
        announcements.add(Component.text("")
                .append(Component.text("           ", NamedTextColor.DARK_GRAY))
                .append(Component.text("「", NamedTextColor.DARK_GRAY))
                .append(Component.text(" BLINDED ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text("」", NamedTextColor.DARK_GRAY))
                .appendNewline()
                .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                        .deserialize(message)));
    }

    public void start() {
        if (taskId != -1)
            return;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin,
                this::broadcast,
                20 * 60,
                20 * 60 * 3);
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void broadcast() {
        if (announcements.isEmpty() || Bukkit.getOnlinePlayers().isEmpty())
            return;

        Component message = announcements.get(currentIndex);
        Component separator = Component.text("§8§m                                                    ");

        Bukkit.getServer().sendMessage(Component.empty());
        Bukkit.getServer().sendMessage(separator);
        Bukkit.getServer().sendMessage(message);
        Bukkit.getServer().sendMessage(separator);
        Bukkit.getServer().sendMessage(Component.empty());

        currentIndex = (currentIndex + 1) % announcements.size();
    }

    public void broadcastCustom(String message) {
        Component customMsg = Component.text("")
                .append(Component.text("           ", NamedTextColor.DARK_GRAY))
                .append(Component.text("「", NamedTextColor.DARK_GRAY))
                .append(Component.text(" ANÚNCIO ", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .append(Component.text("」", NamedTextColor.DARK_GRAY))
                .appendNewline()
                .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                        .deserialize(message));

        Component separator = Component.text("§8§m                                                    ");

        Bukkit.getServer().sendMessage(Component.empty());
        Bukkit.getServer().sendMessage(separator);
        Bukkit.getServer().sendMessage(customMsg);
        Bukkit.getServer().sendMessage(separator);
        Bukkit.getServer().sendMessage(Component.empty());

        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }
}

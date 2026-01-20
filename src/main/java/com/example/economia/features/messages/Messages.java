package com.example.economia.features.messages;

import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Sistema centralizado de mensagens bonitas do plugin.
 */
public final class Messages {

    private static final String PREFIX = "ʙʟɪɴᴅᴇᴅ";
    private static final TextColor PRIMARY = TextColor.color(255, 170, 0); // Dourado
    private static final TextColor SECONDARY = TextColor.color(85, 255, 255); // Aqua
    private static final TextColor SUCCESS = TextColor.color(85, 255, 85); // Verde
    private static final TextColor ERROR = TextColor.color(255, 85, 85); // Vermelho
    private static final TextColor WARNING = TextColor.color(255, 255, 85); // Amarelo
    private static final TextColor INFO = TextColor.color(170, 170, 170); // Cinza

    private Messages() {
    }

    /**
     * Cria o prefixo do plugin.
     */
    private static Component prefix() {
        return Component.text("")
                .append(Component.text("▌ ", SECONDARY))
                .append(Component.text(PREFIX, PRIMARY).decorate(TextDecoration.BOLD))
                .append(Component.text(" » ", INFO));
    }

    /**
     * Mensagem de sucesso.
     */
    public static void success(Player player, String message) {
        player.sendMessage(prefix()
                .append(Component.text("✓ ", SUCCESS))
                .append(Component.text(message, SUCCESS)));
    }

    /**
     * Mensagem de erro.
     */
    public static void error(Player player, String message) {
        player.sendMessage(prefix()
                .append(Component.text("✗ ", ERROR))
                .append(Component.text(message, ERROR)));
    }

    /**
     * Mensagem de aviso.
     */
    public static void warning(Player player, String message) {
        player.sendMessage(prefix()
                .append(Component.text("⚠ ", WARNING))
                .append(Component.text(message, WARNING)));
    }

    /**
     * Mensagem informativa.
     */
    public static void info(Player player, String message) {
        player.sendMessage(prefix()
                .append(Component.text("ℹ ", SECONDARY))
                .append(Component.text(message, NamedTextColor.WHITE)));
    }

    /**
     * Mensagem de dinheiro recebido.
     */
    public static void money(Player player, String amount, String reason) {
        player.sendMessage(prefix()
                .append(Component.text("💰 ", TextColor.color(0, 255, 127)))
                .append(Component.text("+", SUCCESS))
                .append(Component.text(amount, SUCCESS).decorate(TextDecoration.BOLD))
                .append(Component.text(" " + reason, INFO)));
    }

    /**
     * Mensagem de dinheiro gasto.
     */
    public static void spent(Player player, String amount, String reason) {
        player.sendMessage(prefix()
                .append(Component.text("💸 ", ERROR))
                .append(Component.text("-", ERROR))
                .append(Component.text(amount, ERROR).decorate(TextDecoration.BOLD))
                .append(Component.text(" " + reason, INFO)));
    }

    /**
     * Linha divisória bonita.
     */
    public static void divider(Player player) {
        player.sendMessage(Component.text("                                          ", INFO)
                .decorate(TextDecoration.STRIKETHROUGH));
    }

    /**
     * Cabeçalho de seção.
     */
    public static void header(Player player, String title) {
        divider(player);
        player.sendMessage(Component.text("  ")
                .append(Component.text("★ ", PRIMARY))
                .append(Component.text(title.toUpperCase(), PRIMARY).decorate(TextDecoration.BOLD))
                .append(Component.text(" ★", PRIMARY)));
        divider(player);
    }

    /**
     * Mensagem customizada com componente.
     */
    public static void send(Player player, Component message) {
        player.sendMessage(prefix().append(message));
    }

    /**
     * Mensagem em caixa bonita (para anúncios e destaques).
     */
    public static void box(Player player, String title, String... lines) {
        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("╔══════════════════════════════════╗", PRIMARY));
        player.sendMessage(Component.text("║  ", PRIMARY)
                .append(Component.text("★ " + title + " ★", SECONDARY).decorate(TextDecoration.BOLD)));
        player.sendMessage(Component.text("╠══════════════════════════════════╣", PRIMARY));
        for (String line : lines) {
            player.sendMessage(Component.text("║  ", PRIMARY)
                    .append(Component.text(line, NamedTextColor.WHITE)));
        }
        player.sendMessage(Component.text("╚══════════════════════════════════╝", PRIMARY));
        player.sendMessage(Component.empty());
    }

    /**
     * Mensagem de item (para mercado/loja).
     */
    public static void item(Player player, String itemName, double price, String seller) {
        player.sendMessage(prefix()
                .append(Component.text("📦 ", TextColor.color(255, 215, 0)))
                .append(Component.text(itemName, SECONDARY).decorate(TextDecoration.BOLD))
                .append(Component.text(" por ", INFO))
                .append(Component.text("$" + String.format("%.2f", price), SUCCESS).decorate(TextDecoration.BOLD))
                .append(Component.text(" - " + seller, INFO)));
    }

    /**
     * Mensagem de XP ganho.
     */
    public static void xp(Player player, int amount, String skill) {
        player.sendMessage(prefix()
                .append(Component.text("⭐ ", TextColor.color(138, 43, 226)))
                .append(Component.text("+" + amount + " XP ", TextColor.color(138, 43, 226))
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(skill, INFO)));
    }
}

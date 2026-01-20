package com.example.economia.features.generators;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import com.example.economia.features.economy.EconomyService;
import com.example.economia.features.gui.GuiTitles;
import com.example.economia.features.gui.GuiUtils;
import com.example.economia.features.messages.Messages;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class GeneratorsGui implements Listener {

    private final GeneratorService generatorService;
    private final EconomyService economyService;

    public GeneratorsGui(GeneratorService generatorService, EconomyService economyService) {
        this.generatorService = generatorService;
        this.economyService = economyService;
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GuiTitles.GENERATORS);

        int current = 10;

        for (GeneratorType type : GeneratorType.values()) {
            if (current > 16)
                break;

            inv.setItem(current, GuiUtils.item(
                    type.getBlockMaterial(),
                    type.getName(),
                    "§7Gera: " + type.getDropMaterial().name(),
                    "§7Intervalo: §f" + type.getIntervalSeconds() + "s",
                    "",
                    "§aPreço: §2$" + String.format("%,.0f", type.getPrice()),
                    "",
                    "§eClique para comprar!"));
            current++;
        }

        inv.setItem(26, GuiUtils.item(Material.BARRIER, "§cVoltar", "§7Voltar ao menu"));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals(GuiTitles.GENERATORS_TEXT))
            return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null)
            return;
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        // Voltar
        if (event.getSlot() == 26) {
            player.closeInventory();
            player.performCommand("painel");
            return;
        }

        // Comprar
        for (GeneratorType type : GeneratorType.values()) {
            if (event.getCurrentItem().getType() == type.getBlockMaterial()) {
                if (economyService.getBalance(player) >= type.getPrice()) {
                    economyService.withdraw(player, type.getPrice());
                    generatorService.createGenerator(player, type);
                    Messages.spent(player, "$" + String.format("%,.0f", type.getPrice()),
                            "na compra de um " + type.getName());
                    player.closeInventory();
                } else {
                    Messages.error(player, "Dinheiro insuficiente!");
                }
                return;
            }
        }
    }
}

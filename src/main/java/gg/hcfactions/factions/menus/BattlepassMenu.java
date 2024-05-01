package gg.hcfactions.factions.menus;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.battlepass.EBPState;
import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import gg.hcfactions.factions.models.battlepass.impl.BPTracker;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.menu.impl.GenericMenu;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.stream.Collectors;

public final class BattlepassMenu extends GenericMenu {
    @Getter public Factions plugin;

    public BattlepassMenu(Factions plugin, Player player) {
        super(plugin, player, "Battlepass", 6);
        this.plugin = plugin;
    }

    @Override
    public void open() {
        super.open();

        addUpdater(() -> {
            final List<BPObjective> activeObjectives = plugin.getBattlepassManager().getActiveObjectives();
            final BPTracker tracker = plugin.getBattlepassManager().getTracker(player);

            if (activeObjectives.isEmpty()) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "There are no active objectives");
                return;
            }

            clear();

            int dailyCursor = 10;
            int weeklyCursor = 37;

            for (final BPObjective obj : activeObjectives.stream().filter(obj -> obj.getState().equals(EBPState.DAILY)).collect(Collectors.toList())) {
                addItem(new Clickable(obj.getMenuItem(tracker), dailyCursor, click -> player.sendMessage(ChatColor.RESET + "You selected: " + obj.getIcon().getDisplayName())));
                dailyCursor += 3;
            }

            for (final BPObjective obj : activeObjectives.stream().filter(obj -> obj.getState().equals(EBPState.WEEKLY)).collect(Collectors.toList())) {
                addItem(new Clickable(obj.getMenuItem(tracker), weeklyCursor, click -> player.sendMessage(ChatColor.RESET + "You selected: " + obj.getIcon().getDisplayName())));
                weeklyCursor += 3;
            }

            fill(new ItemBuilder()
                    .setMaterial(Material.BLACK_STAINED_GLASS_PANE)
                    .setName(ChatColor.RESET + "")
                    .addEnchant(Enchantment.LUCK_OF_THE_SEA, 1)
                    .addFlag(ItemFlag.HIDE_ENCHANTS)
                    .build());
        }, 20L);
    }
}

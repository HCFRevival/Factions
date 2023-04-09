package gg.hcfactions.factions.menus;

import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.libs.bukkit.AresPlugin;
import gg.hcfactions.libs.bukkit.menu.impl.ConfirmationMenu;
import org.bukkit.entity.Player;

public final class DisbandConfirmationMenu extends ConfirmationMenu {
    public DisbandConfirmationMenu(AresPlugin plugin, Player player, IFaction faction, Runnable acceptTask) {
        super(plugin, player, "Disband " + faction.getName() + "?", acceptTask);
    }
}

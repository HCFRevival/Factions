package gg.hcfactions.factions.menus;

import gg.hcfactions.libs.bukkit.AresPlugin;
import gg.hcfactions.libs.bukkit.menu.impl.GenericMenu;
import org.bukkit.entity.Player;

public final class StatsMenu extends GenericMenu {
    public StatsMenu(AresPlugin plugin, Player player, String holderName) {
        super(plugin, player, holderName + "'s Stats", 1);
    }
}

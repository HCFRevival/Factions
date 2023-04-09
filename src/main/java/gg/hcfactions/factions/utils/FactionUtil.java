package gg.hcfactions.factions.utils;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.bukkit.utils.Players;
import org.bukkit.entity.Player;

public final class FactionUtil {
    public static void cleanPlayer(Factions plugin, Player player) {
        Players.resetHealth(player);
        player.teleport(plugin.getConfiguration().getOverworldSpawn());
    }
}

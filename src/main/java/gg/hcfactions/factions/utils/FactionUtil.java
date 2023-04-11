package gg.hcfactions.factions.utils;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.utils.Players;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class FactionUtil {
    public static void cleanPlayer(Factions plugin, Player player) {
        Players.resetHealth(player);
        player.teleport(plugin.getConfiguration().getOverworldSpawn());
    }

    public static void teleportToSafety(Factions plugin, Player player) {
        final PLocatable location = new PLocatable(player);

        new Scheduler(plugin).async(() -> {
            while (plugin.getClaimManager().getClaimAt(location) != null) {
                location.setX(location.getX() + 1.0);
                location.setZ(location.getZ() + 1.0);
            }

            new Scheduler(plugin).sync(() -> {
                location.setY(Objects.requireNonNull(location.getBukkitLocation().getWorld()).getHighestBlockYAt(location.getBukkitLocation()));
                Players.teleportWithVehicle(plugin, player, location.getBukkitLocation());
            }).run();
        }).run();
    }
}

package gg.hcfactions.factions.models.waypoint;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface IWaypoint {
    UUID getViewingFactionId();
    String getName();
    Location getLocation();
    int getColor();
    LCWaypoint getLegacyWaypoint();
    List<UUID> getViewers();

    default LCWaypoint buildLegacyWaypoint() {
        return new LCWaypoint(getName(), getLocation(), getColor(), true, true);
    }

    default boolean canSee(UUID uniqueId) {
        return getViewers().contains(uniqueId);
    }

    default boolean canSee(Player player) {
        return getViewers().contains(player.getUniqueId());
    }

    default void send(Player player, boolean legacy) {
        if (canSee(player)) {
            return;
        }

        if (legacy && getLegacyWaypoint() != null) {
            if (!LunarClientAPI.getInstance().isRunningLunarClient(player)) {
                return;
            }

            LunarClientAPI.getInstance().sendWaypoint(player, getLegacyWaypoint());
            getViewers().add(player.getUniqueId());
        }
    }

    default void hide(Player player, boolean legacy) {
        if (!canSee(player)) {
            return;
        }

        if (legacy && getLegacyWaypoint() != null) {
            if (!LunarClientAPI.getInstance().isRunningLunarClient(player)) {
                return;
            }

            LunarClientAPI.getInstance().removeWaypoint(player, getLegacyWaypoint());
            getViewers().remove(player.getUniqueId());
        }
    }

    default void hideAll(boolean legacy) {
        getViewers().forEach(viewerId -> {
            final Player player = Bukkit.getPlayer(viewerId);

            if (player != null) {
                if (legacy && LunarClientAPI.getInstance().isRunningLunarClient(player)) {
                    LunarClientAPI.getInstance().removeWaypoint(player, getLegacyWaypoint());
                }
            }
        });

        getViewers().clear();
    }
}

package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public interface IDPSEntity {
    /**
     * @return DPS Event this entity is tied to
     */
    DPSEvent getEvent();

    /**
     * @return Valid DPS event entity types
     */
    EDPSEntityType getEntityType();

    /**
     * @return Return the internal NPS entity object
     */
    CraftEntity getEntity();

    /**
     * @return Return the origin spawn location
     */
    Location getOrigin();

    /**
     * @return Location this entity was suspended at upon chunk unload
     */
    Location getSuspendedLocation();

    /**
     * This function is needed to flag a recently unloaded chunk to not suspend the
     * entity again as chunks seem to reload when a player disconnects in it.
     *
     * @return If true, this entity has been suspended within the last second
     */
    boolean isRecentlySuspended();

    /**
     * Mark this entity as recently suspended so that it can not be suspended again
     * @param b Suspended value
     */
    void setRecentlySuspended(boolean b);

    /**
     * @return True if this entity is currently suspended
     */
    default boolean isSuspended() {
        return getSuspendedLocation() != null;
    }

    /**
     * Performs pre-flight configuration for the entity that is general
     * to all DPS Check entity types.
     */
    default void setup() {
        getEntity().setCustomName(ChatColor.DARK_RED + "🗡" + " " + ChatColor.RED + DPSEvent.getRandomEntityName());
        getEntity().setCustomNameVisible(true);
        getEntity().teleport(getOrigin(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    /**
     * Sets the suspended location this entity will be respawned at
     * @param location Location
     */
    void setSuspendedLocation(Location location);

    /**
     * Spawns this entity in to the world
     */
    void spawn();

    /**
     * Removes this entity from the world
     */
    void despawn();

    default void suspend() {
        if (isRecentlySuspended()) {
            return;
        }

        if (isSuspended()) {
            throw new IllegalStateException("Suspended location was not null when it was expected to be");
        }

        final Location location = getEntity().getLocation();
        setSuspendedLocation(location);
        despawn();
    }

    default int getClosestNodeIndex(BLocatable currentLocation, List<BLocatable> nodes) {
        double closestDist = 0.0D;
        int closestIndex = 0;

        for (int i = 0; i < nodes.size(); i++) {
            final BLocatable node = nodes.get(i);
            final double dist = currentLocation.getDistance(node);

            if (dist < closestDist) {
                closestIndex = i;
                closestDist = dist;
            }
        }

        return closestIndex;
    }
}

package gg.hcfactions.factions.models.logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public interface ICombatLogger {
    /**
     * @return Bukkit UUID
     */
    UUID getOwnerId();

    /**
     * @return Bukkit Username
     */
    String getOwnerUsername();

    /**
     * @return Combat Logger item inventory
     */
    List<ItemStack> getLoggerInventory();

    /**
     * @return Duration the player will be deathbanned if their combat logger is slain
     */
    int getBanDuration();

    /**
     * Spawns this entity in to the world
     */
    void spawn();

    /**
     * Reapply contents to a provided player
     * @param player Player
     */
    void reapply(Player player);

    /**
     * Drop the contents of this combat logger at the provided location
     * @param location Location to drop items at
     */
    default void dropItems(Location location) {
        getLoggerInventory().forEach(i -> Objects.requireNonNull(location.getWorld()).dropItem(location, i));
    }
}

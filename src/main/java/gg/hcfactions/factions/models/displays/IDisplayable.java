package gg.hcfactions.factions.models.displays;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public interface IDisplayable {
    /**
     * @return Factions Plugin
     */
    Factions getPlugin();

    /**
     * @return Unique Identifier (for config)
     */
    UUID getUniqueId();

    /**
     * @return Origin position for the hologram to be displayed at
     */
    PLocatable getOrigin();

    /**
     * @return Title of the display
     */
    Component getTitle();

    /**
     * Calls an update on the display
     */
    void update();

    /**
     * Calls to spawn the hologram at the provided origin position
     */
    void spawn();

    /**
     * Calls to despawn the hologram
     */
    void despawn();

    /**
     * @return If true, this hologram can be seen by at least one player on the server
     */
    default boolean canBeSeen() {
        for (Entity entity : Objects.requireNonNull(getOrigin().getBukkitLocation().getWorld()).getNearbyEntities(getOrigin().getBukkitLocation(), 16, 16, 16)) {
            if (entity instanceof Player) {
                return true;
            }
        }

        return false;
    }
}

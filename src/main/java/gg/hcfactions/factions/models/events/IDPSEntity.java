package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import org.bukkit.ChatColor;

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
     * Performs pre-flight configuration for the entity that is general
     * to all DPS Check entity types.
     * @param entity DPS Entity to apply changes towards
     */
    default void setup(org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity entity) {
        entity.setCustomName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "DPS CHECK");
        entity.setCustomNameVisible(true);
    }

    /**
     * Spawns this entity in to the world
     */
    void spawn();

    /**
     * Removes this entity from the world
     */
    void despawn();
}

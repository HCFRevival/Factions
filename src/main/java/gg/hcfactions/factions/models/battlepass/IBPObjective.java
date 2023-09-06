package gg.hcfactions.factions.models.battlepass;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface IBPObjective {
    Factions getPlugin();

    /**
     * @return Unique identifier to reference this objective
     */
    String getIdentifier();

    /**
     * @return Current objective state
     */
    EBPState getState();

    /**
     * @return Objective Type
     */
    EBPObjectiveType getObjectiveType();

    /**
     * @return Block requirement associated to this objective
     */
    Material getBlockRequirement();

    /**
     * @return Entity requirement associated to this objective
     */
    EntityType getEntityRequirement();

    /**
     * @return Claim requirement associated to this object
     *         NOTE: This is the Faction ID that owns the claims,
     *               not the claim ID itself.
     */
    UUID getClaimRequirement();

    /**
     * @return World Environment requirement associated to this objective
     */
    World.Environment getWorldRequirement();

    /**
     * @return Class requirement associated to this objective
     */
    IClass getClassRequirement();

    /**
     * @return The amount needed to mark this objective as "complete"
     */
    int getAmountRequirement();

    default boolean hasEntityRequirement() {
        return getEntityRequirement() != null;
    }

    default boolean hasBlockRequirement() {
        return getBlockRequirement() != null;
    }

    default boolean hasClaimRequirement() {
        return getClaimRequirement() != null;
    }

    default boolean hasWorldRequirement() {
        return getWorldRequirement() != null;
    }

    default boolean hasClassRequirement() {
        return getClassRequirement() != null;
    }

    /**
     * Checks if the provided parameters match the requirements
     * needed to add a value to this objective's tracker
     *
     * @param player Player
     * @param entity Entity associated to this request, like the entity being killed
     * @return True if all requirements are met
     */
    default boolean meetsRequirement(Player player, Entity entity) {
        if (getEntityRequirement() == null || !getEntityRequirement().equals(entity.getType())) {
            return false;
        }

        if (hasClaimRequirement()) {
            final Claim insideClaim = getPlugin().getClaimManager().getClaimAt(new PLocatable(entity));

            if (insideClaim == null || !insideClaim.getOwner().equals(getClaimRequirement())) {
                return false;
            }
        }

        if (hasWorldRequirement() && !entity.getWorld().getEnvironment().equals(getWorldRequirement())) {
            return false;
        }

        if (hasClassRequirement()) {
            if (!getPlugin().getClassManager().getCurrentClass(player).equals(getClassRequirement())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the provided parameters match the requirements
     * needed to add a value to this objective's tracker.
     *
     * @param player Player
     * @param location Location the event occurred at, like a block broken position
     * @return True if all requirements are met
     */
    default boolean meetsRequirement(Player player, Location location) {
        if (hasBlockRequirement()) {
            final Block block = location.getBlock();

            if (!block.getType().equals(getBlockRequirement())) {
                return false;
            }
        }

        if (hasClaimRequirement()) {
            final Claim insideClaim = getPlugin().getClaimManager().getClaimAt(new BLocatable(location.getBlock()));

            if (insideClaim == null || !insideClaim.getOwner().equals(getClaimRequirement())) {
                return false;
            }
        }

        if (hasWorldRequirement() && (location.getWorld() == null || !location.getWorld().getEnvironment().equals(getWorldRequirement()))) {
            return false;
        }

        if (hasClassRequirement()) {
            if (!getPlugin().getClassManager().getCurrentClass(player).equals(getClassRequirement())) {
                return false;
            }
        }

        return true;
    }
}

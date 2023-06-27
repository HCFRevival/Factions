package gg.hcfactions.factions.models.classes;

import gg.hcfactions.libs.base.util.Time;
import org.bukkit.Material;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IHoldableClass {
    List<IClassHoldable> getHoldables();
    int getHoldableUpdateRate();

    void setHoldableUpdateRate(int rate);

    default Optional<IClassHoldable> getHoldable(Material mat) {
        return getHoldables().stream().filter(h -> h.getMaterial().equals(mat)).findFirst();
    }

    default void resetHoldables(UUID uniqueId) {
        getHoldables().forEach(h -> h.getCurrentHolders().remove(uniqueId));
    }

    default boolean shouldReapplyHoldable(UUID uniqueId, IClassHoldable holdable) {
        final long prev = holdable.getTimeSinceLastHold(uniqueId);

        if (prev <= -1L) {
            return true;
        }

        final long future = Time.now() + (getHoldableUpdateRate() * 1000L);

        return future <= prev;
    }
}

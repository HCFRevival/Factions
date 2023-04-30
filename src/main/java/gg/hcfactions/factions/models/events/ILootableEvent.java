package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.libs.base.util.Time;

import java.util.Map;

public interface ILootableEvent {
    /**
     * @return Timestamps when each loot tier is supposed to unlock
     */
    Map<EPalaceLootTier, Long> getLootUnlockTimes();

    /**
     * @return Epoch time (millis) event should restock at
     */
    long getNextRestockTime();

    /**
     * @param time Time for next restock (in millis)
     */
    void setNextRestockTime(long time);

    /**
     * Trigger an event restock
     */
    void restock();

    /**
     * @return Time (in millis) until next restock
     */
    default long getTimeUntilNextRestock() {
        return getNextRestockTime() - Time.now();
    }

    /**
     * Checks to see if the provided Palace Loot Chest is unlocked
     * @param chest Chest
     * @return True if chest is unlocked
     */
    default boolean isChestUnlocked(PalaceLootChest chest) {
        if (getLootUnlockTimes().isEmpty() || !getLootUnlockTimes().containsKey(chest.getLootTier())) {
            return true;
        }

        final long unlock = getLootUnlockTimes().get(chest.getLootTier());
        return unlock <= Time.now();
    }
}

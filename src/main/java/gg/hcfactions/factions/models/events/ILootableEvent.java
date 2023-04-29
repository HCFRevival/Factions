package gg.hcfactions.factions.models.events;

import gg.hcfactions.libs.base.util.Time;

public interface ILootableEvent {
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
}

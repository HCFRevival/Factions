package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.models.events.impl.DPSEventConfig;
import gg.hcfactions.factions.models.events.impl.DPSSession;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;

import java.util.List;
import java.util.Random;

public interface IDPSEvent {
    /**
     * Active DPS Session
     * @return DPSSession
     */
    DPSSession getSession();

    /**
     * DPS Event configuration
     * @return DPSEventConfig
     */
    DPSEventConfig getEventConfig();

    /**
     * @return Returns a list of all spawnpoints the DPS entity can spawn at
     */
    List<BLocatable> getSpawnpoints();

    /**
     * @return Returns a random spawnpoint from the list of possible spawnpoints
     */
    default BLocatable getRandomSpawnpoint() {
        final Random random = new Random();
        return getSpawnpoints().get(random.nextInt(getSpawnpoints().size() - 1));
    }

    /**
     * Returns true if this event should close
     * @return True if event should close and determine a winner
     */
    default boolean shouldEnd() {
        if (getSession() == null || !getSession().isActive()) {
            return false;
        }

        return getSession().getEventEndTimestamp() <= Time.now();
    }

    /**
     * Start this event with specified parameters
     * @param entityType DPS entity type
     * @param duration Duration (in millis) this event will last before closing
     * @param tokenReward Amount of tokens that will be rewarded for winning the event
     */
    void startEvent(EDPSEntityType entityType, long duration, int tokenReward);

    /**
     * Start this event with default values
     */
    void startEvent();

    /**
     * Complete the event and reward the highest DPS team
     */
    void captureEvent();

    /**
     * Stop this event without rewarding any player
     */
    void stopEvent();
}

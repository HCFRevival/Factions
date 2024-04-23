package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;

public interface ICaptureEvent {
    /**
     * @return Region parameters
     */
    CaptureRegion getCaptureRegion();

    /**
     * @return Capture Event Config
     */
    ICaptureEventConfig getEventConfig();

    /**
     * @param region Set the region for capturing the event
     */
    void setCaptureRegion(CaptureRegion region);

    /**
     * @param faction Capturing faction
     */
    void captureEvent(PlayerFaction faction);

    /**
     * Start the event with default values
     */
    void startEvent();

    /**
     * Start the event with a specified capture event config
     * @param conf CaptureEventConfig
     */
    void startEvent(CaptureEventConfig conf);

    /**
     * Start this event with the specified parameters
     * @param ticketsNeededToWin Tickets needed for the event to be considered captured
     * @param timerDuration Timer between each ticket capture
     * @param tokenReward Tokens that will be rewarded to the capturing faction
     * @param tickCheckpointInterval Tick checkpoint interval
     * @param contestedThreshold Threshold for an event to be considered contested
     */
    void startEvent(int ticketsNeededToWin, int timerDuration, int tokenReward, int tickCheckpointInterval, int contestedThreshold);

    /**
     * Stop the event without rewarding any player
     */
    void stopEvent();
}

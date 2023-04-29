package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;

public interface ICaptureEvent {
    /**
     * @return Location of the loot chest
     */
    BLocatable getCaptureChestLocation();

    /**
     * @return Region parameters
     */
    CaptureRegion getCaptureRegion();

    /**
     * @return Capture Event Config
     */
    ICaptureEventConfig getEventConfig();

    /**
     * @param block Set the location of the loot chest
     */
    void setCaptureChestLocation(BLocatable block);

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
     * Start this event with the specified parameters
     * @param ticketsNeededToWin Tickets needed for the event to be considered captured
     * @param timerDuration Timer between each ticket capture
     */
    void startEvent(int ticketsNeededToWin, int timerDuration);

    /**
     * Stop the event without rewarding any player
     */
    void stopEvent();
}

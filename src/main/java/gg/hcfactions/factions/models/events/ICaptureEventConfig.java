package gg.hcfactions.factions.models.events;

public interface ICaptureEventConfig {
    /**
     * @return Tickets needed to be considered the winner of the event
     */
    int defaultTicketsNeededToWin();

    /**
     * @return Time between each tick
     */
    int defaultTimerDuration();

    /**
     * @return Max time this event can be up for
     */
    int getMaxLifespan();
}

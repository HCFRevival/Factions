package gg.hcfactions.factions.models.events;

public interface ICaptureEventConfig {
    /**
     * @return Tickets needed to be considered the winner of the event
     */
    int getDefaultTicketsNeededToWin();

    /**
     * @return Time between each tick
     */
    int getDefaultTimerDuration();

    /**
     * @return Max time this event can be up for
     */
    int getMaxLifespan();

    /**
     * @return Tokens that will be rewarded for capturing the event
     */
    int getTokenReward();

    /**
     * @return The amount of tick intervals to use as a 'checkpoint'
     * for factions that exceed the number.
     *
     * Example: If ticks is set to 4, there will be stop places
     * at each 25% mark of the total tickets needed to win.
     */
    int getTickCheckpointInterval();
}

package gg.hcfactions.factions.models.events.impl;

import lombok.Getter;

public final class ConquestEventConfig extends CaptureEventConfig {
    @Getter public int ticketsPerTick;

    public ConquestEventConfig(
            int defaultTicketsNeededToWin,
            int defaultTimerDuration,
            int maxLifespan,
            int tokenReward,
            int ticketsPerTick) {
        super(defaultTicketsNeededToWin, defaultTimerDuration, maxLifespan, tokenReward, 0);

        this.ticketsPerTick = ticketsPerTick;
    }
}

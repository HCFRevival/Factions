package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.factions.models.events.ICaptureEventConfig;
import lombok.Getter;

public class CaptureEventConfig implements ICaptureEventConfig {
    @Getter public int defaultTicketsNeededToWin;
    @Getter public int defaultTimerDuration;
    @Getter public int maxLifespan;
    @Getter public int tokenReward;
    @Getter public int tickCheckpointInterval;

    public CaptureEventConfig(int ticketsToWin, int timerDuration, int tokenReward) {
        this.defaultTicketsNeededToWin = ticketsToWin;
        this.defaultTimerDuration = timerDuration;
        this.maxLifespan = -1;
        this.tokenReward = tokenReward;
        this.tickCheckpointInterval = -1;
    }

    public CaptureEventConfig(int ticketsToWin, int timerDuration, int tokenReward, int tickCheckpointInterval) {
        this(ticketsToWin, timerDuration, tokenReward);
        this.tickCheckpointInterval = tickCheckpointInterval;
    }
}

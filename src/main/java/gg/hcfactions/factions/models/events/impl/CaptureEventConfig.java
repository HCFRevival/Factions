package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.factions.models.events.ICaptureEventConfig;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CaptureEventConfig implements ICaptureEventConfig {
    public int defaultTicketsNeededToWin;
    public int defaultTimerDuration;
    public int maxLifespan;
    public int tokenReward;
    public int tickCheckpointInterval;
    public int contestedThreshold;
    public int onlinePlayerLimit;
    public boolean majorityTurnoverEnabled;
    public boolean suddenDeathEnabled;

    public CaptureEventConfig() {
        this.defaultTicketsNeededToWin = 20;
        this.defaultTimerDuration = 60;
        this.maxLifespan = -1;
        this.tokenReward = 100;
        this.tickCheckpointInterval = -1;
        this.contestedThreshold = 0;
        this.onlinePlayerLimit = -1;
    }

    public CaptureEventConfig(int ticketsToWin, int timerDuration, int tokenReward) {
        this.defaultTicketsNeededToWin = ticketsToWin;
        this.defaultTimerDuration = timerDuration;
        this.maxLifespan = -1;
        this.tokenReward = tokenReward;
        this.tickCheckpointInterval = -1;
        this.contestedThreshold = 0;
        this.onlinePlayerLimit = -1;
    }

    public CaptureEventConfig(
            int ticketsToWin,
            int timerDuration,
            int tokenReward,
            int tickCheckpointInterval,
            int contestedThreshold,
            int onlinePlayerLimit
    ) {
        this(ticketsToWin, timerDuration, tokenReward);
        this.tickCheckpointInterval = tickCheckpointInterval;
        this.contestedThreshold = contestedThreshold;
        this.onlinePlayerLimit = onlinePlayerLimit;
    }
}

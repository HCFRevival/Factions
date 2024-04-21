package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.factions.models.events.ICaptureEventConfig;
import lombok.Getter;
import lombok.Setter;

public class CaptureEventConfig implements ICaptureEventConfig {
    @Getter @Setter public int defaultTicketsNeededToWin;
    @Getter @Setter public int defaultTimerDuration;
    @Getter @Setter public int maxLifespan;
    @Getter @Setter public int tokenReward;
    @Getter @Setter public int tickCheckpointInterval;
    @Getter @Setter public int contestedThreshold;
    @Getter @Setter public boolean majorityTurnoverEnabled;
    @Getter @Setter public boolean suddenDeathEnabled;

    public CaptureEventConfig() {
        this.defaultTicketsNeededToWin = 20;
        this.defaultTimerDuration = 60;
        this.maxLifespan = -1;
        this.tokenReward = 100;
        this.tickCheckpointInterval = -1;
        this.contestedThreshold = 0;
    }

    public CaptureEventConfig(int ticketsToWin, int timerDuration, int tokenReward) {
        this.defaultTicketsNeededToWin = ticketsToWin;
        this.defaultTimerDuration = timerDuration;
        this.maxLifespan = -1;
        this.tokenReward = tokenReward;
        this.tickCheckpointInterval = -1;
        this.contestedThreshold = 0;
    }

    public CaptureEventConfig(int ticketsToWin, int timerDuration, int tokenReward, int tickCheckpointInterval, int contestedThreshold) {
        this(ticketsToWin, timerDuration, tokenReward);
        this.tickCheckpointInterval = tickCheckpointInterval;
        this.contestedThreshold = contestedThreshold;
    }
}

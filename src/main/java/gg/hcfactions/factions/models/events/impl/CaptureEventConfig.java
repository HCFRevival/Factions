package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.factions.models.events.ICaptureEventConfig;
import lombok.Getter;

public record CaptureEventConfig(@Getter int defaultTicketsNeededToWin,
                                 @Getter int defaultTimerDuration,
                                 @Getter int maxLifespan,
                                 @Getter int tokenReward) implements ICaptureEventConfig {
}

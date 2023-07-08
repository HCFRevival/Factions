package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.factions.models.events.ICaptureEventConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class CaptureEventConfig implements ICaptureEventConfig {
    @Getter public int defaultTicketsNeededToWin;
    @Getter public int defaultTimerDuration;
    @Getter public int maxLifespan;
    @Getter public int tokenReward;
}

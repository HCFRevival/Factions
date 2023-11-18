package gg.hcfactions.factions.models.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class DPSEventConfig {
    @Getter public int defaultDuration;
    @Getter public int tokenReward;
}

package gg.hcfactions.factions.stats;

import lombok.Getter;

public final class StatsConfig {
    @Getter public double mapNumber;

    public StatsConfig() {
        this.mapNumber = 0.0D;
    }

    public StatsConfig(double mapNumber) {
        this.mapNumber = mapNumber;
    }
}

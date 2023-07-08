package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.libs.base.timer.impl.GenericTimer;
import lombok.Getter;

public final class ConquestTimer extends GenericTimer {
    @Getter public final ConquestZone zone;

    public ConquestTimer(ConquestZone zone, int seconds) {
        super(seconds);
        this.zone = zone;
    }

    public void finish() {
        zone.getEvent().getSession().tick(zone);
    }
}

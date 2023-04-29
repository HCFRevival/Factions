package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.libs.base.timer.impl.GenericTimer;
import lombok.Getter;

public class KOTHTimer extends GenericTimer {
    @Getter public final KOTHEvent event;

    public KOTHTimer(KOTHEvent event, int seconds) {
        super(seconds);
        this.event = event;
    }

    public void finish() {
        event.getSession().tick(event.getSession().getCapturingFaction());
    }
}

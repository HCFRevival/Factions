package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.factions.models.events.IMultiCaptureZone;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import lombok.Setter;

public final class ConquestZone implements IMultiCaptureZone {
    @Getter public ConquestEvent event;
    @Getter @Setter public ConquestTimer timer;
    @Getter @Setter public boolean contested;
    @Getter public final String name;
    @Getter public final String displayName;
    @Getter public CaptureRegion captureRegion;
    @Getter @Setter public PlayerFaction capturingFaction;

    public ConquestZone(ConquestEvent event, String name, String displayName, CaptureRegion region) {
        this.event = event;
        this.timer = null;
        this.name = name;
        this.displayName = displayName;
        this.captureRegion = region;
        this.capturingFaction = null;
    }
}

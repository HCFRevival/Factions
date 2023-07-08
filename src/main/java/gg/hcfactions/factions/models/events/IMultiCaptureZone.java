package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.events.impl.ConquestTimer;

public interface IMultiCaptureZone {
    ConquestTimer getTimer();
    String getName();
    String getDisplayName();
    CaptureRegion getCaptureRegion();
}

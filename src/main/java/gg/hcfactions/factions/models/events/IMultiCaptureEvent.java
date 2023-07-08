package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.models.events.impl.ConquestSession;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;

public interface IMultiCaptureEvent {
    ConquestSession getSession();

    void captureEvent(PlayerFaction faction);
    void startEvent();
    void startEvent(int ticketsNeededToWin, int timerDuration, int tokenReward, int ticketsPerTick);
    void stopEvent();
}

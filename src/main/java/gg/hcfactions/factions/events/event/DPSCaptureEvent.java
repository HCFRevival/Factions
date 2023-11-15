package gg.hcfactions.factions.events.event;

import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class DPSCaptureEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();

    @Getter public final DPSEvent event;
    @Getter public final PlayerFaction capturingFaction;

    public DPSCaptureEvent(DPSEvent event, PlayerFaction capturingFaction) {
        this.event = event;
        this.capturingFaction = capturingFaction;
    }

    @NotNull @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

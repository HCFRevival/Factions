package gg.hcfactions.factions.events.event;

import gg.hcfactions.factions.models.events.IEvent;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class EventStartEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public IEvent event;

    public EventStartEvent(IEvent event) {
        this.event = event;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

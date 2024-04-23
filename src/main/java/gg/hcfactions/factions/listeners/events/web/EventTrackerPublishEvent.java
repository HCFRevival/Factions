package gg.hcfactions.factions.listeners.events.web;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class EventTrackerPublishEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final String url;

    public EventTrackerPublishEvent(String url) {
        this.url = url;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

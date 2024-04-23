package gg.hcfactions.factions.listeners.events.faction;

import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class FactionTicketLossEvent extends Event {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter public final IEvent event;
    @Getter public final PlayerFaction faction;
    @Getter public final int newTicketCount;
    @Getter public final int oldTicketCount;

    public FactionTicketLossEvent(IEvent event, PlayerFaction faction, int newTicketCount, int oldTicketCount) {
        this.event = event;
        this.faction = faction;
        this.newTicketCount = newTicketCount;
        this.oldTicketCount = oldTicketCount;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

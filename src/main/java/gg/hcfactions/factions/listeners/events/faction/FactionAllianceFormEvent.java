package gg.hcfactions.factions.listeners.events.faction;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class FactionAllianceFormEvent extends Event implements Cancellable {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter public final PlayerFaction initiator;
    @Getter public final PlayerFaction otherFaction;
    @Getter @Setter public boolean cancelled;

    public FactionAllianceFormEvent(PlayerFaction initiator, PlayerFaction otherFaction) {
        this.initiator = initiator;
        this.otherFaction = otherFaction;
        this.cancelled = false;
    }

    @NonNull
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
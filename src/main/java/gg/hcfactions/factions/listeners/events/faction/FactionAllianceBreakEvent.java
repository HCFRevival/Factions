package gg.hcfactions.factions.listeners.events.faction;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class FactionAllianceBreakEvent extends Event {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter public final PlayerFaction initiator;
    @Getter public final PlayerFaction otherFaction;

    public FactionAllianceBreakEvent(PlayerFaction initiator, PlayerFaction otherFaction) {
        this.initiator = initiator;
        this.otherFaction = otherFaction;
    }

    @NonNull
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}

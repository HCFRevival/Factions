package gg.hcfactions.factions.listeners.events.faction;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class FactionFocusEvent extends Event implements Cancellable {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter public PlayerFaction faction;
    @Getter public Player player;
    @Getter public Player focusedPlayer;
    @Getter @Setter public boolean cancelled;

    public FactionFocusEvent(PlayerFaction faction, Player initiated, Player focusedPlayer) {
        this.faction = faction;
        this.player = initiated;
        this.focusedPlayer = focusedPlayer;
        this.cancelled = false;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

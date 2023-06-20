package gg.hcfactions.factions.listeners.events.faction;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class FactionUnfocusEvent extends Event {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter public PlayerFaction faction;
    @Getter public Player focusedPlayer;

    public FactionUnfocusEvent(PlayerFaction faction, Player focused) {
        this.faction = faction;
        this.focusedPlayer = focused;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}


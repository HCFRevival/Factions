package gg.hcfactions.factions.listeners.events.faction;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class FactionLeaveEvent extends PlayerEvent {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter public PlayerFaction faction;
    @Getter public Reason reason;

    public FactionLeaveEvent(Player who, PlayerFaction faction, Reason reason) {
        super(who);
        this.faction = faction;
        this.reason = reason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public enum Reason {
        LEAVE, KICK
    }
}

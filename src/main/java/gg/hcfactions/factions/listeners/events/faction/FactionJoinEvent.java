package gg.hcfactions.factions.listeners.events.faction;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class FactionJoinEvent extends PlayerEvent {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter public PlayerFaction faction;

    public FactionJoinEvent(Player who, PlayerFaction faction) {
        super(who);
        this.faction = faction;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

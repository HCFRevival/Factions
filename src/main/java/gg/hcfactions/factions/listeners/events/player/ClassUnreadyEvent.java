package gg.hcfactions.factions.listeners.events.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class ClassUnreadyEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();

    public ClassUnreadyEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

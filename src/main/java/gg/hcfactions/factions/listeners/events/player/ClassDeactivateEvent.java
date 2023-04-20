package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.classes.IClass;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class ClassDeactivateEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final IClass playerClass;

    public ClassDeactivateEvent(Player who, IClass playerClass) {
        super(who);
        this.playerClass = playerClass;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.classes.IClass;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class ClassActivateEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final IClass playerClass;
    @Getter @Setter public boolean message;
    @Getter @Setter public boolean cancelled;

    public ClassActivateEvent(Player who, IClass playerClass) {
        super(who);
        this.playerClass = playerClass;
        this.message = true;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

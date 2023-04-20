package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.classes.IClass;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class ClassReadyEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final IClass playerClass;
    @Getter @Setter public boolean messagePrinted;
    @Getter @Setter public boolean cancelled;

    public ClassReadyEvent(Player who, IClass playerClass) {
        super(who);
        this.playerClass = playerClass;
        this.messagePrinted = true;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

package gg.hcfactions.factions.listeners.events.player;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import gg.hcfactions.factions.models.classes.impl.Bard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;
import java.util.UUID;

public final class ConsumeClassItemEvent extends Event implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Player player;
    @Getter public final IClass playerClass;
    @Getter public final Map<UUID, Boolean> affectedPlayers;
    @Getter public final IConsumeable consumable;
    @Getter @Setter public boolean cancelled;

    public ConsumeClassItemEvent(Player player, IClass playerClass, IConsumeable consumable) {
        this.player = player;
        this.playerClass = playerClass;
        this.affectedPlayers = Maps.newHashMap();
        this.consumable = consumable;
        this.cancelled = false;
    }

    /**
     * Handy utility method to check if this event is fired by a player using Bard
     * @return True if player is using Bard class
     */
    public boolean isBard() {
        return playerClass instanceof Bard;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

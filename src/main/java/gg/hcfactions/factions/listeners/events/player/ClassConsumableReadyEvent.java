package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.classes.IConsumeable;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class ClassConsumableReadyEvent extends Event {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Player player;
    @Getter public final IConsumeable consumable;

    public ClassConsumableReadyEvent(Player player, IConsumeable consumable) {
        this.player = player;
        this.consumable = consumable;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

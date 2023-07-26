package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.classes.impl.Tank;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class TankShieldReadyEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Tank tankClass;
    @Getter @Setter public boolean cancelled;

    public TankShieldReadyEvent(Player who, Tank tankClass) {
        super(who);
        this.tankClass = tankClass;
        this.cancelled = false;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

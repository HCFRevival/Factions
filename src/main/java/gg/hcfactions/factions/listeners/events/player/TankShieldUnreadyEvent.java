package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.classes.impl.Tank;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class TankShieldUnreadyEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Tank tankClass;

    public TankShieldUnreadyEvent(Player who, Tank tankClass) {
        super(who);
        this.tankClass = tankClass;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

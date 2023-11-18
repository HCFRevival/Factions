package gg.hcfactions.factions.events.event;

import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class DPSDamageEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final DPSEvent event;
    @Getter public final int damage;

    public DPSDamageEvent(DPSEvent event, Player who, int damage) {
        super(who);
        this.event = event;
        this.damage = damage;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

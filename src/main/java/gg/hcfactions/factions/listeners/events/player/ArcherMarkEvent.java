package gg.hcfactions.factions.listeners.events.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class ArcherMarkEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final LivingEntity attacked;
    @Getter @Setter public int ticks;
    @Getter @Setter public boolean cancelled;

    public ArcherMarkEvent(Player who, LivingEntity attacked, int tickDuration) {
        super(who);
        this.attacked = attacked;
        this.ticks = tickDuration;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

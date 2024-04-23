package gg.hcfactions.factions.listeners.events.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class DiverPierceEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final LivingEntity attacked;
    @Getter @Setter public double damage;
    @Getter public final double distance;
    @Getter @Setter public boolean cancelled;

    public DiverPierceEvent(Player who, LivingEntity attacked, double damage, double distance) {
        super(who);
        this.attacked = attacked;
        this.damage = damage;
        this.distance = distance;
        this.cancelled = false;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

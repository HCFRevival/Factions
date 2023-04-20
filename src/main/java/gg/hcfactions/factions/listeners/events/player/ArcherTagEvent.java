package gg.hcfactions.factions.listeners.events.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class ArcherTagEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Player attacked;
    @Getter public final double distance;
    @Getter public final int hitCount;
    @Getter @Setter public boolean cancelled;

    public ArcherTagEvent(Player who, Player attacked, double distance, int hitCount) {
        super(who);
        this.attacked = attacked;
        this.distance = distance;
        this.hitCount = hitCount;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

package gg.hcfactions.factions.listeners.events.world;

import gg.hcfactions.factions.models.boss.IBossEntity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class BossSpawnEvent extends Event implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final IBossEntity entity;
    @Getter public final Location spawnLocation;
    @Getter @Setter public boolean cancelled;

    public BossSpawnEvent(IBossEntity entity, Location spawnLocation) {
        this.entity = entity;
        this.spawnLocation = spawnLocation;
        this.cancelled = false;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

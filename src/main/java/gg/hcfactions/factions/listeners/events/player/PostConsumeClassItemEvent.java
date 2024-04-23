package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public final class PostConsumeClassItemEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final IClass playerClass;
    @Getter public final Set<UUID> affectedPlayers;
    @Getter public final IConsumeable consumable;
    @Getter @Setter public boolean cancelled;

    public PostConsumeClassItemEvent(Player who, IClass playerClass, IConsumeable consumable, Set<UUID> affectedPlayers) {
        super(who);
        this.playerClass = playerClass;
        this.consumable = consumable;
        this.affectedPlayers = affectedPlayers;
    }

    public boolean isAffected(Player player) {
        return affectedPlayers.contains(player.getUniqueId());
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

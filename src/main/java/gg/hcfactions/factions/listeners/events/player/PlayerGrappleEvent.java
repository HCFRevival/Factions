package gg.hcfactions.factions.listeners.events.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class PlayerGrappleEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter @Setter public boolean cancelled;
    @Getter @Setter public Location to;
    @Getter public final ItemStack item;
    @Getter public final Location from;
    @Getter public final LivingEntity attachedEntity;

    public PlayerGrappleEvent(Player who, ItemStack grappleItem, Location from, Location to, LivingEntity attachedEntity) {
        super(who);
        this.cancelled = false;
        this.item = grappleItem;
        this.from = from;
        this.to = to;
        this.attachedEntity = attachedEntity;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

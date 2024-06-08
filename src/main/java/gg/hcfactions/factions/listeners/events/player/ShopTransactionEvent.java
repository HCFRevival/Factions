package gg.hcfactions.factions.listeners.events.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopTransactionEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter @Setter public ItemStack item;
    @Getter public final ETransactionType transactionType;
    @Getter @Setter public Number amount;
    @Getter @Setter public boolean cancelled = false;

    public ShopTransactionEvent(Player player, ItemStack item, ETransactionType type, Number amount) {
        super(player);
        this.item = item;
        this.transactionType = type;
        this.amount = amount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public enum ETransactionType {
        BUY, SELL
    }
}

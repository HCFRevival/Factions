package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class EventShopTransactionEvent extends ShopTransactionEvent implements Cancellable {
    public final PlayerFaction faction;
    public boolean cancelled = false;

    public EventShopTransactionEvent(Player player, PlayerFaction faction, ItemStack item, Number amount) {
        super(player, item, ETransactionType.BUY, amount);
        this.faction = faction;
    }
}

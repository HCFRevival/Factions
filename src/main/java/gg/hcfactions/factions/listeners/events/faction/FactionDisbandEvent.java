package gg.hcfactions.factions.listeners.events.faction;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class FactionDisbandEvent extends PlayerEvent implements Cancellable {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter @Setter public PlayerFaction faction;
    @Getter @Setter public String cancelMessage;
    @Getter @Setter public boolean cancelled;

    /**
     * Player Big Move Event
     * @param who Player
     * @param faction Faction being disbanded
     */
    public FactionDisbandEvent(Player who, PlayerFaction faction) {
        super(who);
        this.faction = faction;
        this.cancelMessage = null;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

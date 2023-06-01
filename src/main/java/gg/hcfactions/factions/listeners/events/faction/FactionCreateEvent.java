package gg.hcfactions.factions.listeners.events.faction;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class FactionCreateEvent extends PlayerEvent implements Cancellable {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter @Setter public String factionName;
    @Getter public boolean serverFaction;
    @Getter @Setter public String cancelMessage;
    @Getter @Setter public boolean cancelled;

    /**
     * Player Big Move Event
     * @param who Player
     * @param factionName Name of the faction being created
     */
    public FactionCreateEvent(Player who, String factionName, boolean isServerFaction) {
        super(who);
        this.factionName = factionName;
        this.serverFaction = isServerFaction;
        this.cancelMessage = null;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

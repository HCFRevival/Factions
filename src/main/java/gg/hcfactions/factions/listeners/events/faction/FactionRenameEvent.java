package gg.hcfactions.factions.listeners.events.faction;

import gg.hcfactions.factions.models.faction.IFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class FactionRenameEvent extends Event implements Cancellable {
    @Getter public static HandlerList handlerList = new HandlerList();
    @Getter public IFaction faction;
    @Getter public String oldName;
    @Getter public String newName;
    @Getter @Setter public String cancelMessage;
    @Getter @Setter public boolean cancelled;

    public FactionRenameEvent(IFaction faction, String oldName, String newName) {
        this.faction = faction;
        this.oldName = oldName;
        this.newName = newName;
        this.cancelMessage = null;
        this.cancelled = false;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

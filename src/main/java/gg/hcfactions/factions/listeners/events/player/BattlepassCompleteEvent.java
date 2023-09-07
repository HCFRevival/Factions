package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class BattlepassCompleteEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final BPObjective objective;

    public BattlepassCompleteEvent(@NotNull Player who, BPObjective obj) {
        super(who);
        this.objective = obj;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

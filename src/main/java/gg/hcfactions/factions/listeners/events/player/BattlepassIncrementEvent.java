package gg.hcfactions.factions.listeners.events.player;

import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class BattlepassIncrementEvent extends PlayerEvent {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public BPObjective objective;
    @Getter public int amount;

    public BattlepassIncrementEvent(@NotNull Player who, BPObjective objective, int amount) {
        super(who);
        this.objective = objective;
        this.amount = amount;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

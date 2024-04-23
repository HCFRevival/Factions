package gg.hcfactions.factions.listeners.events.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class TankGuardApplyEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final LivingEntity affectedEntity;
    @Getter public final PotionEffectType effectType;
    @Getter @Setter public boolean cancelled;

    public TankGuardApplyEvent(Player who, LivingEntity affectedEntity, PotionEffectType effectType) {
        super(who);
        this.affectedEntity = affectedEntity;
        this.effectType = effectType;
        this.cancelled = false;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

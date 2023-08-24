package gg.hcfactions.factions.items.horn;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IBattleHorn {
    Factions getPlugin();
    EBattleHornType getType();
    List<PotionEffect> getActiveEffects();
    Map<PotionEffect, Integer> getPostEffects();

    default void apply(Player who, Collection<UUID> affectedIds) {
        if (getType().equals(EBattleHornType.CLEANSE)) {
            affectedIds.forEach(affectedId -> {
                final Player affectedPlayer = Bukkit.getPlayer(affectedId);

                if (affectedPlayer != null) {
                    final List<PotionEffectType> toRemove = List.of(
                            PotionEffectType.POISON,
                            PotionEffectType.SLOW,
                            PotionEffectType.WEAKNESS,
                            PotionEffectType.SLOW_FALLING,
                            PotionEffectType.WITHER,
                            PotionEffectType.DARKNESS
                    );

                    toRemove.forEach(affectedPlayer::removePotionEffect);

                    FMessage.printBattleHornConsumed(who, affectedPlayer, getType().getDisplayName());
                }
            });

            return;
        }

        final Map<UUID, List<PotionEffect>> existingPotionEffects = Maps.newHashMap();

        affectedIds.forEach(affectedId -> {
            final Player affectedPlayer = Bukkit.getPlayer(affectedId);

            if (affectedPlayer != null) {
                final List<PotionEffect> prevEffects = Lists.newArrayList();

                getActiveEffects().forEach(active -> {
                    final PotionEffect existingEffect = affectedPlayer.getPotionEffect(active.getType());

                    if (existingEffect != null && !existingEffect.isInfinite()) {
                        prevEffects.add(affectedPlayer.getPotionEffect(active.getType()));
                    }

                    if (existingEffect == null || !existingEffect.isInfinite()) {
                        if (existingEffect != null) {
                            affectedPlayer.removePotionEffect(active.getType());
                        }

                        affectedPlayer.addPotionEffect(active);
                    }
                });

                existingPotionEffects.put(affectedId, prevEffects);
                FMessage.printBattleHornConsumed(who, affectedPlayer, getType().getDisplayName());
             }
        });

        // re-apply old effect
        getActiveEffects().forEach(active -> new Scheduler(getPlugin()).sync(() -> {
            existingPotionEffects.forEach((uuid, existingEffects) -> {
                final Player affectedPlayer = Bukkit.getPlayer(uuid);

                if (affectedPlayer != null) {
                    existingEffects.stream().filter(eff -> eff.getType().equals(active.getType())).findFirst().ifPresent(prevEffect -> {
                        affectedPlayer.removePotionEffect(active.getType());
                        affectedPlayer.addPotionEffect(prevEffect);
                    });
                }
            });
        }).delay(active.getDuration() + 1).run());

        // apply post effects if they exist
        getPostEffects().forEach((postEffect, delay) -> new Scheduler(getPlugin()).sync(() -> {
            affectedIds.forEach(uuid -> {
                final Player affectedPlayer = Bukkit.getPlayer(uuid);

                if (affectedPlayer != null && !affectedPlayer.hasPotionEffect(postEffect.getType())) {
                    affectedPlayer.addPotionEffect(postEffect);
                }
            });
        }).delay(delay * 20).run());
    }
}

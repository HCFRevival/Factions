package gg.hcfactions.factions.models.classes;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public interface IClassHoldable {
    Factions getPlugin();

    /**
     * @return Material the player needs to be holding to trigger this effect
     */
    Material getMaterial();

    /**
     * @return Type of effect to grant to nearby players
     */
    PotionEffectType getEffectType();

    /**
     * @return Amplifier of the effect being given
     */
    int getAmplifier();

    /**
     * @return Duration this effect should be given to players
     */
    int getDuration();

    /**
     * @return Map of all players currently holding this holdable and the last time they gave the effect
     */
    Map<UUID, Long> getCurrentHolders();

    /**
     * @param uniqueId Player UUID
     * @return True if this player is currently holding the provided item
     */
    default boolean isHolding(UUID uniqueId) {
        return getCurrentHolders().containsKey(uniqueId);
    }

    /**
     * @param uniqueId Bukkit UUID
     * @return If present returns the timestamp since the last time the player issued this effect
     */
    default long getTimeSinceLastHold(UUID uniqueId) {
        return getCurrentHolders().getOrDefault(uniqueId, -1L);
    }

    /**
     * Applies the effect and marks the player as a holder
     * @param player Player
     * @param updateInterval Interval where this should be updated again
     */
    default void apply(Player player, int updateInterval, boolean initial) {
        if (!player.isOnline()
                || player.isDead()
                || (!player.getInventory().getItemInMainHand().getType().equals(getMaterial()) && !initial)
                || !(getPlugin().getClassManager().getCurrentClass(player) instanceof IHoldableClass)) {

            getCurrentHolders().remove(player.getUniqueId());
            return;
        }

        final PotionEffect eff = new PotionEffect(getEffectType(), getDuration()*20, getAmplifier());
        final List<Player> affected = Lists.newArrayList(FactionUtil.getNearbyFriendlies(getPlugin(), player, 16.0));
        affected.add(player);

        for (Player affectedPlayer : affected) {
            if (affectedPlayer.hasPotionEffect(getEffectType())) {
                if (Objects.requireNonNull(affectedPlayer.getPotionEffect(getEffectType())).isInfinite()) {
                    continue;
                }

                if (Objects.requireNonNull(affectedPlayer.getPotionEffect(getEffectType())).getAmplifier() > getAmplifier()) {
                    continue;
                }

                if (Objects.requireNonNull(affectedPlayer.getPotionEffect(getEffectType())).getDuration() > (getDuration()*20)) {
                    continue;
                }

                affectedPlayer.removePotionEffect(getEffectType());
            }

            affectedPlayer.addPotionEffect(eff);
        }

        getCurrentHolders().put(player.getUniqueId(), (Time.now() + (updateInterval*1000L)));
        new Scheduler(getPlugin()).sync(() -> apply(player, updateInterval, false)).delay(updateInterval*20L).run();
    }
}

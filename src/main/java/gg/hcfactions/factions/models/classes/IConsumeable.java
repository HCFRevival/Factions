package gg.hcfactions.factions.models.classes;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.listeners.events.player.ConsumeClassItemEvent;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.utils.Players;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IConsumeable {
    ClassManager getManager();
    Material getMaterial();
    EConsumableApplicationType getApplicationType();
    PotionEffectType getEffectType();
    int getDuration();
    int getCooldown();
    int getAmplifier();
    Map<UUID, Long> getCooldowns();

    default boolean hasCooldown(Player player) {
        return getCooldowns().containsKey(player.getUniqueId());
    }

    default boolean hasCooldown(UUID uniqueId) {
        return getCooldowns().containsKey(uniqueId);
    }

    default long getPlayerCooldown(Player player) {
        return getCooldowns().getOrDefault(player.getUniqueId(), 0L);
    }

    default void consume(Player player, ItemStack item, EPlayerHand hand) {
        final UUID uniqueId = player.getUniqueId();
        final IClass playerClass = getManager().getCurrentClass(player);
        final ConsumeClassItemEvent consumeEvent = new ConsumeClassItemEvent(player, playerClass, this);

        Bukkit.getPluginManager().callEvent(consumeEvent);
        if (consumeEvent.isCancelled()) {
            return;
        }

        // start consuming item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else if (hand.equals(EPlayerHand.MAIN)) {
            player.getInventory().setItemInMainHand(null);
        } else {
            player.getInventory().setItemInOffHand(null);
        }
        // end consuming item

        // start applying effect to self
        if (!getApplicationType().equals(EConsumableApplicationType.ENEMY_ONLY)) {
            final PotionEffect existing = player.getPotionEffect(getEffectType());
            final boolean wasExistingClassPassive = playerClass.getPassiveEffects().containsKey(getEffectType());

            if (player.hasPotionEffect(getEffectType())) {
                player.removePotionEffect(getEffectType());
            }

            player.addPotionEffect(new PotionEffect(getEffectType(), (getDuration() * 20), getAmplifier()));
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You now have " + ChatColor.AQUA + WordUtils.capitalize(getEffectType().getName().toLowerCase().replace("_", " ")) + " " +
                    (getAmplifier() + 1) + ChatColor.LIGHT_PURPLE + " for " + ChatColor.AQUA + getDuration() + " seconds");

            if (existing != null) {
                new Scheduler(getManager().getPlugin()).sync(() -> {
                    if (wasExistingClassPassive && !playerClass.getActivePlayers().contains(uniqueId)) {
                        return;
                    }

                    player.addPotionEffect(existing);
                }).delay((getDuration() * 20L) + 1L).run();
            }
        }

        getCooldowns().put(player.getUniqueId(), (Time.now() + (getCooldown() * 1000L)));

        new Scheduler(getManager().getPlugin()).sync(() -> {
            if (getCooldowns().containsKey(uniqueId) && Bukkit.getPlayer(uniqueId) != null) {
                Bukkit.getPlayer(uniqueId).sendMessage(ChatColor.GREEN + WordUtils.capitalize(getEffectType().getName().toLowerCase().replace("_", " ")) + " has been unlocked");
                getCooldowns().remove(uniqueId);
            }
        }).delay(getCooldown() * 20L).run();

        if (getApplicationType().equals(EConsumableApplicationType.INDIVIDUAL)) {
            return;
        }
        // end applying effect to self

        // start gathers all affected uuids in a set
        final Set<UUID> affected = Sets.newHashSet();

        consumeEvent.getAffectedPlayers().forEach((affectedId, isFriendly) -> {
            if (getApplicationType().equals(EConsumableApplicationType.FRIEND_ONLY) && isFriendly) {
                affected.add(affectedId);
            }

            else if (getApplicationType().equals(EConsumableApplicationType.ENEMY_ONLY) && !isFriendly) {
                affected.add(affectedId);
            }

            else if (getApplicationType().equals(EConsumableApplicationType.ALL)) {
                affected.add(affectedId);
            }
        });
        // end uuid gathering

        // apply effects to all gathered uuids
        affected.forEach(affectedId -> {
            final Player affectedPlayer = Bukkit.getPlayer(affectedId);

            if (affectedPlayer != null) {
                final IClass affectedPlayerClass = getManager().getCurrentClass(affectedPlayer);
                final PotionEffect existingPotionEffect = (affectedPlayer.hasPotionEffect(getEffectType()) ? Players.getPotionEffect(affectedPlayer, getEffectType()) : null);
                final boolean hasExistingClassPassive = (affectedPlayerClass != null && affectedPlayerClass.getPassiveEffects().containsKey(getEffectType()));

                if (affectedPlayer.hasPotionEffect(getEffectType())) {
                    affectedPlayer.removePotionEffect(getEffectType());
                }

                affectedPlayer.addPotionEffect(new PotionEffect(getEffectType(), (getDuration() * 20), getAmplifier()));

                switch (getApplicationType()) {
                    case ALL ->
                            affectedPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "You now have " + ChatColor.AQUA + WordUtils.capitalize(getEffectType().getName().toLowerCase().replace("_", " ")) + " " +
                                    (getAmplifier() + 1) + ChatColor.LIGHT_PURPLE + " for " + ChatColor.AQUA + getDuration() + " seconds");
                    case FRIEND_ONLY ->
                            affectedPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "You now have " + ChatColor.AQUA + WordUtils.capitalize(getEffectType().getName().toLowerCase().replace("_", " ")) + " " +
                                    (getAmplifier() + 1) + ChatColor.LIGHT_PURPLE + " for " + ChatColor.AQUA + getDuration() + " seconds " + ChatColor.LIGHT_PURPLE + "thanks to " + ChatColor.AQUA + player.getName());
                    case ENEMY_ONLY ->
                            affectedPlayer.sendMessage(ChatColor.RED + "You now have " + ChatColor.BLUE + WordUtils.capitalize(getEffectType().getName().toLowerCase().replace("_", " ")) + " " +
                                    (getAmplifier() + 1) + ChatColor.RED + " for " + ChatColor.BLUE + getDuration() + " seconds");
                }

                if (existingPotionEffect != null) {
                    new Scheduler(getManager().getPlugin()).sync(() -> {
                        if (hasExistingClassPassive && !affectedPlayerClass.getActivePlayers().contains(affectedId)) {
                            return;
                        }

                        affectedPlayer.addPotionEffect(existingPotionEffect);
                    }).delay((getDuration() * 20L) + 1L).run();
                }
            }
        });
        // end applying effect to all uuids

        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your effect was applied to " + ChatColor.AQUA + affected.size() + ChatColor.LIGHT_PURPLE + " players");
    }
}

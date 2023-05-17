package gg.hcfactions.factions.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.KOTHCaptureEvent;
import gg.hcfactions.factions.listeners.events.player.CombatLoggerDeathEvent;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.impl.PlayerStatHolder;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public record StatsListener(@Getter Factions plugin) implements Listener {
    /**
     * Handles caching a player's statistic holder upon login
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        plugin.getStatsManager().getPlayerStatistics(player.getUniqueId(), stats -> {
            stats.setJoinTime(Time.now());
            stats.setName(player.getName());
            plugin.getStatsManager().getTrackerRepository().add(stats);
        });
    }

    /**
     * Handles saving and decaching a player's statistics upon logout
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final PlayerStatHolder stats = plugin.getStatsManager().getPlayerStatistics(player.getUniqueId());

        if (stats == null) {
            return;
        }

        stats.setStatistic(EStatisticType.PLAYTIME, stats.getStatistic(EStatisticType.PLAYTIME) + stats.calculatePlaytime());

        plugin.getStatsManager().savePlayer(stats);
        plugin.getStatsManager().getTrackerRepository().remove(stats);
    }

    @EventHandler
    public void onPlayerGainExperience(PlayerPickupExperienceEvent event) {
        final Player player = event.getPlayer();
        final PlayerStatHolder stats = plugin.getStatsManager().getPlayerStatistics(player.getUniqueId());

        if (stats == null) {
            return;
        }

        final long amount = event.getExperienceOrb().getExperience();
        stats.addToStatistic(EStatisticType.EXP_EARNED, amount);
    }

    @EventHandler
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        final Player player = event.getDamager();

        if (!event.getType().equals(PlayerDamagePlayerEvent.DamageType.PROJECTILE)) {
            return;
        }

        final PlayerStatHolder stats = plugin.getStatsManager().getPlayerStatistics(player.getUniqueId());

        if (stats == null) {
            return;
        }

        final long dist = Math.round(event.getDamager().getLocation().distance(event.getDamaged().getLocation()));

        if (stats.getStatistic(EStatisticType.LONGSHOT) < dist) {
            stats.setStatistic(EStatisticType.LONGSHOT, dist);
        }
    }

    /**
     * Handles creating kill and death entries for players
     *
     * @param event PlayerDeathEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player slain = event.getEntity();
        final PlayerStatHolder slainStats = plugin.getStatsManager().getPlayerStatistics(slain.getUniqueId());

        if (slainStats == null) {
            plugin.getAresLogger().error("attempted to process player death statistics but player stat holder is null");
            return;
        }

        slainStats.addToStatistic(EStatisticType.DEATH, 1);

        if (slain.getKiller() != null) {
            final Player killer = slain.getKiller();
            final PlayerStatHolder killerStats = plugin.getStatsManager().getPlayerStatistics(killer.getUniqueId());

            if (killerStats != null) {
                killerStats.addToStatistic(EStatisticType.KILL, 1);
            }

            plugin.getStatsManager().createKill(killer.getUniqueId(), killer.getName(), slain.getUniqueId(), slain.getName(), event.getDeathMessage());
        }

        plugin.getStatsManager().createDeath(slain.getUniqueId(), slain.getName(), event.getDeathMessage());
    }

    /**
     * Handles creating kill and death entries for players
     *
     * @param event CombatLoggerDeathEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        plugin.getStatsManager().getPlayerStatistics(event.getLogger().getOwnerId(), slainHolder -> {
            if (slainHolder == null) {
                plugin.getAresLogger().error("attempted to retrieve player stat holder but returned null");
                return;
            }

            slainHolder.addToStatistic(EStatisticType.DEATH, 1);
            new Scheduler(plugin).async(() -> plugin.getStatsManager().savePlayer(slainHolder)).run();
        });

        if (event.getKiller() != null) {
            final Player killer = event.getKiller();
            final PlayerStatHolder killerStats = plugin.getStatsManager().getPlayerStatistics(killer.getUniqueId());

            if (killerStats != null) {
                killerStats.addToStatistic(EStatisticType.KILL, 1);
            }

            plugin.getStatsManager().createKill(
                    killer.getUniqueId(),
                    killer.getName(),
                    event.getLogger().getOwnerId(),
                    event.getLogger().getOwnerUsername(),
                    event.getLogger().getOwnerUsername() + "'s combat-logger was slain by " + killer.getName()
            );
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onEventCapture(KOTHCaptureEvent event) {
        final List<String> capturingUsernames = Lists.newArrayList();

        event.getCapturingFaction().getOnlineMembers().forEach(onlineMember -> {
            final Player player = Bukkit.getPlayer(onlineMember.getUniqueId());

            if (player != null) {
                final PlayerStatHolder holder = plugin.getStatsManager().getPlayerStatistics(player.getUniqueId());
                capturingUsernames.add(player.getName());
                holder.addToStatistic(EStatisticType.EVENT_CAPTURES, 1);
            }
        });

        plugin.getStatsManager().createEventCapture(
                event.getEvent().getName(),
                event.getCapturingFaction().getUniqueId(),
                event.getCapturingFaction().getName(),
                capturingUsernames
        );
    }
}

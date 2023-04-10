package gg.hcfactions.factions.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.impl.PlayerStatHolder;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public final class StatsListener implements Listener {
    @Getter public final Factions plugin;

    /**
     * Handles caching a player's statistic holder upon login
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        plugin.getStatsManager().getPlayerStatistics(player.getUniqueId(), stats -> {
            stats.setJoinTime(Time.now());
            plugin.getStatsManager().savePlayer(stats);
        });
    }

    /**
     * Handles saving and decaching a player's statistics upon logout
     * @param event PlayerQuitEvent
     */
    @EventHandler (priority = EventPriority.LOW)
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
     * @param event PlayerDeathEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player slain = event.getEntity();
        final PlayerStatHolder slainStats = plugin.getStatsManager().getPlayerStatistics(slain.getUniqueId());

        slainStats.addToStatistic(EStatisticType.DEATH, 1);

        if (slain.getKiller() != null) {
            final Player killer = slain.getKiller();
            final PlayerStatHolder killerStats = plugin.getStatsManager().getPlayerStatistics(killer.getUniqueId());

            plugin.getStatsManager().createKill(killer.getUniqueId(), killer.getName(), slain.getUniqueId(), slain.getName(), event.getDeathMessage());
            killerStats.addToStatistic(EStatisticType.KILL, 1);

            return;
        }

        plugin.getStatsManager().createDeath(slain.getUniqueId(), slain.getName(), event.getDeathMessage());
    }

    /* @EventHandler (priority = EventPriority.MONITOR)
    public void onEventCapture(EventCaptureEvent event) {
        final List<String> capturingUsernames = Lists.newArrayList();

        event.getFaction().getOnlineMembers().forEach(onlineMember -> {
            final Player player = Bukkit.getPlayer(onlineMember.getUniqueId());

            if (player != null) {
                final PlayerStatisticHolder stats = addon.getPlayerStatistics(player.getUniqueId());

                capturingUsernames.add(player.getName());

                stats.addToStatistic(PlayerStatisticHolder.StatisticType.EVENT_CAPTURES, 1);
            }
        });

        addon.createEventCapture(event.getEvent().getName(), event.getFaction().getUniqueId(), event.getFaction().getName(), capturingUsernames);
    } */
}

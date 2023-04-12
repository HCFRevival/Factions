package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.CombatLoggerDeathEvent;
import gg.hcfactions.factions.models.logger.impl.CombatLogger;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.impl.PlayerStatHolder;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.event.PlayerDeathbanEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public record DeathbanListener(@Getter Factions plugin) implements Listener {
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final UUID uniqueId = player.getUniqueId();
        final ServerStateManager stateManager = plugin.getServerStateManager();
        final PlayerStatHolder statHolder = plugin.getStatsManager().getPlayerStatistics(uniqueId);

        if (statHolder == null) {
            return;
        }

        final PlayerDeathbanEvent de = new PlayerDeathbanEvent(
                uniqueId,
                event.getDeathMessage(),
                (int)(statHolder.getStatistic(EStatisticType.PLAYTIME) / 1000L),
                stateManager.getCurrentState().equals(EServerState.SOTW),
                (stateManager.getCurrentState().equals(EServerState.EOTW_PHASE_1) || stateManager.getCurrentState().equals(EServerState.EOTW_PHASE_2))
        );

        Bukkit.getPluginManager().callEvent(de);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        final CombatLogger logger = event.getLogger();
        final UUID uniqueId = logger.getOwnerId();

        plugin.getStatsManager().getPlayerStatistics(uniqueId, holder -> {
            if (holder == null) {
                return;
            }

            final PlayerDeathbanEvent deathbanEvent = new PlayerDeathbanEvent(
                    uniqueId,
                    ChatColor.RED + "Your combat-logger was slain by " + ChatColor.YELLOW + event.getKiller().getName(),
                    (int)(holder.getStatistic(EStatisticType.PLAYTIME) / 1000L),
                    plugin.getServerStateManager().getCurrentState().equals(EServerState.SOTW),
                    (plugin.getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_1) || plugin.getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2))
            );

            Bukkit.getPluginManager().callEvent(deathbanEvent);
        });
    }
}

package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.CombatLoggerDeathEvent;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.logger.impl.CombatLogger;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.impl.PlayerStatHolder;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.event.PlayerDeathbanEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record DeathbanListener(@Getter Factions plugin) implements Listener {
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfiguration().isDeathbansEnabled() || !plugin.getConfiguration().isDeathbansStandalone()) {
            return;
        }

        final Player player = event.getEntity();
        final UUID uniqueId = player.getUniqueId();
        final ServerStateManager stateManager = plugin.getServerStateManager();
        final PlayerStatHolder statHolder = plugin.getStatsManager().getPlayerStatistics(uniqueId);
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(new PLocatable(player));
        boolean isInEvent = false;

        if (insideClaim != null && plugin.getFactionManager().getFactionById(insideClaim.getOwner()) instanceof final ServerFaction sf) {
            final Optional<IEvent> eventQuery = plugin.getEventManager().getEvent(sf);

            if (eventQuery.isPresent() && eventQuery.get().isActive()) {
                isInEvent = true;
            }
        }

        if (statHolder == null) {
            return;
        }

        final PlayerDeathbanEvent de = new PlayerDeathbanEvent(
                uniqueId,
                event.getDeathMessage(),
                (int)(statHolder.getStatistic(EStatisticType.PLAYTIME) / 1000L),
                isInEvent,
                stateManager.getCurrentState().equals(EServerState.SOTW),
                (stateManager.getCurrentState().equals(EServerState.EOTW_PHASE_1) || stateManager.getCurrentState().equals(EServerState.EOTW_PHASE_2))
        );

        Bukkit.getPluginManager().callEvent(de);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        if (!plugin.getConfiguration().isDeathbansEnabled() || !plugin.getConfiguration().isDeathbansStandalone()) {
            return;
        }

        final CombatLogger logger = event.getLogger();
        final Location location = logger.getBukkitEntity().getLocation();
        final UUID uniqueId = logger.getOwnerId();
        final String deathMessage = "Your combat-logger was slain" + (event.getKiller() != null ? ChatColor.RED + " by " + event.getKiller().getName() : "");
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(
                new PLocatable(Objects.requireNonNull(location.getWorld()).getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()));

        boolean inEvent = false;
        if (insideClaim != null && plugin.getFactionManager().getFactionById(insideClaim.getOwner()) instanceof final ServerFaction sf) {
            final Optional<IEvent> eventQuery = plugin.getEventManager().getEvent(sf);

            if (eventQuery.isPresent() && eventQuery.get().isActive()) {
                inEvent = true;
            }
        }

        final boolean isInEvent = inEvent;

        plugin.getStatsManager().getPlayerStatistics(uniqueId, holder -> {
            if (holder == null) {
                return;
            }

            final PlayerDeathbanEvent deathbanEvent = new PlayerDeathbanEvent(
                    uniqueId,
                    deathMessage,
                    (int)(holder.getStatistic(EStatisticType.PLAYTIME) / 1000L),
                    isInEvent,
                    plugin.getServerStateManager().getCurrentState().equals(EServerState.SOTW),
                    (plugin.getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_1) || plugin.getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2))
            );

            Bukkit.getPluginManager().callEvent(deathbanEvent);
        });
    }
}

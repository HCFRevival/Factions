package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.KOTHTickEvent;
import gg.hcfactions.factions.events.tracker.EventTrackerManager;
import gg.hcfactions.factions.listeners.events.faction.FactionTicketLossEvent;
import gg.hcfactions.factions.listeners.events.player.*;
import gg.hcfactions.factions.models.events.ICaptureEvent;
import gg.hcfactions.factions.models.events.impl.tracking.entry.EventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.tracking.entry.types.DeathEventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.tracking.entry.types.KOTHTickEventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.tracking.entry.types.KOTHTicketLossEventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.tracking.entry.types.KillEventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.tracking.IEventTrackerPlayer;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public final class EventTrackerListener implements Listener {
    @Getter public final Factions plugin;

    public EventTrackerListener(Factions plugin) {
        this.plugin = plugin;
    }

    private EventTrackerEntry createDeathEvent(UUID slainUniqueId, UUID killerUniqueId, String slainUsername, String killerUsername, BLocatable deathLocation) {
        if (killerUniqueId != null && killerUsername != null) {
            return new KillEventTrackerEntry(
                    plugin,
                    killerUniqueId,
                    killerUsername,
                    slainUniqueId,
                    slainUsername,
                    deathLocation
            );
        }

        return new DeathEventTrackerEntry(
                plugin,
                slainUniqueId,
                slainUsername,
                deathLocation
        );
    }

    /**
     * Handles updating existing player tracker if
     * the player changed their username
     * @param event Bukkit PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        plugin.getEventManager().getTrackerManager()
                .getActiveTrackers(player.getUniqueId())
                .stream()
                .filter(tracker -> !tracker.getUsername().equals(player.getName()))
                .forEach(trackerNeedUpdate -> trackerNeedUpdate.setUsername(player.getName()));
    }

    /**
     * Handles creating a player death event
     * for a tracked event
     * @param event Bukkit PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player slainPlayer = event.getEntity();
        final Player killerPlayer = slainPlayer.getKiller();

        if (killerPlayer != null) {
            final BLocatable location = new BLocatable(killerPlayer.getLocation().getBlock());

            plugin.getEventManager().getTrackerManager().getTrackerByLocation(location).ifPresent(tracker -> tracker.addEntry(createDeathEvent(
                    slainPlayer.getUniqueId(),
                    killerPlayer.getUniqueId(),
                    slainPlayer.getName(),
                    killerPlayer.getName(),
                    location
            )));

            return;
        }

        final BLocatable location = new BLocatable(slainPlayer.getLocation().getBlock());

        plugin.getEventManager().getTrackerManager().getTrackerByLocation(location).ifPresent(tracker -> tracker.addEntry(createDeathEvent(
                slainPlayer.getUniqueId(),
                null,
                slainPlayer.getName(),
                null,
                location
        )));
    }

    /**
     * Handles creating a player death event
     * for a tracked event
     * @param event HCFR CombatLoggerDeathEvent
     */
    @EventHandler
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        final UUID slainUniqueId = event.getLogger().getOwnerId();
        final String slainUsername = event.getLogger().getOwnerUsername();
        final Player killerPlayer = event.getKiller();

        if (killerPlayer != null) {
            final BLocatable location = new BLocatable(killerPlayer.getLocation().getBlock());

            plugin.getEventManager().getTrackerManager().getTrackerByLocation(location).ifPresent(tracker -> tracker.addEntry(createDeathEvent(
                    slainUniqueId,
                    killerPlayer.getUniqueId(),
                    slainUsername,
                    killerPlayer.getName(),
                    location
            )));

            return;
        }

        final BLocatable location = new BLocatable(event.getLogger().getBukkitEntity().getLocation().getBlock());

        plugin.getEventManager().getTrackerManager().getTrackerByLocation(location).ifPresent(tracker -> tracker.addEntry(createDeathEvent(
                slainUniqueId,
                null,
                slainUsername,
                null,
                location
        )));
    }

    /**
     * Handles creating a koth tick entry
     * @param event HCFR KOTHTickEvent
     */
    @EventHandler
    public void onTicketCaptured(KOTHTickEvent event) {
        final KOTHEvent kothEvent = event.getEvent();
        final PlayerFaction faction = event.getCapturingFaction();
        kothEvent.getSession().getTracker().addEntry(new KOTHTickEventTrackerEntry(plugin, faction.getUniqueId(), event.getNewTicketCount()));
    }

    /**
     * Handles creating a koth ticket loss entry
     * @param event HCFR FactionTicketLossEvent
     */
    @EventHandler
    public void onTicketLoss(FactionTicketLossEvent event) {
        if (!(event.getEvent() instanceof final KOTHEvent kothEvent)) {
            return;
        }

        kothEvent.getSession().getTracker().addEntry(new KOTHTicketLossEventTrackerEntry(
                plugin,
                event.getFaction().getUniqueId(),
                (event.getNewTicketCount() - event.getOldTicketCount()))
        );
    }

    /**
     * Handles increasing damage for a player being
     * tracked in an event
     * @param event Ares PlayerDamagePlayerEvent
     */
    @EventHandler
    public void onPlayerAttackPlayer(PlayerDamagePlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final double damage = event.getDamage();

        plugin.getEventManager().getTrackerManager().getTrackerByLocation(new PLocatable(attacker)).ifPresent(attackerTracker -> {
            final IEventTrackerPlayer trackedPlayer = plugin.getEventManager().getTrackerManager().getOrCreatePlayerTracker(attacker, attackerTracker);
            trackedPlayer.add(EventTrackerManager.P_DAMAGE_DEALT, damage);
        });

        plugin.getEventManager().getTrackerManager().getTrackerByLocation(new PLocatable(attacked)).ifPresent(attackedTracker -> {
            final IEventTrackerPlayer trackedPlayer = plugin.getEventManager().getTrackerManager().getOrCreatePlayerTracker(attacked, attackedTracker);
            trackedPlayer.add(EventTrackerManager.P_DAMAGE_TAKEN, damage);
        });
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onArcherTag(ArcherTagEvent event) {
        if (event.isCancelled() || !(event.getAttacked() instanceof Player)) {
            return;
        }

        final Player player = event.getPlayer();
        plugin.getEventManager().getTrackerManager().getTrackerByLocation(new PLocatable(player)).ifPresent(tracker -> {
            final IEventTrackerPlayer playerTracker = plugin.getEventManager().getTrackerManager().getOrCreatePlayerTracker(player, tracker);
            playerTracker.add(EventTrackerManager.P_ARCHER_RANGE_DMG, event.getDamage());
        });
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onArcherMarkEvent(ArcherMarkEvent event) {
        if (event.isCancelled() || !(event.getAttacked() instanceof Player)) {
            return;
        }

        final Player player = event.getPlayer();
        plugin.getEventManager().getTrackerManager().getTrackerByLocation(new PLocatable(player)).ifPresent(tracker -> {
            final IEventTrackerPlayer playerTracker = plugin.getEventManager().getTrackerManager().getOrCreatePlayerTracker(player, tracker);
            playerTracker.add(EventTrackerManager.P_ARCHER_TAG_HIT, 1);
        });
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onRogueBackstab(RogueBackstabEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        plugin.getEventManager().getTrackerManager().getTrackerByLocation(new PLocatable(player)).ifPresent(tracker -> {
            final IEventTrackerPlayer playerTracker = plugin.getEventManager().getTrackerManager().getOrCreatePlayerTracker(player, tracker);
            playerTracker.add(EventTrackerManager.P_ROGUE_BACKSTAB, 1);
        });
    }

    @EventHandler
    public void onDiverPierce(DiverPierceEvent event) {
        if (event.isCancelled() || !(event.getAttacked() instanceof Player)) {
            return;
        }

        final Player player = event.getPlayer();
        plugin.getEventManager().getTrackerManager().getTrackerByLocation(new PLocatable(player)).ifPresent(tracker -> {
            final IEventTrackerPlayer playerTracker = plugin.getEventManager().getTrackerManager().getOrCreatePlayerTracker(player, tracker);
            playerTracker.add(EventTrackerManager.P_DIVER_DMG, event.getDamage());
        });
    }
}

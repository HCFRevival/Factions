package gg.hcfactions.factions.events.tracker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.tracking.GenericEventTrackerPlayer;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.tracking.IEventTracker;
import gg.hcfactions.factions.models.events.tracking.IEventTrackerPlayer;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.bukkit.location.ILocatable;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class EventTrackerManager {
    // Player Tracker Keys
    public static final String P_DAMAGE_DEALT = "melee_damage_dealt";
    public static final String P_DAMAGE_TAKEN = "melee_damage_taken";
    public static final String P_ARCHER_RANGE_DMG = "archer_damage_dealt";
    public static final String P_ARCHER_TAG_HIT = "archer_tags_dealt";
    public static final String P_ROGUE_BACKSTAB = "rogue_backstabs";
    public static final String P_BARD_EFFECT_GIVEN = "bard_effects_dealt";
    public static final String P_BARD_ASSISTS = "bard_assists";
    public static final String P_DIVER_DMG = "diver_damage_dealt";
    public static final String P_HEALTH_POTIONS_USED = "health_potions_consumed";
    public static final String P_TOTEMS_USED = "totems_consumed";
    public static final String P_GAPPLES_USED = "gapples_consumed";
    public static final String P_TANK_GUARD = "guardian_protection";

    @Getter public final EventManager eventManager;

    public EventTrackerManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Attempts to query a player tracker and if none
     * is found creates and returns a new instance
     *
     * This function also handles adding the newly created
     * tracker to the provided EventTracker's player repo
     *
     * @param player Bukkit Player
     * @param tracker Event Tracker
     * @return Event Tracker Player Instance
     */
    public IEventTrackerPlayer getOrCreatePlayerTracker(Player player, IEventTracker<?> tracker) {
        return getOrCreatePlayerTracker(player.getUniqueId(), player.getName(), tracker);
    }

    /**
     * Attempts to query a player tracker and if none
     * is found creates and returns a new instance
     *
     * This function also handles adding the newly created
     * tracker to the provided EventTracker's player repo
     *
     * @param uniqueId Bukkit Player UUID
     * @param username Bukkit Player Username
     * @param tracker Event Tracker
     * @return Event Tracker Player Instance
     */
    public IEventTrackerPlayer getOrCreatePlayerTracker(UUID uniqueId, String username, IEventTracker<?> tracker) {
        final Optional<IEventTrackerPlayer> existingPlayerQuery = tracker.getParticipant(uniqueId);
        if (existingPlayerQuery.isEmpty()) {
            final GenericEventTrackerPlayer newPlayerTracker = new GenericEventTrackerPlayer(uniqueId, username);
            tracker.getParticipants().add(newPlayerTracker);
            return newPlayerTracker;
        }

        return existingPlayerQuery.get();
    }

    /**
     * Queries an event tracker active at the provided location
     * @param location Location to query
     * @return Optional of Event Trackers
     */
    public Optional<IEventTracker<?>> getTrackerByLocation(ILocatable location) {
        Claim insideClaim = eventManager.getPlugin().getClaimManager().getClaimAt(location);
        if (insideClaim == null) {
            return Optional.empty();
        }

        final ServerFaction serverFaction = eventManager.getPlugin().getFactionManager().getServerFactionById(insideClaim.getOwner());
        if (serverFaction == null) {
            return Optional.empty();
        }

        final Optional<IEvent> eventQuery = eventManager.getPlugin().getEventManager().getEvent(serverFaction);
        if (eventQuery.isEmpty()) {
            return Optional.empty();
        }

        final IEvent event = eventQuery.get();

        if (event instanceof final KOTHEvent kothEvent) {
            return Optional.of(kothEvent.getSession().getTracker());
        }

        eventManager.getPlugin().getAresLogger().warn("Attempted to query an event tracker for an unsupported event type");
        return Optional.empty();
    }

    /**
     * Returns all active event trackers associated
     * with the provided Bukkit Player UUID
     * @param uniqueId Bukkit Player UUID
     * @return List of Event Tracker Player instances
     */
    public ImmutableList<IEventTrackerPlayer> getActiveTrackers(UUID uniqueId) {
        final List<IEventTrackerPlayer> res = Lists.newArrayList();

        eventManager.getActiveEvents().forEach(event -> {
            if (event instanceof final KOTHEvent kothEvent) {
                kothEvent.getSession().getTracker().getParticipant(uniqueId).ifPresent(res::add);
            }

            // TODO: Add support for other event types
        });

        return ImmutableList.copyOf(res);
    }
}

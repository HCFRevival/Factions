package gg.hcfactions.factions.models.events.tracking;

import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.tracking.entry.EventTrackerEntry;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IEventTracker<T> extends MongoDocument<T> {
    /**
     * @return Event that owns this tracker instance
     */
    IEvent getEvent();

    /**
     * @return Collection of player tracker objects
     */
    Set<IEventTrackerPlayer> getParticipants();

    /**
     * @return Collection of global event entries
     */
    Set<IEventTrackerEntry> getEntries();

    /**
     * @return True if this tracker is active
     */
    boolean isTracking();

    /**
     * @param player Player to query
     * @return True if the provided player is being tracked
     */
    default boolean isTracking(Player player) {
        return getParticipants().stream().anyMatch(t -> t.getUniqueId().equals(player.getUniqueId()));
    }

    /**
     * @param playerUniqueId Player UUID
     * @return True if the provided player is being tracked
     */
    default boolean isTracking(UUID playerUniqueId) {
        return getParticipants().stream().anyMatch(t -> t.getUniqueId().equals(playerUniqueId));
    }

    /**
     * Queries for a specified participant
     * @param uniqueId Participant Bukkit UUID
     * @return Optional of the player tracker instance
     */
    default Optional<IEventTrackerPlayer> getParticipant(UUID uniqueId) {
        return getParticipants().stream().filter(p -> p.getUniqueId().equals(uniqueId)).findAny();
    }

    /**
     * Queries for a specified participant
     * @param player Bukkit Player
     * @return Optional of the player tracker instance
     */
    default Optional<IEventTrackerPlayer> getParticipant(Player player) {
        return getParticipant(player.getUniqueId());
    }

    /**
     * Adds a new global entry to this tracker
     * @param entry EventTrackerEntry
     */
    default void addEntry(EventTrackerEntry entry) {
        if (!isTracking()) {
            return;
        }

        getEntries().add(entry);
    }

    /**
     * Starts the tracking process and accepts
     * new tracker events
     */
    void startTracking();

    /**
     * Stops the tracking process and no longer
     * accepts new tracker events
     */
    void stopTracking();

    /**
     * Push tracking data to the database
     */
    void publishTracking();
}

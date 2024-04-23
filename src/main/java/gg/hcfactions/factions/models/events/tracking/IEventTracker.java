package gg.hcfactions.factions.models.events.tracking;

import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.tracking.entry.EventTrackerEntry;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IEventTracker<T> extends MongoDocument<T> {
    IEvent getEvent();
    Set<IEventTrackerPlayer> getParticipants();
    Set<IEventTrackerEntry> getEntries();
    boolean isTracking();

    default boolean isTracking(Player player) {
        return getParticipants().stream().anyMatch(t -> t.getUniqueId().equals(player.getUniqueId()));
    }

    default boolean isTracking(UUID playerUniqueId) {
        return getParticipants().stream().anyMatch(t -> t.getUniqueId().equals(playerUniqueId));
    }

    default Optional<IEventTrackerPlayer> getParticipant(UUID uniqueId) {
        return getParticipants().stream().filter(p -> p.getUniqueId().equals(uniqueId)).findAny();
    }

    default Optional<IEventTrackerPlayer> getParticipant(Player player) {
        return getParticipant(player.getUniqueId());
    }

    default void addEntry(EventTrackerEntry entry) {
        if (!isTracking()) {
            return;
        }

        getEntries().add(entry);
        getEvent().getPlugin().getAresLogger().info("entry added to stack");
    }

    void startTracking();
    void stopTracking();
    void publishTracking();
}

package gg.hcfactions.factions.models.events.tracking;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import gg.hcfactions.factions.listeners.events.web.EventTrackerPublishEvent;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.tracking.entry.EventTrackerEntry;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
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
     * @return Event end timestamp
     */
    long getEndTime();

    /**
     * @param timestamp Set event end timestamp
     */
    void setEndTime(long timestamp);

    /**
     * Update the tracking status
     * @param b Value
     */
    void setTracking(boolean b);

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
    default void publishTracking() {
        final long start = Time.now();

        setTracking(false);
        setEndTime(Time.now());

        new Scheduler(getEvent().getPlugin()).async(() -> {
            final Mongo mdb = (Mongo) getEvent().getPlugin().getConnectable(Mongo.class);
            if (mdb == null) {
                getEvent().getPlugin().getAresLogger().error("Attempted to publish event tracker with null mdb instance");
                return;
            }

            final MongoDatabase db = mdb.getDatabase(getEvent().getPlugin().getConfiguration().getMongoDatabaseName());
            if (db == null) {
                getEvent().getPlugin().getAresLogger().error("Attempted to publish event tracker with null mongo database");
                return;
            }

            final MongoCollection<Document> coll = db.getCollection(getEvent().getPlugin().getConfiguration().getTrackerCollection());
            final Document doc = toDocument();

            coll.insertOne(doc);
            final ObjectId docId = doc.getObjectId("_id");

            new Scheduler(getEvent().getPlugin()).sync(() -> {
                final EventTrackerPublishEvent pubEvent = new EventTrackerPublishEvent(getEvent().getPlugin().getConfiguration().getWebsiteDomain() + "tracker/" + docId);
                Bukkit.getPluginManager().callEvent(pubEvent);

                final long end = Time.now();
                final long diff = (end - start);
                getEvent().getPlugin().getAresLogger().info("Published event tracker data (took " + diff + "ms)");
            }).run();
        }).run();
    }
}

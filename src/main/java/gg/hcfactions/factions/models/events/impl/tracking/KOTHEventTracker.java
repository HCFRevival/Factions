package gg.hcfactions.factions.models.events.impl.tracking;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.tracking.IEventTracker;
import gg.hcfactions.factions.models.events.tracking.IEventTrackerEntry;
import gg.hcfactions.factions.models.events.tracking.IEventTrackerPlayer;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class KOTHEventTracker implements IEventTracker<KOTHEventTracker> {
    @Getter public final KOTHEvent event;
    @Getter @Setter public boolean tracking;
    @Getter public final Set<IEventTrackerPlayer> participants;
    @Getter public final Set<IEventTrackerEntry> entries;

    private final Factions plugin;
    private long startTime;
    private long endTime;

    public KOTHEventTracker(KOTHEvent event) {
        this.event = event;
        this.plugin = event.getPlugin();
        this.participants = Sets.newConcurrentHashSet();
        this.entries = Sets.newHashSet();
    }

    @Override
    public void startTracking() {
        this.tracking = true;
        this.startTime = Time.now();
    }

    @Override
    public void stopTracking() {
        this.tracking = false;
    }

    @Override
    public void publishTracking() {
        final long start = Time.now();

        this.tracking = false;
        this.endTime = Time.now();

        new Scheduler(plugin).async(() -> {
            final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
            if (mdb == null) {
                plugin.getAresLogger().error("Attempted to publish event tracker with null mdb instance");
                return;
            }

            final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
            if (db == null) {
                plugin.getAresLogger().error("Attempted to publish event tracker with null mongo database");
                return;
            }

            final MongoCollection<Document> coll = db.getCollection(plugin.getConfiguration().getTrackerCollection());
            coll.insertOne(toDocument());

            new Scheduler(plugin).sync(() -> {
                final long end = Time.now();
                final long diff = (end - start);
                plugin.getAresLogger().info("Published event tracker data (took " + diff + "ms)");
            }).run();
        }).run();
    }

    @Override
    public KOTHEventTracker fromDocument(Document document) {
        return null;
    }

    public Document toDocument() {
        final Document doc = new Document();
        final List<Document> participantDocs = Lists.newArrayList();
        final List<Document> eventDocs = Lists.newArrayList();
        final Map<String, Integer> leaderboard = Maps.newHashMap();
        final PlayerFaction winnerFaction = event.getSession().getCapturingFaction();
        final long duration = endTime - startTime;

        event.getSession().getLeaderboard().forEach((fid, tickets) -> {
            final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionById(fid);

            if (faction != null) {
                final int newTickets = (winnerFaction != null && winnerFaction.getUniqueId().equals(faction.getUniqueId()))
                        ? (tickets + 1)
                        : tickets;

                leaderboard.put(faction.getName(), newTickets);
            }
        });

        participants.forEach(p -> participantDocs.add(p.toDocument()));
        entries.forEach(e -> eventDocs.add(e.toDocument()));

        doc.append("metadata", new Document()
                .append("name", ChatColor.stripColor(event.getDisplayName()))
                .append("start_time", startTime)
                .append("end_time", endTime)
                .append("duration", duration));

        doc.append("leaderboard", leaderboard);
        doc.append("participants", participantDocs);
        doc.append("entries", eventDocs);

        if (winnerFaction != null) {
            doc.append("winner", new Document()
                    .append("id", winnerFaction.getUniqueId().toString())
                    .append("name", winnerFaction.getName()));
        }

        return doc;
    }
}

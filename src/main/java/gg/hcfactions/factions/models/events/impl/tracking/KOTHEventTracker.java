package gg.hcfactions.factions.models.events.impl.tracking;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.tracking.IEventTracker;
import gg.hcfactions.factions.models.events.tracking.IEventTrackerEntry;
import gg.hcfactions.factions.models.events.tracking.IEventTrackerPlayer;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.util.Time;
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
    @Getter @Setter public long endTime;
    @Getter public final Set<IEventTrackerPlayer> participants;
    @Getter public final Set<IEventTrackerEntry> entries;

    private final Factions plugin;
    private long startTime;

    public KOTHEventTracker(KOTHEvent event) {
        this.event = event;
        this.plugin = event.getPlugin();
        this.participants = Sets.newConcurrentHashSet();
        this.entries = Sets.newHashSet();
        this.endTime = -1L;
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

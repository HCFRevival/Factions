package gg.hcfactions.factions.models.events.impl.tracking.entry;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.tracking.EEventTrackerEntryType;
import gg.hcfactions.factions.models.events.tracking.IEventTrackerEntry;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import org.bson.Document;

public class EventTrackerEntry implements IEventTrackerEntry<EventTrackerEntry> {
    @Getter public final Factions plugin;
    @Getter public EEventTrackerEntryType type;
    @Getter public final long time;

    public EventTrackerEntry(Factions plugin) {
        this.plugin = plugin;
        this.type = EEventTrackerEntryType.GENERIC;
        this.time = Time.now();
    }

    @Override
    public EventTrackerEntry fromDocument(Document document) {
        return null;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("type", type.name().toLowerCase())
                .append("time", time);
    }
}

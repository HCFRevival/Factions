package gg.hcfactions.factions.models.stats.impl.stat;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.models.stats.ITrackable;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import org.bson.Document;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class EventCaptureStat implements ITrackable, MongoDocument<EventCaptureStat> {
    @Getter public UUID factionUniqueId;
    @Getter public String factionName;
    @Getter public String eventName;
    @Getter public List<String> factionMemberNames;
    @Getter public double mapNumber;
    @Getter public long date;

    public EventCaptureStat() {
        this.factionUniqueId = null;
        this.factionName = null;
        this.eventName = null;
        this.factionMemberNames = Lists.newArrayList();
        this.mapNumber = 0.0D;
        this.date = 0L;
    }

    public EventCaptureStat(UUID factionUniqueId, String factionName, String eventName, Collection<String> factionMemberNames, double currentMap) {
        this.factionUniqueId = factionUniqueId;
        this.factionName = factionName;
        this.eventName = eventName;
        this.factionMemberNames = Lists.newArrayList(factionMemberNames);
        this.mapNumber = currentMap;
        this.date = Time.now();
    }

    @Override
    public EventCaptureStat fromDocument(Document document) {
        this.factionUniqueId = (UUID)document.get("faction_id");
        this.factionName = document.getString("faction_name");
        this.eventName = document.getString("event_name");
        this.factionMemberNames = document.getList("faction_members", String.class);
        this.mapNumber = document.getDouble("map");
        this.date = document.getLong("date");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("faction_id", factionUniqueId)
                .append("faction_name", factionName)
                .append("event_name", eventName)
                .append("faction_members", factionMemberNames)
                .append("map", mapNumber)
                .append("date", date);
    }
}

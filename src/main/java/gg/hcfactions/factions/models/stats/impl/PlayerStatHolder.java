package gg.hcfactions.factions.models.stats.impl;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.IStatHolder;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.Map;
import java.util.UUID;

public final class PlayerStatHolder implements IStatHolder, MongoDocument<PlayerStatHolder> {
    @Getter public UUID uniqueId;
    @Getter @Setter public String name;
    @Getter public double mapNumber;
    @Getter @Setter public long joinTime;
    @Getter public final Map<EStatisticType, Long> values;

    public PlayerStatHolder() {
        this.uniqueId = null;
        this.name = null;
        this.joinTime = 0L;
        this.mapNumber = 0;
        this.values = Maps.newConcurrentMap();

        for (EStatisticType t : EStatisticType.values()) {
            values.put(t, 0L);
        }
    }

    public PlayerStatHolder(UUID uniqueId, String name, double map) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.joinTime = Time.now();
        this.mapNumber = map;
        this.values = Maps.newConcurrentMap();

        for (EStatisticType t : EStatisticType.values()) {
            values.put(t, 0L);
        }
    }

    /**
     * Return the time in millis since this holder was initialized
     * @return Millis
     */
    public long calculatePlaytime() {
        return (Time.now() - joinTime);
    }

    @Override
    public PlayerStatHolder fromDocument(Document document) {
        this.uniqueId = UUID.fromString(document.getString("uuid"));
        this.mapNumber = document.getDouble("map");

        for (EStatisticType type : EStatisticType.values()) {
            if (!document.containsKey(type.name()) || document.get(type.name()) == null) {
                continue;
            }

            final long value = document.getLong(type.name());
            this.values.put(type, value);
        }

        return this;
    }

    @Override
    public Document toDocument() {
        final Document document = new Document();

        document.append("uuid", uniqueId.toString())
                .append("map", mapNumber);

        for (EStatisticType type : values.keySet()) {
            document.append(type.name(), values.get(type));
        }

        return document;
    }
}

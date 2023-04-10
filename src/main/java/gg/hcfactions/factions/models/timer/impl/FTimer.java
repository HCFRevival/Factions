package gg.hcfactions.factions.models.timer.impl;

import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.base.timer.impl.GenericTimer;
import lombok.Getter;
import org.bson.Document;

public final class FTimer extends GenericTimer implements MongoDocument<FTimer> {
    @Getter public ETimerType type;

    public FTimer() {
        super(0L);
    }

    public FTimer(ETimerType type, long ms) {
        super(ms);
        this.type = type;
    }

    public FTimer(ETimerType type, int s) {
        super(s);
        this.type = type;
    }

    @Override
    public FTimer fromDocument(Document document) {
        this.type = ETimerType.fromString(document.getString("type"));
        this.expire = document.getLong("expire");
        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("type", type.name())
                .append("expire", getExpire());
    }
}

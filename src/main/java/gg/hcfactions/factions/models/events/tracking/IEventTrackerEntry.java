package gg.hcfactions.factions.models.events.tracking;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;

public interface IEventTrackerEntry<T> extends MongoDocument<T> {
    Factions getPlugin();
    EEventTrackerEntryType getType();
    long getTime();
}

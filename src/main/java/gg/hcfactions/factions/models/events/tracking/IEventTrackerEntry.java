package gg.hcfactions.factions.models.events.tracking;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;

public interface IEventTrackerEntry<T> extends MongoDocument<T> {
    /**
     * @return Factions instance
     */
    Factions getPlugin();

    /**
     * @return Tracker Entry Type
     */
    EEventTrackerEntryType getType();

    /**
     * @return Timestamp this entry occurred at
     */
    long getTime();
}

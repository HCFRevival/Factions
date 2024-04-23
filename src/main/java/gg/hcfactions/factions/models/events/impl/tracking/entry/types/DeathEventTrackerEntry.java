package gg.hcfactions.factions.models.events.impl.tracking.entry.types;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.impl.tracking.entry.EventTrackerEntry;
import gg.hcfactions.factions.models.events.tracking.EEventTrackerEntryType;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

public final class DeathEventTrackerEntry extends EventTrackerEntry {
    @Getter public final UUID slainUniqueId;
    @Getter public final String slainUsername;
    @Getter public final BLocatable location;

    public DeathEventTrackerEntry(Factions plugin, UUID slainUniqueId, String slainUsername, BLocatable location) {
        super(plugin);
        this.type = EEventTrackerEntryType.DEATH;
        this.slainUniqueId = slainUniqueId;
        this.slainUsername = slainUsername;
        this.location = location;
    }

    @Override
    public Document toDocument() {
        final Document doc = super.toDocument();
        doc.append("slain", new Document("id", slainUniqueId.toString()).append("name", slainUsername));
        doc.append("location", location.toDocument());
        return doc;
    }
}

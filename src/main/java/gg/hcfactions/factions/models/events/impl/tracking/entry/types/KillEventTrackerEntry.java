package gg.hcfactions.factions.models.events.impl.tracking.entry.types;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.impl.tracking.entry.EventTrackerEntry;
import gg.hcfactions.factions.models.events.tracking.EEventTrackerEntryType;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

public final class KillEventTrackerEntry extends EventTrackerEntry {
    @Getter public final UUID killerUniqueId;
    @Getter public final String killerUsername;
    @Getter public final UUID slainUniqueId;
    @Getter public final String slainUsername;
    @Getter public final BLocatable location;

    public KillEventTrackerEntry(
            Factions plugin,
            UUID killerUniqueId,
            String killerUsername,
            UUID slainUniqueId,
            String slainUsername,
            BLocatable location
    ) {
        super(plugin);
        this.type = EEventTrackerEntryType.KILL;
        this.killerUniqueId = killerUniqueId;
        this.killerUsername = killerUsername;
        this.slainUniqueId = slainUniqueId;
        this.slainUsername = slainUsername;
        this.location = location;
    }

    @Override
    public Document toDocument() {
        final Document doc = super.toDocument();
        doc.append("killer", new Document().append("id", killerUniqueId.toString()).append("name", killerUsername));
        doc.append("slain", new Document().append("id", slainUniqueId.toString()).append("name", slainUsername));
        doc.append("location", location.toDocument());
        return doc;
    }
}

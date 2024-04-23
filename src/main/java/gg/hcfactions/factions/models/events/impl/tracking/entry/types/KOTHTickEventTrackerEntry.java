package gg.hcfactions.factions.models.events.impl.tracking.entry.types;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.impl.tracking.entry.EventTrackerEntry;
import gg.hcfactions.factions.models.events.tracking.EEventTrackerEntryType;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

public class KOTHTickEventTrackerEntry extends EventTrackerEntry {
    @Getter public final UUID factionId;
    @Getter public final int newTicketCount;

    public KOTHTickEventTrackerEntry(Factions plugin, UUID factionId, int newTicketCount) {
        super(plugin);
        this.type = EEventTrackerEntryType.KOTH_TICK;
        this.factionId = factionId;
        this.newTicketCount = newTicketCount;
    }

    @Override
    public Document toDocument() {
        final Document doc = super.toDocument();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionById(factionId);

        doc.append("faction", new Document().append("id", factionId.toString()).append("name", faction.getName()));
        doc.append("new_ticket_count", newTicketCount);

        return doc;
    }
}

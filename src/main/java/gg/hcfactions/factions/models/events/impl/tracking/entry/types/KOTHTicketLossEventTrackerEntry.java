package gg.hcfactions.factions.models.events.impl.tracking.entry.types;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.impl.tracking.entry.EventTrackerEntry;
import gg.hcfactions.factions.models.events.tracking.EEventTrackerEntryType;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

public final class KOTHTicketLossEventTrackerEntry extends EventTrackerEntry {
    @Getter public final UUID factionId;
    @Getter public final int ticketsLost;

    public KOTHTicketLossEventTrackerEntry(Factions plugin, UUID factionId, int ticketsLost) {
        super(plugin);
        this.type = EEventTrackerEntryType.KOTH_TICKET_LOSS;
        this.factionId = factionId;
        this.ticketsLost = ticketsLost;
    }

    @Override
    public Document toDocument() {
        final Document doc = super.toDocument();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionById(factionId);

        doc.append("faction", new Document().append("id", factionId.toString()).append("name", faction.getName()));
        doc.append("tickets_lost", ticketsLost);

        return doc;
    }
}

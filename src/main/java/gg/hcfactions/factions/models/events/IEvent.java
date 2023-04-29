package gg.hcfactions.factions.models.events;

import gg.hcfactions.factions.Factions;

import java.util.UUID;

public interface IEvent extends IEventSession {
    Factions getPlugin();

    /**
     * @return Server Faction UUID
     */
    UUID getOwner();

    /**
     * @return Internal event name
     */
    String getName();

    /**
     * @return Display event name
     */
    String getDisplayName();
}

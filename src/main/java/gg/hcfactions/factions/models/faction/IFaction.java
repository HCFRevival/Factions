package gg.hcfactions.factions.models.faction;

import java.util.UUID;

public interface IFaction {
    UUID getUniqueId();
    String getName();
    void setName(String name);
}

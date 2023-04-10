package gg.hcfactions.factions.models.faction;

import gg.hcfactions.libs.bukkit.location.impl.PLocatable;

import java.util.UUID;

public interface IFaction {
    UUID getUniqueId();
    String getName();
    PLocatable getHomeLocation();

    void setName(String name);
    void setHomeLocation(PLocatable location);
}

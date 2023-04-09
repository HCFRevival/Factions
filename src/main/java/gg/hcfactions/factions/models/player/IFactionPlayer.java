package gg.hcfactions.factions.models.player;

import gg.hcfactions.factions.models.econ.IBankable;

import java.util.UUID;

public interface IFactionPlayer extends IBankable {
    /**
     * @return Bukkit UUID
     */
    UUID getUniqueId();

    /**
     * @return Bukkit Username
     */
    String getUsername();
}

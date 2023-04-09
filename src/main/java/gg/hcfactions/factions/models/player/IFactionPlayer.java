package gg.hcfactions.factions.models.player;

import java.util.UUID;

public interface IFactionPlayer {
    /**
     * @return Bukkit UUID
     */
    UUID getUniqueId();

    /**
     * @return Bukkit Username
     */
    String getUsername();
}

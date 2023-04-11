package gg.hcfactions.factions.stats;

import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.entity.Player;

public interface IStatsExecutor {
    /**
     * View a player's statistics
     * @param viewer Inventory viewer
     * @param username Username to perform query with
     * @param promise Promise
     */
    void openPlayerStats(Player viewer, String username, Promise promise);

    /**
     * View a faction's statistics
     * @param viewer Inventory view
     * @param factionName Faction name to perform query with
     * @param promise Promise
     */
    void openFactionStats(Player viewer, String factionName, Promise promise);
}

package gg.hcfactions.factions.displays;

import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.entity.Player;

public interface IDisplayExecutor {
    /**
     * Create a new leaderboard display
     * @param player Player
     * @param type Statistic Type
     */
    void createLeaderboardDisplay(Player player, EStatisticType type);

    /**
     * Delete all displayables within a provided radius of the player
     * @param player Player
     * @param radius Radius
     * @param promise Promise
     */
    void deleteDisplay(Player player, double radius, Promise promise);
}

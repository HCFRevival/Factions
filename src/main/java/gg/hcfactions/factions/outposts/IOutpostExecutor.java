package gg.hcfactions.factions.outposts;

import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.entity.Player;

public interface IOutpostExecutor {
    /**
     * Adds a new material to the Outpost block list
     * @param materialName Material Name
     * @param promise Promise
     */
    void addBlock(String materialName, Promise promise);

    /**
     * Removes an existing materail from the Outpost block list
     * @param materialName Material Name
     * @param promise Promise
     */
    void removeBlock(String materialName, Promise promise);

    /**
     * List all blocks currently active with Outpost claims
     * @param player Player
     */
    void listBlocks(Player player);

    /**
     * Reschedule the Outpost Restock time to the provided time
     * @param player Player
     * @param duration Duration
     * @param promise Promise
     */
    void rescheduleReset(Player player, String duration, Promise promise);

    /**
     * Reset a specific outpost block type
     * @param materialName Material name
     * @param promise Promise
     */
    void resetBlock(String materialName, Promise promise);

    /**
     * Resets all outpost blocks
     * @param promise Promise
     */
    void resetAllBlocks(Promise promise);
}

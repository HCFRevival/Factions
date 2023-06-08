package gg.hcfactions.factions.crowbar;

import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ICrowbarExecutor {
    CrowbarManager getManager();

    /**
     * Performs crowbar use
     * @param player Player
     * @param item Crowbar Item (in hand)
     * @param block Block to perform crowbar action on
     * @param promise Promise
     */
    void useCrowbar(Player player, ItemStack item, Block block, Promise promise);
}

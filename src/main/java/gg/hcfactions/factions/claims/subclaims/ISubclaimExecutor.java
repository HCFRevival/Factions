package gg.hcfactions.factions.claims.subclaims;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface ISubclaimExecutor {
    /**
     * Add a new player to the subclaim
     * @param player Adding player
     * @param subclaimName Subclaim Name
     * @param username Username being added
     * @param promise Promise
     */
    void addToSubclaim(Player player, String subclaimName, String username, Promise promise);

    /**
     * Remove a player from an existing subclaim
     * @param player Removing Player
     * @param subclaimName Subclaim Name
     * @param username Username being removed
     * @param promise Promise
     */
    void removeFromSubclaim(Player player, String subclaimName, String username, Promise promise);

    /**
     * Start the subclaiming process
     * @param player Player
     * @param subclaimName Subclaim name
     * @param promise Promise
     */
    void startSubclaiming(Player player, String subclaimName, Promise promise);

    /**
     * Start subclaiming for a named faction
     * @param player Player
     * @param factionName Faction Name
     * @param subclaimName Subclaim Name
     * @param promise Promise
     */
    void startSubclaiming(Player player, String factionName, String subclaimName, Promise promise);

    /**
     * Creates a new chest subclaim
     * @param player Player
     * @param block Sign Block
     * @param promise Promise
     */
    void createChestSubclaim(Player player, Block block, Promise promise);

    /**
     * Removes a subclaim
     * @param player Player
     * @param subclaimName Subclaim name to remove
     * @param promise Promise
     */
    void unsubclaim(Player player, String subclaimName, Promise promise);

    /**
     * Print a list of all available subclaims
     * @param player Player
     * @param promise Promise
     */
    void listSubclaims(Player player, Promise promise);

    /**
     * Print a list of all available subclaims for a faction
     * @param player Player
     * @param faction Faction
     * @param promise Promise
     */
    void listSubclaims(Player player, PlayerFaction faction, Promise promise);
}

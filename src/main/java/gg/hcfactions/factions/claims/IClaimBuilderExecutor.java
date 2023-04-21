package gg.hcfactions.factions.claims;

import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.entity.Player;

public interface IClaimBuilderExecutor {
    /**
     * Starts the claiming process for the players faction
     * @param player Bukkit Player
     * @param promise Promise
     */
    void startClaiming(Player player, Promise promise);

    /**
     * Starts the claiming process for a named faction
     * @param player Bukkit Player
     * @param faction Faction to claim for
     * @param promise Promise
     */
    void startClaiming(Player player, IFaction faction, Promise promise);
}

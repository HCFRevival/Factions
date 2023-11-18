package gg.hcfactions.factions.events.builder;

import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.entity.Player;

public interface IEventBuilderExecutor {
    /**
     * Start building a new capture event
     * @param player Player
     * @param eventName Event Name
     * @param isPalace If true, the event will be treated as a Palace event and saved to file as so
     * @param promise Promise
     */
    void buildCaptureEvent(Player player, String eventName, boolean isPalace, Promise promise);

    /**
     * Create a new Conquest event
     * @param player Player
     * @param eventName Event Name
     * @param displayName Event Display Name
     * @param serverFactionName Server Faction name
     * @param promise Promise
     */
    void buildConquestEvent(Player player, String eventName, String displayName, String serverFactionName, Promise promise);

    /**
     * Start building a new conquest event
     * @param player Player
     * @param eventName Event Name
     * @param promise Promise
     */
    void buildConquestZone(Player player, String eventName, String zoneName, Promise promise);

    /**
     * Start building a new DPS event
     * @param player Player
     * @param eventName Event Name
     * @param promise Promise
     */
    void buildDpsEvent(Player player, String eventName, Promise promise);

    /**
     * Cancel the building process
     * @param player Player
     * @param promise Promise
     */
    void cancelBuilding(Player player, Promise promise);
}

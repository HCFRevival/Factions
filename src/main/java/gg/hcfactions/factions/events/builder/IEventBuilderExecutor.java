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
     * Cancel the building process
     * @param player Player
     * @param promise Promise
     */
    void cancelBuilding(Player player, Promise promise);
}

package gg.hcfactions.factions.events;

import gg.hcfactions.factions.models.events.EPalaceLootTier;
import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.entity.Player;

public interface IEventExecutor {
    /**
     * @return Event Manager
     */
    EventManager getManager();

    /**
     * Start a Capture Event with specified rules
     * @param player Player
     * @param eventName Event Name
     * @param ticketsToWin Tickets to win
     * @param timerDuration Timer duration
     * @param tokenReward Token reward
     * @param tickCheckpointInterval Tick checkpoint interval
     * @param promise Promise
     */
    void startCaptureEvent(Player player, String eventName, int ticketsToWin, int timerDuration, int tokenReward, int tickCheckpointInterval, Promise promise);

    /**
     * Start a Conquest Event with specified rules
     * @param player Player
     * @param eventName Event Name
     * @param ticketsToWin Tickets to win
     * @param timerDuration Timer duration
     * @param tokenReward Token reward
     * @param ticketsPerTick Tickets to send per tick
     * @param promise Promise
     */
    void startConquestEvent(Player player, String eventName, int ticketsToWin, int timerDuration, int tokenReward, int ticketsPerTick, Promise promise);

    /**
     * Start a new DPS event
     * @param player Player
     * @param eventName Event name
     * @param entityTypeName DPS Check custom entity type
     * @param duration Event duration (in seconds)
     * @param tokenReward Token reward
     * @param promise Promise
     */
    void startDpsEvent(Player player, String eventName, String entityTypeName, int duration, int tokenReward, Promise promise);

    /**
     * Alter a Capture Event that is already running
     * @param player Player
     * @param eventName Event Name
     * @param ticketsToWin Tickets to win
     * @param timerDuration Timer duration
     * @param tokenReward Token reward
     * @param tickCheckpointInterval Tick checkpoint interval
     * @param promise Promise
     */
    void setCaptureEventConfig(Player player, String eventName, int ticketsToWin, int timerDuration, int tokenReward, int tickCheckpointInterval, Promise promise);

    /**
     * Alter a Capture Event's leaderboard
     * @param player Player
     * @param eventName Event Name
     * @param factionName Player Faction name
     * @param tickets Tickets to update to
     * @param promise Promise
     */
    void setCaptureLeaderboard(Player player, String eventName, String factionName, int tickets, Promise promise);

    /**
     * Stop an event
     * @param player Player
     * @param eventName Event name
     * @param promise Promise
     */
    void stopEvent(Player player, String eventName, Promise promise);

    /**
     * Delete an event
     * @param player Player
     * @param eventName Event name
     * @param promise Promise
     */
    void deleteEvent(Player player, String eventName, Promise promise);

    /**
     * Delete a Conquest Capture Zone
     * @param player Player
     * @param eventName Conquest Event Name
     * @param zoneName Conquest Zone Name
     * @param promise Promise
     */
    void deleteZone(Player player, String eventName, String zoneName, Promise promise);

    /**
     * Add a scheduled start time to an event
     * @param player Player
     * @param eventName Event name
     * @param day Day
     * @param hour Hour
     * @param minute Minute
     * @param temp If true schedule will not be saved
     * @param promise Promise
     */
    void addEventSchedule(Player player, String eventName, int day, int hour, int minute, boolean temp, Promise promise);

    /**
     * Remove a scheduled start time from an event
     * @param player Player
     * @param eventName Event name
     * @param day Day of week
     * @param hour Hour of day
     * @param minute Minute of hour
     * @param temp If true schedule will not be saved
     * @param promise Promise
     */
    void removeEventSchedule(Player player, String eventName, int day, int hour, int minute, boolean temp, Promise promise);

    /**
     * Add a new loot item to the Palace Loot Table
     * @param player Player
     * @param tier Loot Tier
     * @param minAmount Min drop amount
     * @param maxAmount Max drop amount
     * @param probability Probability
     * @param promise Promise
     */
    void addPalaceLoot(Player player, EPalaceLootTier tier, int minAmount, int maxAmount, int probability, Promise promise);

    /**
     * Add a new loot chest to a Palace Event
     * @param player Player
     * @param eventName Event name
     * @param tier Loot Tier
     * @param promise Promise
     */
    void addPalaceLootChest(Player player, String eventName, EPalaceLootTier tier, Promise promise);

    /**
     * Opens a new event display menu
     * @param player Player
     * @param promise Promise
     */
    void openEventsMenu(Player player, Promise promise);

    /**
     * Opens a loot viewer for Palace Loot
     * @param player Player
     * @param tier Loot Tier
     * @param promise Promise
     */
    void openPalaceLootMenu(Player player, EPalaceLootTier tier, Promise promise);

    /**
     * Restocks all loot chests within an event
     * @param player Player
     * @param eventName Event name
     * @param broadcast If true this event will trigger a global broadcast
     * @param promise Promise
     */
    void restockPalaceEvent(Player player, String eventName, boolean broadcast, Promise promise);
}


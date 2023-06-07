package gg.hcfactions.factions.models.player;

import gg.hcfactions.factions.models.claim.IPillar;
import gg.hcfactions.factions.models.claim.IShield;
import gg.hcfactions.factions.models.econ.IBankable;
import gg.hcfactions.factions.models.scoreboard.FScoreboard;
import gg.hcfactions.factions.models.timer.ITimeable;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface IFactionPlayer extends IBankable, ITimeable {
    /**
     * @return Bukkit UUID
     */
    UUID getUniqueId();

    /**
     * @return Bukkit Username
     */
    String getUsername();

    /**
     * @return Faction Scoreboard
     */
    FScoreboard getScoreboard();

    /**
     * @return Timers
     */
    Set<FTimer> getTimers();

    /**
     * @return Active Pillars
     */
    Set<IPillar> getPillars();

    /**
     * @return Active Shields
     */
    Set<IShield> getShields();

    /**
     * @param name New username
     */
    void setUsername(String name);

    /**
     * If true the player will be reset with a free setup when they join
     * @return If true player will be reset when they join
     */
    boolean isResetOnJoin();

    /**
     * If true the player will not leave a combat logger when they disconnect
     * @return If true player does not leave combat loggers
     */
    boolean isSafeDisconnect();

    /**
     * Set the players reset flag to the provided state
     * @param b If true the player will be reset when they log in the next time
     */
    void setResetOnJoin(boolean b);

    /**
     * Set the players safe disconnect flag to the provided state
     * @param b If true the player will not spawn a combat logger when they disconnect
     */
    void setSafeDisconnect(boolean b);

    /**
     * Performs initial scoreboard setup
     */
    void setupScoreboard();

    /**
     * Destroy scoreboard data
     */
    void destroyScoreboard();

    /**
     * Add the provided player to a "friendly" team in the scoreboard, rendering their name green
     * @param player Player
     */
    void addToScoreboard(Player player);

    /**
     * Remove the provided player from the "friendly" team in the scoreboard
     * @param player Player
     */
    void removeFromScoreboard(Player player);

    /**
     * Remove all friendly players from scoreboard
     */
    void removeAllFromScoreboard();
}

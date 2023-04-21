package gg.hcfactions.factions.models.player;

import gg.hcfactions.factions.models.claim.IPillar;
import gg.hcfactions.factions.models.econ.IBankable;
import gg.hcfactions.factions.models.timer.ITimeable;
import gg.hcfactions.factions.models.timer.impl.FTimer;

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
     * @return Timers
     */
    Set<FTimer> getTimers();

    /**
     * @return Active Pillars
     */
    Set<IPillar> getPillars();

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
     * Set the players reset flag to the provided state
     * @param b If true the player will be reset when they log in the next time
     */
    void setResetOnJoin(boolean b);
}

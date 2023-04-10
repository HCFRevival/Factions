package gg.hcfactions.factions.models.ticking;

import gg.hcfactions.libs.base.util.Time;

public interface ITickable {
    long getNextTick();
    void setNextTick(long time);
    void tick();

    default boolean canTick() {
        return getNextTick() <= Time.now();
    }
}

package gg.hcfactions.factions.models.timer;

import gg.hcfactions.factions.models.timer.impl.FTimer;

import java.util.Set;

public interface ITimeable {
    Set<FTimer> getTimers();
    void finishTimer(ETimerType type);

    default void addTimer(FTimer timer) {
        getTimers().removeIf(t -> t.getType().equals(timer.getType()));
        getTimers().add(timer);
    }

    default void removeTimer(ETimerType type) {
        getTimers().removeIf(t -> t.getType().equals(type));
    }

    default FTimer getTimer(ETimerType type) {
        return getTimers().stream().filter(t -> t.getType().equals(type)).findFirst().orElse(null);
    }

    default boolean hasTimer(ETimerType type) {
        return getTimers().stream().anyMatch(t -> t.getType().equals(type));
    }
}

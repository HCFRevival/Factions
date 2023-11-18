package gg.hcfactions.factions.events.tick;

import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

public final class DPSTickingTask {
    @Getter public final EventManager manager;
    @Getter @Setter public boolean active;
    @Getter public BukkitTask task;

    public DPSTickingTask(EventManager manager) {
        this.manager = manager;
    }

    public void start() {
        setActive(true);

        task = new Scheduler(manager.getPlugin()).sync(() -> manager.getActiveDpsEvents().forEach(dpsEvent -> {
            if (dpsEvent.shouldEnd()) {
                dpsEvent.captureEvent();
            }
        })).repeat(0L, 20L).run();
    }

    public void stop() {
        setActive(false);

        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}

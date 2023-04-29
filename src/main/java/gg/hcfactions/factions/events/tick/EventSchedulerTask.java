package gg.hcfactions.factions.events.tick;

import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

@AllArgsConstructor
public final class EventSchedulerTask {
    @Getter public EventManager manager;
    @Getter public BukkitTask task;

    public EventSchedulerTask(EventManager manager) {
        this.manager = manager;
    }

    public void start() {
        task = new Scheduler(manager.getPlugin()).async(() -> manager.getEventsThatShouldStart().forEach(event -> {
            if (event instanceof final KOTHEvent koth) {
                if (!manager.getKothTickingTask().isActive()) {
                    manager.getKothTickingTask().start();
                }

                koth.startEvent();
            }
        })).repeat(0L, 60 * 20L).run();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}

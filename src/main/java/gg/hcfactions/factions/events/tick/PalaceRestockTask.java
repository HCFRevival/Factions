package gg.hcfactions.factions.events.tick;

import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

public class PalaceRestockTask {
    @Getter public final EventManager manager;
    @Getter @Setter public BukkitTask task;
    @Getter @Setter public boolean active;

    public PalaceRestockTask(EventManager manager) {
        this.manager = manager;
    }

    public void start() {
        task = new Scheduler(manager.getPlugin()).async(() -> manager.getPlugin().getEventManager().getPalaceEvents()
                .stream()
                .filter(palace -> palace.getCapturingFaction() != null)
                .forEach(PalaceEvent::restock)).repeat(3600 * 20L, 3600 * 20L).run();
    }

    public void stop() {
        setActive(false);

        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}

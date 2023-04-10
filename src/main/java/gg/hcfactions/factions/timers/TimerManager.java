package gg.hcfactions.factions.timers;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.timer.impl.GenericTimer;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

public final class TimerManager implements IManager {
    @Getter public final Factions plugin;
    @Getter @Setter public BukkitTask updateTask;

    public TimerManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        updateTask = new Scheduler(plugin).sync(() -> {
            plugin.getPlayerManager().getPlayerRepository()
                    .stream()
                    .filter(p -> !p.getTimers().isEmpty())
                    .forEach(p -> {
                        p.getTimers().stream().filter(GenericTimer::isExpired).forEach(exp -> p.finishTimer(exp.getType()));
            });

            // total lambda nightmare, streams player factions and expires their timers
            plugin.getFactionManager().getFactionRepository()
                    .stream()
                    .filter(f -> f instanceof PlayerFaction)
                    .filter(pf -> ((PlayerFaction)pf).getTimers().isEmpty())
                    .forEach(pf -> ((PlayerFaction) pf).getTimers().stream().filter(GenericTimer::isExpired).forEach(exp -> ((PlayerFaction)pf).finishTimer(exp.getType())));
        }).repeat(0L, 1L).run();
    }

    @Override
    public void onDisable() {
        updateTask.cancel();
        updateTask = null;
    }
}

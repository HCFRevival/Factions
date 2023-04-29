package gg.hcfactions.factions.events.tick;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;

public final class KOTHTickingTask {
    @Getter public final EventManager manager;
    @Getter @Setter public boolean active;
    @Getter public BukkitTask task;

    public KOTHTickingTask(EventManager manager) {
        this.manager = manager;
    }

    public void start() {
        setActive(true);

        task = new Scheduler(manager.getPlugin()).sync(() -> {
            for (KOTHEvent event : manager.getActiveKothEvents()) {
                final Set<UUID> playersInCapzone = Sets.newHashSet();
                final Set<PlayerFaction> factionsInCapzone = Sets.newHashSet();

                if (event.getSession().getTimer().isExpired()) {
                    event.getSession().getTimer().finish();
                }

                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (event.getCaptureRegion().isInside(new PLocatable(player), false)) {
                        playersInCapzone.add(player.getUniqueId());
                    }
                });

                if (playersInCapzone.isEmpty() && event.getSession().getCapturingFaction() != null) {
                    event.getSession().reset();
                    continue;
                }

                playersInCapzone.forEach(uuid -> {
                    final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(uuid);

                    if (faction != null) {
                        factionsInCapzone.add(faction);
                    }
                });

                if (factionsInCapzone.isEmpty() && event.getSession().getCapturingFaction() != null) {
                    event.getSession().reset();
                    continue;
                }

                if (event.getSession().getCapturingFaction() != null && !factionsInCapzone.contains(event.getSession().getCapturingFaction())) {
                    event.getSession().reset();
                    continue;
                }

                if (factionsInCapzone.size() >= 2) {
                    if (event.getSession().isContested()) {
                        continue;
                    }

                    event.getSession().setContested(factionsInCapzone);
                    continue;
                }

                if (factionsInCapzone.size() == 1 && factionsInCapzone.contains(event.getSession().getCapturingFaction())) {
                    if (event.getSession().isContested()) {
                        event.getSession().setUncontested(false);
                    }

                    continue;
                }

                if (event.getSession().getCapturingFaction() == null) {
                    factionsInCapzone.stream().findFirst().ifPresent(f -> {
                        event.getSession().getTimer().setFrozen(false);
                        event.getSession().getTimer().setExpire(Time.now() + (event.getSession().getTimerDuration() * 1000L));
                        event.getSession().setCapturingFaction(f);
                        FMessage.broadcastCaptureEventMessage(event.getDisplayName() + FMessage.LAYER_1 + " is being controlled by " + FMessage.LAYER_2 + f.getName());
                    });
                }
            }
        }).repeat(0L, 10L).run();
    }

    public void stop() {
        setActive(false);

        if (task != null) {
            task.cancel();
            task = null;
        }
    }

}

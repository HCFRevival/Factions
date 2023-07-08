package gg.hcfactions.factions.events.tick;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.models.events.impl.ConquestZone;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;

public final class ConquestTickingTask {
    @Getter public final EventManager manager;
    @Getter @Setter public boolean active;
    @Getter public BukkitTask task;

    public ConquestTickingTask(EventManager manager) {
        this.manager = manager;
    }

    public void start() {
        setActive(true);

        task = new Scheduler(manager.getPlugin()).sync(() -> {
            final ConquestEvent conquest = manager.getActiveConquestEvent();

            if (conquest == null) {
                return;
            }

            /* if (!conquest.isActive()) {
                stop();
                return;
            } */

            for (ConquestZone zone : conquest.getZones()) {
                final Set<UUID> playersInCapzone = Sets.newHashSet();
                final Set<PlayerFaction> factionsInCapzone = Sets.newHashSet();

                if (zone.getTimer() == null) {
                    continue;
                }

                if (zone.getTimer().isExpired()) {
                    zone.getTimer().finish();
                    continue;
                }

                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (zone.getCaptureRegion().isInside(new PLocatable(player), false)) {
                        playersInCapzone.add(player.getUniqueId());
                    }
                });

                if (playersInCapzone.isEmpty() && zone.getCapturingFaction() != null) {
                    zone.getEvent().getSession().reset(zone);
                    continue;
                }

                playersInCapzone.forEach(uuid -> {
                    final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(uuid);

                    if (faction != null) {
                        factionsInCapzone.add(faction);
                    }
                });

                if (factionsInCapzone.isEmpty() && zone.getCapturingFaction() != null) {
                    conquest.getSession().reset(zone);
                    continue;
                }

                if (zone.getCapturingFaction() != null && !factionsInCapzone.contains(zone.getCapturingFaction())) {
                    conquest.getSession().reset(zone);
                    continue;
                }

                if (factionsInCapzone.size() >= 2) {
                    if (zone.isContested()) {
                        continue;
                    }

                    conquest.getSession().setContested(zone, factionsInCapzone);
                    continue;
                }

                if (factionsInCapzone.size() == 1 && factionsInCapzone.contains(zone.getCapturingFaction())) {
                    if (zone.isContested()) {
                        conquest.getSession().setUncontested(zone, false);
                    }

                    continue;
                }

                if (zone.getCapturingFaction() == null) {
                    factionsInCapzone.stream().findFirst().ifPresent(f -> {
                        zone.getTimer().setFrozen(false);
                        zone.getTimer().setExpire(Time.now() + (conquest.getSession().getTimerDuration() * 1000L));
                        zone.setCapturingFaction(f);

                        // TODO: Send "controlled by" message here?
                    });
                }
            }
        }).repeat(0L, 20L).run();
    }

    public void stop() {
        setActive(false);

        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
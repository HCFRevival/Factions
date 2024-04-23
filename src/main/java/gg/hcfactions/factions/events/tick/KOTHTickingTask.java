package gg.hcfactions.factions.events.tick;

import com.google.common.collect.Maps;
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

import java.util.Map;
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
                final Map<UUID, Set<UUID>> inCapzone = Maps.newHashMap();

                if (event.getSession().getTimer().isExpired()) {
                    event.getSession().getTimer().finish();
                }

                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (event.getCaptureRegion().isInside(new PLocatable(player), false)) {
                        final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(player);

                        if (faction == null) {
                            return;
                        }

                        if (!inCapzone.containsKey(faction.getUniqueId())) {
                            final Set<UUID> ids = Sets.newHashSet();
                            ids.add(player.getUniqueId());
                            inCapzone.put(faction.getUniqueId(), ids);
                            return;
                        }

                        final Set<UUID> ids = inCapzone.get(faction.getUniqueId());
                        ids.add(player.getUniqueId());
                        inCapzone.put(faction.getUniqueId(), ids);
                    }
                });

                // Cap zone is empty, capturing faction is not null but it should be
                if (inCapzone.isEmpty() && event.getSession().getCapturingFaction() != null) {
                    event.getSession().reset();
                    continue;
                }

                // Capturing faction is not null and the capturing faction is not in the cap zone anymore
                if (event.getSession().getCapturingFaction() != null && !inCapzone.containsKey(event.getSession().getCapturingFaction().getUniqueId())) {
                    event.getSession().reset();
                    continue;
                }

                final boolean shouldBeContested = event.getSession().shouldBeContested(inCapzone);

                // event should be contested and is not yet marked as contested
                if (shouldBeContested && !event.getSession().isContested()) {
                    final Set<PlayerFaction> contestingFactions = Sets.newHashSet();

                    inCapzone.forEach((fid, pids) -> {
                        if (!fid.equals(event.getSession().getCapturingFaction().getUniqueId())) {
                            final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionById(fid);
                            contestingFactions.add(faction);
                        }
                    });

                    event.getSession().setContested(contestingFactions);
                    continue;
                }

                // event should not be contested
                if (!shouldBeContested) {
                    // capturing faction is not null and they are still capturing the event
                    if (
                            event.getSession().isContested()
                            && event.getSession().getCapturingFaction() != null
                            && inCapzone.containsKey(event.getSession().getCapturingFaction().getUniqueId())
                    ) {
                        event.getSession().setUncontested(false);
                        continue;
                    }

                    // TODO: We may need it to reset here again?
                }

                // Capturing faction is null, set a new one
                if (event.getSession().getCapturingFaction() == null) {
                    inCapzone.keySet().stream().findFirst().ifPresent(fid -> {
                        final PlayerFaction f = manager.getPlugin().getFactionManager().getPlayerFactionById(fid);
                        event.getSession().getTimer().setFrozen(false);
                        event.getSession().getTimer().setExpire(Time.now() + (event.getSession().getTimerDuration() * 1000L));
                        event.getSession().setCapturingFaction(f);

                        if (event.getSession().getNextNotificationTime() <= Time.now()) {
                            FMessage.broadcastCaptureEventMessage(event.getDisplayName() + FMessage.LAYER_1 + " is being controlled by " + FMessage.LAYER_2 + f.getName());
                        }

                        event.getSession().setNextNotificationTime(Time.now() + 5000L);
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

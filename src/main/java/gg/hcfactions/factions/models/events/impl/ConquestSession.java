package gg.hcfactions.factions.models.events.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.models.events.IEventSession;
import gg.hcfactions.factions.models.events.impl.tracking.ConquestEventTracker;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.*;

@Getter
public final class ConquestSession implements IEventSession {
    public final ConquestEvent event;
    public ConquestEventTracker tracker;
    public final Map<UUID, Integer> leaderboard;
    @Setter public boolean active;
    @Setter public int ticketsNeededToWin;
    @Setter public int ticketsPerTick;
    @Setter public int timerDuration;
    @Setter public int tokenReward;
    @Setter public long nextNotificationTime;

    public ConquestSession(ConquestEvent event, int ticketsNeededToWin, int ticketsPerTick, int timerDuration, int tokenReward) {
        this.event = event;
        this.ticketsNeededToWin = ticketsNeededToWin;
        this.ticketsPerTick = ticketsPerTick;
        this.timerDuration = timerDuration;
        this.tokenReward = tokenReward;
        this.nextNotificationTime = Time.now();
        this.leaderboard = Maps.newConcurrentMap();
        this.tracker = new ConquestEventTracker(event);
    }

    public int getTickets(PlayerFaction faction) {
        return leaderboard.getOrDefault(faction.getUniqueId(), 0);
    }

    public ImmutableMap<UUID, Integer> getSortedLeaderboard() {
        final List<Map.Entry<UUID, Integer>> collected = new LinkedList<>(leaderboard.entrySet());
        collected.sort(Map.Entry.comparingByValue());
        Collections.reverse(collected);

        final Map<UUID, Integer> sorted = Maps.newLinkedHashMap();
        collected.forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));

        return ImmutableMap.copyOf(sorted);
    }

    public void setContested(ConquestZone zone, Set<PlayerFaction> factions) {
        final List<String> names = Lists.newArrayList();
        factions.forEach(f -> names.add(f.getName()));

        zone.setContested(true);
        zone.getTimer().setFrozen(true);

        if (nextNotificationTime <= Time.now()) {
            FMessage.broadcastConquestMessage(zone.getDisplayName() + FMessage.LAYER_1 + " is being contested by " + FMessage.LAYER_2 + Joiner.on(", ").join(names));
            nextNotificationTime = (Time.now() + 5000L);
        }
    }

    public void setUncontested(ConquestZone zone, boolean reset) {
        if (reset) {
            reset(zone);
            return;
        }

        zone.getTimer().setFrozen(false);
        zone.setContested(false);

        if (nextNotificationTime <= Time.now()) {
            FMessage.broadcastConquestMessage(zone.getDisplayName() + FMessage.LAYER_1 + " is being controlled by " + FMessage.LAYER_2 + zone.getCapturingFaction().getName());
            nextNotificationTime = (Time.now() + 5000L);
        }
    }

    public void reset(ConquestZone zone) {
        zone.setCapturingFaction(null);
        zone.setContested(false);

        zone.getTimer().setExpire(Time.now() + (timerDuration * 1000L));
        zone.getTimer().setFrozen(true);

        if (nextNotificationTime <= Time.now()) {
            FMessage.broadcastConquestMessage(zone.getDisplayName() + FMessage.LAYER_1 + " has been reset");
            nextNotificationTime = (Time.now() + 5000L);
        }
    }

    public void tick(ConquestZone zone) {
        final int existingTickets = getTickets(zone.getCapturingFaction());
        final int newTickets = existingTickets + ticketsPerTick;

        leaderboard.put(zone.getCapturingFaction().getUniqueId(), newTickets);

        if (newTickets >= ticketsNeededToWin) {
            event.captureEvent(zone.getCapturingFaction());
            return;
        }

        FMessage.broadcastConquestMessage(FMessage.LAYER_2 + zone.getCapturingFaction().getName() + FMessage.LAYER_1 + " has gained " + FMessage.LAYER_2 + ticketsPerTick + " tickets" + FMessage.LAYER_1 + " for controlling " + zone.getDisplayName() + ChatColor.RED + " (" + newTickets + "/" + ticketsNeededToWin + ")");
        zone.getTimer().setExpire(Time.now() + (timerDuration * 1000L));
    }
}

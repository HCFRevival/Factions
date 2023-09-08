package gg.hcfactions.factions.models.events.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.events.event.KOTHTickEvent;
import gg.hcfactions.factions.models.events.IEventSession;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.stream.Collectors;

public final class KOTHSession implements IEventSession {
    @Getter public final KOTHEvent event;
    @Getter @Setter public boolean active;
    @Getter public boolean contested;
    @Getter @Setter public int ticketsNeededToWin;
    @Getter @Setter public int timerDuration;
    @Getter @Setter public int tokenReward;
    @Getter @Setter public int tickCheckpointInterval;
    @Getter @Setter public long nextNotificationTime;
    @Getter @Setter public PlayerFaction capturingFaction;
    @Getter public final Map<UUID, Integer> leaderboard;
    @Getter public final KOTHTimer timer;

    public KOTHSession(KOTHEvent event, int ticketsNeededToWin, int timerDuration, int tokenReward, int tickCheckpointInterval) {
        this.event = event;
        this.active = false;
        this.ticketsNeededToWin = ticketsNeededToWin;
        this.timerDuration = timerDuration;
        this.tokenReward = tokenReward;
        this.tickCheckpointInterval = tickCheckpointInterval;
        this.nextNotificationTime = Time.now();
        this.capturingFaction = null;
        this.leaderboard = Maps.newConcurrentMap();
        this.timer = new KOTHTimer(event, timerDuration);
        this.timer.setFrozen(true);
    }

    public boolean isCaptured() {
        return capturingFaction != null && !active;
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

    /**
     * Calculates each ticket checkpoint and stores them
     * an in array. This list can be used to determine if
     * a faction has exceeded a set checkpoint
     * @return List of Ints
     */
    public List<Integer> getTickCheckpoints() {
        final List<Integer> res = Lists.newArrayList();

        if (getTickCheckpointInterval() <= 0) {
            return res;
        }

        final int rounded = (int)Math.ceil((double)ticketsNeededToWin / tickCheckpointInterval);
        int cursor = rounded;

        while (cursor <= ticketsNeededToWin) {
            res.add(cursor);
            cursor += rounded;
        }

        return res;
    }

    /**
     * Calculates the closest ticket checkpoint
     * for the provided tickets to determine the
     * 'true zero'
     *
     * @param currentTickets Current ticket count before subtraction
     * @return Max ticket loss
     */
    public int getTicketLossFloor(int currentTickets) {
        final List<Integer> checkpoints = getTickCheckpoints();

        if (checkpoints.isEmpty()) {
            return 0;
        }

        for (int i = checkpoints.size() - 1; i >= 0; i--) {
            final int floorValue = checkpoints.get(i);

            if (currentTickets >= floorValue) {
                return floorValue;
            }
        }

        return 0;
    }

    public void setContested(Set<PlayerFaction> factions) {
        final List<String> names = Lists.newArrayList();
        factions.forEach(f -> names.add(f.getName()));

        contested = true;
        timer.setFrozen(true);

        if (nextNotificationTime <= Time.now()) {
            FMessage.broadcastCaptureEventMessage(event.getDisplayName() + FMessage.LAYER_1 + " is being contested by " + FMessage.LAYER_2 + Joiner.on(", ").join(names));
            nextNotificationTime = (Time.now() + 5000L);
        }
    }

    public void setUncontested(boolean reset) {
        if (reset) {
            reset();
            return;
        }

        timer.setFrozen(false);
        contested = false;

        if (nextNotificationTime <= Time.now()) {
            FMessage.broadcastCaptureEventMessage(event.getDisplayName() + FMessage.LAYER_1 + " is being controlled by " + FMessage.LAYER_2 + capturingFaction.getName());
            nextNotificationTime = (Time.now() + 5000L);
        }
    }

    public void reset() {
        capturingFaction = null;
        contested = false;

        timer.setExpire(Time.now() + (timerDuration * 1000L));
        timer.setFrozen(true);

        if (nextNotificationTime <= Time.now()) {
            FMessage.broadcastCaptureEventMessage(event.getDisplayName() + FMessage.LAYER_1 + " has been reset");
            nextNotificationTime = (Time.now() + 5000L);
        }
    }

    public void tick(PlayerFaction faction) {
        final int existingTickets = getTickets(faction);
        final int newTickets = existingTickets + 1;

        if (newTickets >= getTicketsNeededToWin()) {
            event.captureEvent(faction);
            return;
        }

        final KOTHTickEvent tickEvent = new KOTHTickEvent(getEvent());
        Bukkit.getPluginManager().callEvent(tickEvent);

        leaderboard.put(faction.getUniqueId(), newTickets);
        FMessage.broadcastCaptureEventMessage(FMessage.LAYER_2 + faction.getName() + FMessage.LAYER_1 + " has gained a ticket for controlling " + event.getDisplayName() + ChatColor.RED + " (" + newTickets + "/" + ticketsNeededToWin + ")");

        for (UUID otherFactionId : leaderboard.keySet().stream().filter(f -> !f.equals(faction.getUniqueId())).collect(Collectors.toList())) {
            final PlayerFaction otherFaction = event.getPlugin().getFactionManager().getPlayerFactionById(otherFactionId);
            final int currentTickets = getTickets(otherFaction);
            final int subtractedTickets = Math.max(currentTickets - 1, getTicketLossFloor(currentTickets));

            if (subtractedTickets <= 0) {
                leaderboard.remove(otherFactionId);

                otherFaction.sendMessage(" ");
                otherFaction.sendMessage(FMessage.KOTH_PREFIX + "Your faction is no longer on the leaderboard for " + event.getDisplayName());
                otherFaction.sendMessage(" ");
                continue;
            }

            if (subtractedTickets != currentTickets) {
                leaderboard.put(otherFactionId, subtractedTickets);

                otherFaction.sendMessage(" ");
                otherFaction.sendMessage(FMessage.KOTH_PREFIX + "Your faction now has " + FMessage.LAYER_2 + subtractedTickets + " tickets" + FMessage.LAYER_1 + " on the leaderboard for " + event.getDisplayName());
                otherFaction.sendMessage(" ");
            }
        }

        timer.setExpire(Time.now() + (timerDuration * 1000L));
    }
}

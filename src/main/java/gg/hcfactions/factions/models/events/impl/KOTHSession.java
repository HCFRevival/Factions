package gg.hcfactions.factions.models.events.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.events.event.KOTHTickEvent;
import gg.hcfactions.factions.models.events.IEventSession;
import gg.hcfactions.factions.models.events.impl.tracking.KOTHEventTracker;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public final class KOTHSession implements IEventSession {
    public final KOTHEvent event;
    public boolean contested;
    public final Map<UUID, Integer> leaderboard;
    public final KOTHTimer timer;
    public KOTHEventTracker tracker;
    @Setter public boolean active;
    @Setter public int ticketsNeededToWin;
    @Setter public int timerDuration;
    @Setter public int tokenReward;
    @Setter public int tickCheckpointInterval;
    @Setter public int contestedThreshold;
    @Setter public int onlinePlayerLimit;
    @Setter public boolean majorityTurnoverEnabled;
    @Setter public boolean suddenDeathEnabled;
    @Setter public long nextNotificationTime;
    @Setter public PlayerFaction capturingFaction;

    public KOTHSession(KOTHEvent event, CaptureEventConfig config) {
        this.event = event;
        this.active = false;
        this.ticketsNeededToWin = config.getDefaultTicketsNeededToWin();
        this.timerDuration = config.getDefaultTimerDuration();
        this.tokenReward = config.getTokenReward();
        this.tickCheckpointInterval = config.getTickCheckpointInterval();
        this.contestedThreshold = config.getContestedThreshold();
        this.onlinePlayerLimit = config.getOnlinePlayerLimit();
        this.majorityTurnoverEnabled = config.isMajorityTurnoverEnabled();
        this.suddenDeathEnabled = config.isSuddenDeathEnabled();
        this.nextNotificationTime = Time.now();
        this.capturingFaction = null;
        this.leaderboard = Maps.newConcurrentMap();
        this.timer = new KOTHTimer(event, timerDuration);
        this.timer.setFrozen(true);
        this.tracker = new KOTHEventTracker(event);
    }

    public KOTHSession(
            KOTHEvent event,
            int ticketsNeededToWin,
            int timerDuration,
            int tokenReward,
            int tickCheckpointInterval,
            int contestedThreshold,
            int onlinePlayerLimit
    ) {
        this.event = event;
        this.active = false;
        this.ticketsNeededToWin = ticketsNeededToWin;
        this.timerDuration = timerDuration;
        this.tokenReward = tokenReward;
        this.tickCheckpointInterval = tickCheckpointInterval;
        this.contestedThreshold = contestedThreshold;
        this.onlinePlayerLimit = onlinePlayerLimit;
        this.nextNotificationTime = Time.now();
        this.capturingFaction = null;
        this.leaderboard = Maps.newConcurrentMap();
        this.timer = new KOTHTimer(event, timerDuration);
        this.timer.setFrozen(true);
        this.tracker = new KOTHEventTracker(event);
    }

    public boolean isCaptured() {
        return capturingFaction != null && !active;
    }

    public boolean hasContestThreshold() {
        return contestedThreshold > 1;
    }

    public boolean isExceedingPlayerLimit(PlayerFaction faction) {
        if (onlinePlayerLimit <= 0) {
            return false;
        }

        int onlineCount = FactionUtil.getOnlineAlliesCount(faction);

        return onlineCount >= onlinePlayerLimit;
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

    /**
     * Calculates and returns the provided Player Faction's
     * current tick checkpoint they are on
     *
     * @param faction PlayerFaction to query
     * @return Checkpoint value
     */
    public int getFactionTickCheckpoint(PlayerFaction faction) {
        if (tickCheckpointInterval <= 0) {
            return -1;
        }

        List<Integer> checkpoints = getTickCheckpoints();

        if (checkpoints.isEmpty()) {
            return -1;
        }

        int currentTickets = getTickets(faction);

        if (currentTickets <= 0) {
            return -1;
        }

        for (int i = checkpoints.size() - 1; i >= 0; i--) {
            final int checkpointValue = checkpoints.get(i);

            if (currentTickets >= checkpointValue) {
                return i;
            }
        }

        return -1;
    }

    public boolean shouldBeContested(Map<UUID, Set<UUID>> inCapzone) {
        if (capturingFaction == null) {
            return false;
        }

        boolean isLastTick = (getTickCheckpoints().size() - 1) <= getFactionTickCheckpoint(capturingFaction);
        if (isSuddenDeathEnabled() && isLastTick) {
            return false;
        }

        if (!hasContestThreshold()) {
            return (inCapzone.size() > 1);
        }

        final Set<UUID> capturingFactionCount = inCapzone.get(capturingFaction.getUniqueId());
        final int contestRequirement = (int)Math.floor(Math.round((double)(capturingFactionCount.size() / contestedThreshold)));

        for (UUID factionId : inCapzone.keySet()) {
            PlayerFaction faction = event.getPlugin().getFactionManager().getPlayerFactionById(factionId);

            if (faction == null) {
                continue;
            }

            if (isExceedingPlayerLimit(faction)) {
                continue;
            }

            if (factionId.equals(capturingFaction.getUniqueId())) {
                continue;
            }

            final Set<UUID> playerIds = inCapzone.get(factionId);

            if (playerIds.size() >= contestRequirement) {
                return true;
            }
        }

        return false;
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

        final KOTHTickEvent tickEvent = new KOTHTickEvent(getEvent(), newTickets);
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

    public void updateConfig(CaptureEventConfig config) {
        this.ticketsNeededToWin = config.getDefaultTicketsNeededToWin();
        this.timerDuration = config.getDefaultTimerDuration();
        this.tokenReward = config.getTokenReward();
        this.tickCheckpointInterval = config.getTickCheckpointInterval();
        this.contestedThreshold = config.getContestedThreshold();
        this.majorityTurnoverEnabled = config.isMajorityTurnoverEnabled();
        this.suddenDeathEnabled = config.isSuddenDeathEnabled();
    }
}

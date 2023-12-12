package gg.hcfactions.factions.models.events.impl.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.EventStartEvent;
import gg.hcfactions.factions.models.events.*;
import gg.hcfactions.factions.models.events.impl.*;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.ILocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public final class ConquestEvent implements IEvent, IMultiCaptureEvent, IScheduledEvent, ILootableEvent {
    @Getter public final Factions plugin;
    @Getter @Setter public boolean active;
    @Getter @Setter public UUID owner;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter @Setter public UUID capturingFaction;
    @Getter @Setter public long nextRestockTime;
    @Getter @Setter public int restockInterval;
    @Getter @Setter public ConquestEventConfig config;
    @Getter public List<ConquestZone> zones;
    @Getter @Setter public Map<EPalaceLootTier, Long> lootUnlockTimes;
    @Getter @Setter public List<PalaceLootChest> lootChests;
    @Getter public final List<EventSchedule> schedule;
    @Getter @Setter public ConquestSession session;

    public ConquestEvent(Factions plugin, UUID owner, String name, String displayName, ConquestEventConfig config, List<EventSchedule> schedule) {
        this.plugin = plugin;
        this.active = false;
        this.owner = owner;
        this.name = name;
        this.displayName = displayName;
        this.config = config;
        this.schedule = schedule;
        this.session = null;
        this.zones = Lists.newArrayList();
        this.lootUnlockTimes = Maps.newHashMap();
        this.lootChests = Lists.newArrayList();
    }

    public Optional<ConquestZone> getZoneAt(ILocatable location) {
        return zones.stream().filter(z -> z.getCaptureRegion().isInside(location, false)).findFirst();
    }

    public void initZone(String name, String displayName, CaptureRegion region) {
        zones.add(new ConquestZone(this, name, displayName, region));
    }

    public List<ConquestZone> getZonesByAlphabetical() {
        final List<ConquestZone> sorted = Lists.newArrayList(zones);
        sorted.sort(Comparator.comparing(ConquestZone::getName));
        return sorted;
    }

    @Override
    public void captureEvent(PlayerFaction faction) {
        session.setActive(false);
        setActive(false); // TODO: Remove one of these, it's redundant

        // reset zones
        zones.forEach(z -> {
            z.setTimer(null);
            z.setCapturingFaction(null);
        });

        capturingFaction = faction.getUniqueId();

        FMessage.broadcastConquestMessage(displayName + FMessage.LAYER_1 + " has been captured by " + FMessage.LAYER_2 + faction.getName());

        new Scheduler(plugin).async(() -> {
            final LocalDate date = LocalDate.now();
            final LocalDate monday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
            final LocalDate wednesday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
            final LocalDate friday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
            final ZoneOffset offset = ZoneId.of("America/New_York").getRules().getOffset(Instant.now());

            lootUnlockTimes.put(EPalaceLootTier.T3, monday.atStartOfDay().toInstant(offset).toEpochMilli());
            lootUnlockTimes.put(EPalaceLootTier.T2, wednesday.atStartOfDay().toInstant(offset).toEpochMilli());
            lootUnlockTimes.put(EPalaceLootTier.T1, friday.atStartOfDay().toInstant(offset).toEpochMilli());

            plugin.getEventManager().saveConquestEvent(this);
        }).run();
    }

    @Override
    public void startEvent() {
        startEvent(config.getDefaultTicketsNeededToWin(), config.getDefaultTimerDuration(), config.getTokenReward(), config.getTicketsPerTick());
    }

    @Override
    public void startEvent(int ticketsNeededToWin, int timerDuration, int tokenReward, int ticketsPerTick) {
        setActive(true); // TODO: Remove this as it's redundant

        session = new ConquestSession(this, ticketsNeededToWin, ticketsPerTick, timerDuration, tokenReward);
        session.setActive(true);

        // Set up zone timers
        zones.forEach(zone -> {
            final ConquestTimer timer = new ConquestTimer(zone, timerDuration);
            timer.setFrozen(true);
            zone.setTimer(timer);
        });

        Bukkit.getPluginManager().callEvent(new EventStartEvent(this));

        FMessage.broadcastConquestMessage(displayName + FMessage.LAYER_1 + " is now open");
    }

    @Override
    public void stopEvent() {
        session.setActive(false);
        setActive(false); // TODO: Remove one of these, it's redundant

        // reset zones
        zones.forEach(z -> {
            z.setTimer(null);
            z.setCapturingFaction(null);
        });
    }

    @Override
    public void restock() {
        restock(true);
    }

    @Override
    public void restock(boolean broadcast) {
        if (lootChests.isEmpty()) {
            return;
        }

        lootChests.forEach(PalaceLootChest::restock);
        nextRestockTime = Time.now() + (restockInterval*1000L);

        if (broadcast) {
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(FMessage.CONQ_PREFIX + displayName + FMessage.LAYER_1 + " has been restocked");
            Bukkit.broadcastMessage(" ");
        }
    }
}

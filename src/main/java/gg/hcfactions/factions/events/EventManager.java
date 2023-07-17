package gg.hcfactions.factions.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.builder.EventBuilderManager;
import gg.hcfactions.factions.events.impl.EventExecutor;
import gg.hcfactions.factions.events.tick.ConquestTickingTask;
import gg.hcfactions.factions.events.tick.EventSchedulerTask;
import gg.hcfactions.factions.events.tick.KOTHTickingTask;
import gg.hcfactions.factions.events.tick.PalaceRestockTask;
import gg.hcfactions.factions.items.EventBuilderWand;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.events.*;
import gg.hcfactions.factions.models.events.impl.*;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public final class EventManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public final EventExecutor executor;
    @Getter public final EventBuilderManager builderManager;
    @Getter public final PalaceLootManager palaceLootManager;
    @Getter public final List<IEvent> eventRepository;

    @Getter public KOTHTickingTask kothTickingTask;
    @Getter public ConquestTickingTask conqTickingTask;
    @Getter public EventSchedulerTask eventScheduleTask;
    @Getter public PalaceRestockTask palaceRestockTask;

    public EventManager(Factions plugin) {
        this.plugin = plugin;
        this.executor = new EventExecutor(this);
        this.builderManager = new EventBuilderManager(this);
        this.palaceLootManager = new PalaceLootManager(plugin);
        this.eventRepository = Lists.newArrayList();
    }

    @Override
    public void onEnable() {
        loadEvents();

        final CustomItemService cis = (CustomItemService)plugin.getService(CustomItemService.class);

        if (cis == null) {
            plugin.getAresLogger().error("failed to obtain custom item service");
            return;
        }

        cis.registerNewItem(new EventBuilderWand());

        kothTickingTask = new KOTHTickingTask(this);
        conqTickingTask = new ConquestTickingTask(this);
        eventScheduleTask = new EventSchedulerTask(this);
        palaceRestockTask = new PalaceRestockTask(this);

        // TODO: This is super unoptimized, let's make these more dynamic
        conqTickingTask.start();
        eventScheduleTask.start();
        kothTickingTask.start();
        palaceRestockTask.start();

        builderManager.onEnable();
        palaceLootManager.onEnable();
    }

    @Override
    public void onDisable() {
        builderManager.onDisable();
        palaceLootManager.onDisable();

        conqTickingTask.stop();
        palaceRestockTask.stop();
        kothTickingTask.stop();
        eventScheduleTask.stop();

        eventRepository.clear();
    }

    private void loadKoths(YamlConfiguration conf) {
        if (conf.getConfigurationSection("data.koth") != null) {
            for (String eventName : Objects.requireNonNull(conf.getConfigurationSection("data.koth")).getKeys(false)) {
                final String key = "data.koth." + eventName + ".";
                final UUID ownerId = (conf.get(key + "owner") != null ? UUID.fromString(conf.getString(key + "owner")) : null);
                final String displayName = ChatColor.translateAlternateColorCodes('&', conf.getString(key + "display_name"));
                final int ticketsNeeded = conf.getInt(key + "tickets_needed");
                final int timerDuration = conf.getInt(key + "timer_duration");
                final int maxLifespan = conf.getInt(key + "max_lifespan");
                final int tokenReward = conf.getInt(key + "token_reward");

                final String cornerAWorld = conf.getString(key + "capture_region.a.world");
                final double cornerAX = conf.getDouble(key + "capture_region.a.x");
                final double cornerAY = conf.getDouble(key + "capture_region.a.y");
                final double cornerAZ = conf.getDouble(key + "capture_region.a.z");

                final String cornerBWorld = conf.getString(key + "capture_region.b.world");
                final double cornerBX = conf.getDouble(key + "capture_region.b.x");
                final double cornerBY = conf.getDouble(key + "capture_region.b.y");
                final double cornerBZ = conf.getDouble(key + "capture_region.b.z");

                final List<EventSchedule> schedule = Lists.newArrayList();
                final BLocatable cornerA = new BLocatable(cornerAWorld, cornerAX, cornerAY, cornerAZ);
                final BLocatable cornerB = new BLocatable(cornerBWorld, cornerBX, cornerBY, cornerBZ);
                final CaptureRegion captureRegion = new CaptureRegion(cornerA, cornerB);
                final CaptureEventConfig eventConfig = new CaptureEventConfig(ticketsNeeded, timerDuration, maxLifespan, tokenReward);

                for (String dateValue : conf.getStringList(key + "schedule")) {
                    final String[] split = dateValue.split(":");
                    if (split.length != 3) {
                        plugin.getAresLogger().error("bad schedule format: " + dateValue);
                        continue;
                    }

                    int dayOfWeek;
                    int hourOfDay;
                    int minuteOfHour;

                    try {
                        dayOfWeek = Integer.parseInt(split[0]);
                        hourOfDay = Integer.parseInt(split[1]);
                        minuteOfHour = Integer.parseInt(split[2]);
                    } catch (NumberFormatException e) {
                        plugin.getAresLogger().error("bad schedule format", e);
                        continue;
                    }

                    schedule.add(new EventSchedule(hourOfDay, minuteOfHour, dayOfWeek));
                }

                final KOTHEvent event = new KOTHEvent(
                        plugin,
                        ownerId,
                        eventName,
                        displayName,
                        schedule,
                        captureRegion,
                        eventConfig
                );

                eventRepository.add(event);
            }
        }
    }

    private void loadPalaces(YamlConfiguration conf) {
        if (conf.getConfigurationSection("data.palace") != null) {
            for (String eventName : Objects.requireNonNull(conf.getConfigurationSection("data.palace")).getKeys(false)) {
                final String key = "data.palace." + eventName + ".";
                final UUID ownerId = (conf.get(key + "owner") != null ? UUID.fromString(conf.getString(key + "owner")) : null);
                final UUID capturingFaction = (conf.get(key + "capturing_faction") != null ? UUID.fromString(conf.getString(key + "capturing_faction")) : null);
                final String displayName = ChatColor.translateAlternateColorCodes('&', conf.getString(key + "display_name"));
                final int ticketsNeeded = conf.getInt(key + "tickets_needed");
                final int timerDuration = conf.getInt(key + "timer_duration");
                final int maxLifespan = conf.getInt(key + "max_lifespan");
                final int restockInterval = conf.getInt(key + "restock_interval");
                final int tokenReward = conf.getInt(key + "token_reward");

                final String cornerAWorld = conf.getString(key + "capture_region.a.world");
                final double cornerAX = conf.getDouble(key + "capture_region.a.x");
                final double cornerAY = conf.getDouble(key + "capture_region.a.y");
                final double cornerAZ = conf.getDouble(key + "capture_region.a.z");

                final String cornerBWorld = conf.getString(key + "capture_region.b.world");
                final double cornerBX = conf.getDouble(key + "capture_region.b.x");
                final double cornerBY = conf.getDouble(key + "capture_region.b.y");
                final double cornerBZ = conf.getDouble(key + "capture_region.b.z");

                final List<EventSchedule> schedule = Lists.newArrayList();
                final List<PalaceLootChest> lootChests = Lists.newArrayList();
                final Map<EPalaceLootTier, Long> lootUnlockTimes = Maps.newHashMap();
                final BLocatable cornerA = new BLocatable(cornerAWorld, cornerAX, cornerAY, cornerAZ);
                final BLocatable cornerB = new BLocatable(cornerBWorld, cornerBX, cornerBY, cornerBZ);
                final CaptureRegion captureRegion = new CaptureRegion(cornerA, cornerB);
                final CaptureEventConfig eventConfig = new CaptureEventConfig(ticketsNeeded, timerDuration, maxLifespan, tokenReward);

                for (String dateValue : conf.getStringList(key + "schedule")) {
                    final String[] split = dateValue.split(":");
                    if (split.length != 3) {
                        plugin.getAresLogger().error("bad schedule format: " + dateValue);
                        continue;
                    }

                    int dayOfWeek;
                    int hourOfDay;
                    int minuteOfHour;

                    try {
                        dayOfWeek = Integer.parseInt(split[0]);
                        hourOfDay = Integer.parseInt(split[1]);
                        minuteOfHour = Integer.parseInt(split[2]);
                    } catch (NumberFormatException e) {
                        plugin.getAresLogger().error("bad schedule format", e);
                        continue;
                    }

                    schedule.add(new EventSchedule(hourOfDay, minuteOfHour, dayOfWeek));
                }

                // - '-103:54:102:world
                for (EPalaceLootTier tier : EPalaceLootTier.values()) {
                    if (conf.get(key + "chests." + tier.getName()) == null) {
                        continue;
                    }

                    for (String chestValue : conf.getStringList(key + "chests." + tier.getName())) {
                        final String[] split = chestValue.split(":");

                        if (split.length != 4) {
                            plugin.getAresLogger().error("bad chest location: " + chestValue);
                            continue;
                        }

                        final double x = Double.parseDouble(split[0]);
                        final double y = Double.parseDouble(split[1]);
                        final double z = Double.parseDouble(split[2]);
                        final String worldName = split[3];
                        final BLocatable location = new BLocatable(worldName, x, y, z);

                        lootChests.add(new PalaceLootChest(this, location, tier));
                    }
                }

                if (conf.get(key + "unlock_times") != null) {
                    for (EPalaceLootTier tier : EPalaceLootTier.values()) {
                        final long unlockTime = conf.getLong(key + "unlock_times." + tier.name);
                        lootUnlockTimes.put(tier, unlockTime);
                    }
                }

                final PalaceEvent event = new PalaceEvent(
                        plugin,
                        ownerId,
                        eventName,
                        displayName,
                        schedule,
                        captureRegion,
                        capturingFaction,
                        restockInterval,
                        lootUnlockTimes,
                        lootChests,
                        eventConfig
                );

                eventRepository.add(event);
            }
        }
    }

    public void loadConquests(YamlConfiguration conf) {
        if (conf.getConfigurationSection("data.conquest") != null) {
            for (String eventName : Objects.requireNonNull(conf.getConfigurationSection("data.conquest")).getKeys(false)) {
                final String key = "data.conquest." + eventName + ".";
                final UUID ownerId = (conf.get(key + "owner") != null ? UUID.fromString(conf.getString(key + "owner")) : null);
                final UUID capturingFaction = (conf.get(key + "capturing_faction") != null ? UUID.fromString(conf.getString(key + "capturing_faction")) : null);
                final String displayName = ChatColor.translateAlternateColorCodes('&', conf.getString(key + "display_name"));
                final int ticketsNeeded = conf.getInt(key + "tickets_needed");
                final int timerDuration = conf.getInt(key + "timer_duration");
                final int maxLifespan = conf.getInt(key + "max_lifespan");
                final int restockInterval = conf.getInt(key + "restock_interval");
                final int ticketsPerTick = conf.getInt(key + "tickets_per_tick");
                final int tokenReward = conf.getInt(key + "token_reward");

                final List<EventSchedule> schedule = Lists.newArrayList();
                final List<PalaceLootChest> lootChests = Lists.newArrayList();
                final Map<EPalaceLootTier, Long> lootUnlockTimes = Maps.newHashMap();

                for (String dateValue : conf.getStringList(key + "schedule")) {
                    final String[] split = dateValue.split(":");
                    if (split.length != 3) {
                        plugin.getAresLogger().error("bad schedule format: " + dateValue);
                        continue;
                    }

                    int dayOfWeek;
                    int hourOfDay;
                    int minuteOfHour;

                    try {
                        dayOfWeek = Integer.parseInt(split[0]);
                        hourOfDay = Integer.parseInt(split[1]);
                        minuteOfHour = Integer.parseInt(split[2]);
                    } catch (NumberFormatException e) {
                        plugin.getAresLogger().error("bad schedule format", e);
                        continue;
                    }

                    schedule.add(new EventSchedule(hourOfDay, minuteOfHour, dayOfWeek));
                }

                // - '-103:54:102:world
                for (EPalaceLootTier tier : EPalaceLootTier.values()) {
                    if (conf.get(key + "chests." + tier.getName()) == null) {
                        continue;
                    }

                    for (String chestValue : conf.getStringList(key + "chests." + tier.getName())) {
                        final String[] split = chestValue.split(":");

                        if (split.length != 4) {
                            plugin.getAresLogger().error("bad chest location: " + chestValue);
                            continue;
                        }

                        final double x = Double.parseDouble(split[0]);
                        final double y = Double.parseDouble(split[1]);
                        final double z = Double.parseDouble(split[2]);
                        final String worldName = split[3];
                        final BLocatable location = new BLocatable(worldName, x, y, z);

                        lootChests.add(new PalaceLootChest(this, location, tier));
                    }
                }

                if (conf.get(key + "unlock_times") != null) {
                    for (EPalaceLootTier tier : EPalaceLootTier.values()) {
                        final long unlockTime = conf.getLong(key + "unlock_times." + tier.name);
                        lootUnlockTimes.put(tier, unlockTime);
                    }
                }

                final ConquestEventConfig config = new ConquestEventConfig(ticketsNeeded, timerDuration, (3600*8), tokenReward, ticketsPerTick);
                final ConquestEvent event = new ConquestEvent(plugin, ownerId, eventName, displayName, config, schedule);

                event.setRestockInterval(restockInterval);
                event.setCapturingFaction(capturingFaction);

                for (String zoneName : Objects.requireNonNull(conf.getConfigurationSection(key + "zones")).getKeys(false)) {
                    final String zoneKey = key + "zones." + zoneName + ".";
                    final String zoneDisplayName = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(conf.getString(zoneKey + "display_name")));
                    final double ax = conf.getDouble(zoneKey + "region.a.x");
                    final double ay = conf.getDouble(zoneKey + "region.a.y");
                    final double az = conf.getDouble(zoneKey + "region.a.z");
                    final String aWorld = conf.getString(zoneKey + "region.a.world");

                    final double bx = conf.getDouble(zoneKey + "region.b.x");
                    final double by = conf.getDouble(zoneKey + "region.b.y");
                    final double bz = conf.getDouble(zoneKey + "region.b.z");
                    final String bWorld = conf.getString(zoneKey + "region.b.world");

                    final BLocatable cornerA = new BLocatable(aWorld, ax, ay, az);
                    final BLocatable cornerB = new BLocatable(bWorld, bx, by, bz);

                    final CaptureRegion region = new CaptureRegion(cornerA, cornerB);

                    event.initZone(zoneName, zoneDisplayName, region);
                }

                eventRepository.add(event);
            }
        }
    }

    public void loadEvents() {
        final YamlConfiguration conf = plugin.loadConfiguration("events");

        loadKoths(conf);
        loadPalaces(conf);
        loadConquests(conf);

        plugin.getAresLogger().info("loaded " + eventRepository.size() + " events");
    }

    public void saveCaptureEvent(IEvent event) {
        final YamlConfiguration conf = plugin.loadConfiguration("events");

        if (event instanceof final KOTHEvent kothEvent) {
            final String key = (event instanceof PalaceEvent)
                    ? "data.palace." + kothEvent.getName() + "."
                    : "data.koth." + kothEvent.getName() + ".";

            if (event.getOwner() != null) {
                conf.set(key + "owner", event.getOwner().toString());
            }

            conf.set(key + "display_name", event.getDisplayName());
            conf.set(key + "tickets_needed", kothEvent.getEventConfig().getDefaultTicketsNeededToWin());
            conf.set(key + "timer_duration", kothEvent.getEventConfig().getDefaultTimerDuration());
            conf.set(key + "max_lifespan", kothEvent.getEventConfig().getMaxLifespan());
            conf.set(key + "token_reward", kothEvent.getEventConfig().getTokenReward());

            conf.set(key + "capture_region.a.world", kothEvent.getCaptureRegion().getCornerA().getWorldName());
            conf.set(key + "capture_region.a.x", kothEvent.getCaptureRegion().getCornerA().getX());
            conf.set(key + "capture_region.a.y", kothEvent.getCaptureRegion().getCornerA().getY());
            conf.set(key + "capture_region.a.z", kothEvent.getCaptureRegion().getCornerA().getZ());

            conf.set(key + "capture_region.b.world", kothEvent.getCaptureRegion().getCornerB().getWorldName());
            conf.set(key + "capture_region.b.x", kothEvent.getCaptureRegion().getCornerB().getX());
            conf.set(key + "capture_region.b.y", kothEvent.getCaptureRegion().getCornerB().getY());
            conf.set(key + "capture_region.b.z", kothEvent.getCaptureRegion().getCornerB().getZ());

            if (!kothEvent.getSchedule().isEmpty()) {
                final List<String> entries = Lists.newArrayList();

                for (EventSchedule schedule : kothEvent.getSchedule()) {
                    entries.add(schedule.getDay() + ":" + schedule.getHour() + ":" + schedule.getMinute());
                }

                conf.set(key + "schedule", entries);
            }

            if (kothEvent instanceof final PalaceEvent palaceEvent) {
                conf.set(key + "restock_interval", palaceEvent.getRestockInterval());
                conf.set(key + "capturing_faction", palaceEvent.getCapturingFaction() != null ? palaceEvent.getCapturingFaction().toString() : null);

                if (!palaceEvent.getLootUnlockTimes().isEmpty()) {
                    palaceEvent.getLootUnlockTimes().forEach((tier, timestamp) -> conf.set(key + "unlock_times." + tier.name, timestamp));
                } else {
                    conf.set(key + "unlock_times", null);
                }

                for (EPalaceLootTier tier : EPalaceLootTier.values()) {
                    final List<PalaceLootChest> chests = palaceEvent.getLootChests().stream().filter(plc -> plc.getLootTier().equals(tier)).collect(Collectors.toList());

                    if (chests.isEmpty()) {
                        continue;
                    }

                    final List<String> locations = Lists.newArrayList();

                    chests.forEach(chest -> locations.add(
                            chest.getLocation().getX()
                                    + ":" + chest.getLocation().getY()
                                    + ":" + chest.getLocation().getZ()
                                    + ":" + chest.getLocation().getWorldName()));

                    conf.set(key + "chests." + tier.name, locations);
                }
            }
        }

        plugin.saveConfiguration("events", conf);
    }

    public void saveConquestEvent(ConquestEvent event) {
        final YamlConfiguration conf = plugin.loadConfiguration("events");
        final String key = "data.conquest." + event.getName() + ".";

        conf.set(key + "display_name", event.getDisplayName());
        conf.set(key + "capturing_faction", event.getCapturingFaction() != null ? event.getCapturingFaction().toString() : null);
        conf.set(key + "restock_interval", event.getRestockInterval());
        conf.set(key + "timer_duration", event.getConfig().getDefaultTimerDuration());
        conf.set(key + "tickets_per_tick", event.getConfig().getTicketsPerTick());
        conf.set(key + "token_reward", event.getConfig().getTokenReward());
        conf.set(key + "tickets_needed", event.getConfig().getDefaultTicketsNeededToWin());

        if (event.getLootUnlockTimes().isEmpty()) {
            conf.set(key + "unlock_times", null);
        } else {
            event.getLootUnlockTimes().forEach((tier, timestamp) -> conf.set(key + "unlock_times." + tier.name, timestamp));
        }

        for (EPalaceLootTier tier : EPalaceLootTier.values()) {
            final List<PalaceLootChest> chests = event.getLootChests().stream().filter(plc -> plc.getLootTier().equals(tier)).collect(Collectors.toList());

            if (chests.isEmpty()) {
                continue;
            }

            final List<String> locations = Lists.newArrayList();

            chests.forEach(chest -> locations.add(chest.getLocation().getX() + ":" + chest.getLocation().getY() + ":" + chest.getLocation().getZ() + ":" + chest.getLocation().getWorldName()));
            conf.set(key + "chests." + tier.name, locations);
        }

        for (ConquestZone zone : event.getZones()) {
            final String zoneKey = key + "zones." + zone.getName() + ".";

            conf.set(zoneKey + "display_name", zone.getDisplayName());

            conf.set(zoneKey + "region.a.x", zone.getCaptureRegion().getCornerA().getX());
            conf.set(zoneKey + "region.a.y", zone.getCaptureRegion().getCornerA().getY());
            conf.set(zoneKey + "region.a.z", zone.getCaptureRegion().getCornerA().getZ());
            conf.set(zoneKey + "region.a.world", zone.getCaptureRegion().getCornerA().getWorldName());

            conf.set(zoneKey + "region.b.x", zone.getCaptureRegion().getCornerA().getX());
            conf.set(zoneKey + "region.b.y", zone.getCaptureRegion().getCornerA().getY());
            conf.set(zoneKey + "region.b.z", zone.getCaptureRegion().getCornerA().getZ());
            conf.set(zoneKey + "region.b.world", zone.getCaptureRegion().getCornerA().getWorldName());
        }

        plugin.saveConfiguration("events", conf);
    }

    public void deleteEvent(IEvent event) {
        final YamlConfiguration conf = plugin.loadConfiguration("events");
        String key = "data.koth.";

        if (event instanceof PalaceEvent) {
            key = "data.palace.";
        }

        if (event instanceof ConquestEvent) {
            key = "data.conquest.";
        }

        conf.set(key + event.getName(), null);
        plugin.saveConfiguration("events", conf);
    }

    public Optional<IEvent> getEvent(String name) {
        return eventRepository.stream().filter(e -> e.getName().equalsIgnoreCase(name)).findAny();
    }

    public Optional<IEvent> getEvent(ServerFaction owningFaction) {
        return eventRepository.stream().filter(e -> e.getOwner() != null && e.getOwner().equals(owningFaction.getUniqueId())).findAny();
    }

    public ImmutableList<IScheduledEvent> getEventsThatShouldStart() {
        final List<IScheduledEvent> events = Lists.newArrayList();

        eventRepository.forEach(event -> {
            if (event instanceof final IScheduledEvent scheduledEvent) {
                if (scheduledEvent.shouldStart()) {
                    events.add(scheduledEvent);
                }
            }
        });

        return ImmutableList.copyOf(events);
    }

    public ImmutableList<IEvent> getActiveEvents() {
        return ImmutableList.copyOf(eventRepository.stream().filter(IEventSession::isActive).collect(Collectors.toList()));
    }

    public ImmutableList<KOTHEvent> getActiveKothEvents() {
        final List<KOTHEvent> koths = Lists.newArrayList();
        eventRepository.stream().filter(e -> e.isActive() && e instanceof KOTHEvent).forEach(koth -> koths.add((KOTHEvent) koth));
        return ImmutableList.copyOf(koths);
    }

    public ConquestEvent getActiveConquestEvent() {
        return (ConquestEvent) eventRepository.stream().filter(e -> e.isActive() && e instanceof ConquestEvent).findFirst().orElse(null);
    }

    public ImmutableList<PalaceEvent> getPalaceEvents() {
        final List<PalaceEvent> palaces = Lists.newArrayList();
        eventRepository.stream().filter(e -> e instanceof PalaceEvent).forEach(palace -> palaces.add((PalaceEvent) palace));
        return ImmutableList.copyOf(palaces);
    }

    public ImmutableList<ConquestEvent> getConquestEvents() {
        final List<ConquestEvent> conquests = Lists.newArrayList();
        eventRepository.stream().filter(e -> e instanceof ConquestEvent).forEach(conq -> conquests.add((ConquestEvent) conq));
        return ImmutableList.copyOf(conquests);
    }

    public ImmutableList<IEvent> getEventsAlphabeticalOrder() {
        final List<IEvent> events = Lists.newArrayList(eventRepository);
        events.sort(Comparator.comparing(IEvent::getName));
        return ImmutableList.copyOf(events);
    }
}

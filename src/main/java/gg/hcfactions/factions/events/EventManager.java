package gg.hcfactions.factions.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.builder.EventBuilderManager;
import gg.hcfactions.factions.events.impl.EventExecutor;
import gg.hcfactions.factions.events.tick.EventSchedulerTask;
import gg.hcfactions.factions.events.tick.KOTHTickingTask;
import gg.hcfactions.factions.items.EventBuilderWand;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.events.*;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
    @Getter public EventSchedulerTask eventScheduleTask;

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
        eventScheduleTask = new EventSchedulerTask(this);

        eventScheduleTask.start();
        kothTickingTask.start();

        builderManager.onEnable();
        palaceLootManager.onEnable();
    }

    @Override
    public void onDisable() {
        builderManager.onDisable();
        palaceLootManager.onDisable();

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

                final String captureChestWorld = conf.getString(key + "capture_chest.world");
                final double captureChestX = conf.getDouble(key + "capture_chest.x");
                final double captureChestY = conf.getDouble(key + "capture_chest.y");
                final double captureChestZ = conf.getDouble(key + "capture_chest.z");

                final String cornerAWorld = conf.getString(key + "capture_region.a.world");
                final double cornerAX = conf.getDouble(key + "capture_region.a.x");
                final double cornerAY = conf.getDouble(key + "capture_region.a.y");
                final double cornerAZ = conf.getDouble(key + "capture_region.a.z");

                final String cornerBWorld = conf.getString(key + "capture_region.b.world");
                final double cornerBX = conf.getDouble(key + "capture_region.b.x");
                final double cornerBY = conf.getDouble(key + "capture_region.b.y");
                final double cornerBZ = conf.getDouble(key + "capture_region.b.z");

                final List<EventSchedule> schedule = Lists.newArrayList();
                final BLocatable captureChest = new BLocatable(captureChestWorld, captureChestX, captureChestY, captureChestZ);
                final BLocatable cornerA = new BLocatable(cornerAWorld, cornerAX, cornerAY, cornerAZ);
                final BLocatable cornerB = new BLocatable(cornerBWorld, cornerBX, cornerBY, cornerBZ);
                final CaptureRegion captureRegion = new CaptureRegion(cornerA, cornerB);
                final CaptureEventConfig eventConfig = new CaptureEventConfig(ticketsNeeded, timerDuration, maxLifespan);

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
                        captureChest,
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
                final String displayName = ChatColor.translateAlternateColorCodes('&', conf.getString(key + "display_name"));
                final int ticketsNeeded = conf.getInt(key + "tickets_needed");
                final int timerDuration = conf.getInt(key + "timer_duration");
                final int maxLifespan = conf.getInt(key + "max_lifespan");
                final int restockInterval = conf.getInt(key + "restock_interval");

                final String captureChestWorld = conf.getString(key + "capture_chest.world");
                final double captureChestX = conf.getDouble(key + "capture_chest.x");
                final double captureChestY = conf.getDouble(key + "capture_chest.y");
                final double captureChestZ = conf.getDouble(key + "capture_chest.z");

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

                final BLocatable captureChest = new BLocatable(captureChestWorld, captureChestX, captureChestY, captureChestZ);
                final BLocatable cornerA = new BLocatable(cornerAWorld, cornerAX, cornerAY, cornerAZ);
                final BLocatable cornerB = new BLocatable(cornerBWorld, cornerBX, cornerBY, cornerBZ);
                final CaptureRegion captureRegion = new CaptureRegion(cornerA, cornerB);
                final CaptureEventConfig eventConfig = new CaptureEventConfig(ticketsNeeded, timerDuration, maxLifespan);

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
                    if (conf.get("chests." + tier.getName()) == null) {
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

                final PalaceEvent event = new PalaceEvent(
                        plugin,
                        ownerId,
                        eventName,
                        displayName,
                        schedule,
                        captureChest,
                        captureRegion,
                        restockInterval,
                        lootChests,
                        eventConfig
                );

                eventRepository.add(event);
            }
        }
    }

    public void loadEvents() {
        final YamlConfiguration conf = plugin.loadConfiguration("events");

        loadKoths(conf);
        loadPalaces(conf);

        plugin.getAresLogger().info("loaded " + eventRepository.size() + " events");
    }

    public void saveEvent(IEvent event) {
        final YamlConfiguration conf = plugin.loadConfiguration("events");

        if (event instanceof final KOTHEvent kothEvent) {
            final String key = (event instanceof PalaceEvent)
                    ? "data.palace." + kothEvent.getName() + "."
                    : "data.koth." + kothEvent.getName() + ".";

            if (event.getOwner() != null) {
                conf.set(key + "owner", event.getOwner().toString());
            }

            conf.set(key + "display_name", event.getDisplayName());
            conf.set(key + "tickets_needed", kothEvent.getEventConfig().defaultTicketsNeededToWin());
            conf.set(key + "timer_duration", kothEvent.getEventConfig().defaultTimerDuration());
            conf.set(key + "max_lifespan", kothEvent.getEventConfig().getMaxLifespan());

            conf.set(key + "capture_chest.world", kothEvent.getCaptureChestLocation().getWorldName());
            conf.set(key + "capture_chest.x", kothEvent.getCaptureChestLocation().getX());
            conf.set(key + "capture_chest.y", kothEvent.getCaptureChestLocation().getY());
            conf.set(key + "capture_chest.z", kothEvent.getCaptureChestLocation().getZ());

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

                if (!palaceEvent.getLootChests().isEmpty()) {
                    final List<String> locations = Lists.newArrayList();

                    palaceEvent.getLootChests().forEach(chest -> locations.add(
                            chest.getLocation().getX()
                                    + ":" + chest.getLocation().getY()
                                    + ":" + chest.getLocation().getZ()
                                    + ":" + chest.getLocation().getWorldName()));

                    conf.set(key + "chests", locations);
                }
            }
        }

        plugin.saveConfiguration("events", conf);
    }

    public void deleteEvent(IEvent event) {
        final YamlConfiguration conf = plugin.loadConfiguration("events");

        if (event instanceof KOTHEvent) {
            final String key = (event instanceof PalaceEvent) ? "data.palace." : "data.koth.";
            conf.set(key + event.getName(), null);
        }

        plugin.saveConfiguration("events", conf);
    }

    public Optional<IEvent> getEvent(String name) {
        return eventRepository.stream().filter(e -> e.getName().equalsIgnoreCase(name)).findAny();
    }

    public Optional<IEvent> getEvent(ServerFaction owningFaction) {
        return eventRepository.stream().filter(e -> e.getOwner().equals(owningFaction.getUniqueId())).findAny();
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

    public Optional<IEvent> getEventByCaptureChest(Location location) {
        return eventRepository.stream().filter(e -> e instanceof ICaptureEvent).filter(ce ->
                        ((ICaptureEvent)ce).getCaptureChestLocation().getX() == location.getX() &&
                        ((ICaptureEvent)ce).getCaptureChestLocation().getY() == location.getY() &&
                        ((ICaptureEvent)ce).getCaptureChestLocation().getZ() == location.getZ() &&
                        ((ICaptureEvent)ce).getCaptureChestLocation().getWorldName().equalsIgnoreCase(Objects.requireNonNull(location.getWorld()).getName())).findAny();
    }

    public ImmutableList<IEvent> getEventsAlphabeticalOrder() {
        final List<IEvent> events = Lists.newArrayList(eventRepository);
        events.sort(Comparator.comparing(IEvent::getName));
        return ImmutableList.copyOf(events);
    }
}

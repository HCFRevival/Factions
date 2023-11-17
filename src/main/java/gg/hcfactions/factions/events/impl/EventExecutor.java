package gg.hcfactions.factions.events.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.events.IEventExecutor;
import gg.hcfactions.factions.events.menu.EventMenu;
import gg.hcfactions.factions.models.events.*;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.ConquestZone;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootable;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.loot.impl.LootTableMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public final class EventExecutor implements IEventExecutor {
    @Getter public EventManager manager;

    @Override
    public void startCaptureEvent(Player player, String eventName, int ticketsToWin, int timerDuration, int tokenReward, int tickCheckpointInterval, Promise promise) {
        final Optional<IEvent> event = manager.getEvent(eventName);

        if (event.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent generic = event.get();

        if (generic.isActive()) {
            promise.reject("This event is already active");
            return;
        }

        if (!(generic instanceof final ICaptureEvent captureEvent)) {
            promise.reject("This is not a Capture Event");
            return;
        }

        captureEvent.startEvent(ticketsToWin, timerDuration, tokenReward, tickCheckpointInterval);
        promise.resolve();
    }

    @Override
    public void startConquestEvent(Player player, String eventName, int ticketsToWin, int timerDuration, int tokenReward, int ticketsPerTick, Promise promise) {
        final Optional<IEvent> event = manager.getEvent(eventName);

        if (event.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent generic = event.get();

        if (generic.isActive()) {
            promise.reject("This event is already active");
            return;
        }

        if (!(generic instanceof final ConquestEvent conquestEvent)) {
            promise.reject("This is not a Conquest Event");
            return;
        }

        conquestEvent.startEvent(ticketsToWin, timerDuration, tokenReward, ticketsPerTick);
        promise.resolve();
    }

    @Override
    public void startDpsEvent(Player player, String eventName, String entityTypeName, String durationName, int tokenReward, Promise promise) {
        final Optional<IEvent> event = manager.getEvent(eventName);

        if (event.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent generic = event.get();

        if (generic.isActive()) {
            promise.reject("This event is already active");
            return;
        }

        if (!(generic instanceof final DPSEvent dpsEvent)) {
            promise.reject("This is not a DPS Event");
            return;
        }

        final EDPSEntityType entityType = EDPSEntityType.getByName(entityTypeName);

        if (entityType == null) {
            promise.reject("Invalid DPS Entity Type");
            return;
        }

        final long duration = Time.parseTime(durationName);

        if (duration <= 0L) {
            promise.reject("Invalid time format");
            return;
        }

        dpsEvent.startEvent(entityType, duration, tokenReward);
        promise.resolve();
    }

    @Override
    public void setCaptureEventConfig(Player player, String eventName, int ticketsToWin, int timerDuration, int tokenReward, int tickCheckpointInterval, Promise promise) {
        final Optional<IEvent> event = manager.getEvent(eventName);

        if (event.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent generic = event.get();

        if (!(generic instanceof final KOTHEvent koth)) {
            promise.reject("Not a capture event");
            return;
        }

        koth.setEventConfig(new CaptureEventConfig(ticketsToWin, timerDuration, tokenReward, tickCheckpointInterval));

        if (koth.isActive()) {
            koth.getSession().setTicketsNeededToWin(ticketsToWin);
            koth.getSession().setTimerDuration(timerDuration);
            koth.getSession().setTokenReward(tokenReward);
            koth.getSession().setTickCheckpointInterval(tickCheckpointInterval);
        }

        promise.resolve();
    }

    @Override
    public void setCaptureLeaderboard(Player player, String eventName, String factionName, int tickets, Promise promise) {
        final Optional<IEvent> eventQuery = manager.getEvent(eventName);

        if (eventQuery.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent generic = eventQuery.get();

        if (!(generic instanceof final KOTHEvent koth)) {
            promise.reject("Not a capture event");
            return;
        }

        if (!koth.isActive() || koth.getSession() == null) {
            promise.reject("This event is not active");
            return;
        }

        if (tickets < 0) {
            promise.reject("Negative values are not supported");
            return;
        }

        if (koth.getSession().getTicketsNeededToWin() >= tickets) {
            promise.reject("You are trying to update this factions ticket count higher than the win condition");
            return;
        }

        final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByName(factionName);

        if (faction == null) {
            promise.reject("Faction not found");
            return;
        }

        faction.sendMessage(FMessage.KOTH_PREFIX + "Your faction's tickets for " + koth.getDisplayName() + FMessage.LAYER_1 + " has been updated to " + FMessage.LAYER_2 + tickets);
        koth.getSession().getLeaderboard().put(faction.getUniqueId(), tickets);
        promise.resolve();
    }

    @Override
    public void stopEvent(Player player, String eventName, Promise promise) {
        final Optional<IEvent> event = manager.getEvent(eventName);

        if (event.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent generic = event.get();

        if (generic instanceof final KOTHEvent koth) {
            if (!koth.isActive()) {
                promise.reject("Event is not active");
                return;
            }

            koth.stopEvent();
            promise.resolve();
            return;
        }

        if (generic instanceof final ConquestEvent conquestEvent) {
            if (!conquestEvent.isActive()) {
                promise.reject("Event is not active");
                return;
            }

            conquestEvent.stopEvent();
            promise.resolve();
            return;
        }

        promise.reject("Unknown event type");
    }

    @Override
    public void deleteEvent(Player player, String eventName, Promise promise) {
        final Optional<IEvent> eventQuery = manager.getEvent(eventName);

        if (eventQuery.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent event = eventQuery.get();

        if (event.isActive()) {
            promise.reject("Please stop the event before deleting it");
            return;
        }

        manager.getEventRepository().remove(event);
        manager.deleteEvent(event);
        promise.resolve();
    }

    @Override
    public void deleteZone(Player player, String eventName, String zoneName, Promise promise) {
        final Optional<IEvent> eventQuery = manager.getEvent(eventName);

        if (eventQuery.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent event = eventQuery.get();

        if (!(event instanceof final ConquestEvent conquestEvent)) {
            promise.reject("This event is not a Conquest Event");
            return;
        }

        final Optional<ConquestZone> zoneQuery = conquestEvent.getZones().stream().filter(z -> z.getName().equalsIgnoreCase(zoneName)).findFirst();

        if (zoneQuery.isEmpty()) {
            promise.reject("Zone not found");
            return;
        }

        final ConquestZone zone = zoneQuery.get();

        conquestEvent.getZones().remove(zone);
        manager.saveConquestEvent(conquestEvent);
        promise.resolve();
    }

    @Override
    public void addEventSchedule(Player player, String eventName, int day, int hour, int minute, boolean temp, Promise promise) {
        final Optional<IEvent> eventQuery = manager.getEvent(eventName);

        if (eventQuery.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent event = eventQuery.get();

        if (!(event instanceof final IScheduledEvent scheduledEvent)) {
            promise.reject("This type of event can not be scheduled");
            return;
        }

        final Optional<EventSchedule> scheduleQuery = scheduledEvent.getScheduleAt(day, hour, minute);

        if (scheduleQuery.isPresent()) {
            promise.reject("This event is already scheduled to start at this time");
            return;
        }

        final EventSchedule schedule = new EventSchedule(hour, minute, day);
        scheduledEvent.getSchedule().add(schedule);

        if (!temp) {
            manager.saveCaptureEvent((IEvent) scheduledEvent);
        }

        promise.resolve();
    }

    @Override
    public void removeEventSchedule(Player player, String eventName, int day, int hour, int minute, boolean temp, Promise promise) {
        final Optional<IEvent> eventQuery = manager.getEvent(eventName);

        if (eventQuery.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent event = eventQuery.get();

        if (!(event instanceof final IScheduledEvent scheduledEvent)) {
            promise.reject("This type of event can not be scheduled");
            return;
        }

        final Optional<EventSchedule> scheduleQuery = scheduledEvent.getScheduleAt(day, hour, minute);

        if (scheduleQuery.isEmpty()) {
            promise.reject("This event is not scheduled to start at this time");
            return;
        }

        scheduledEvent.getSchedule().remove(scheduleQuery.get());

        if (!temp) {
            manager.saveCaptureEvent((IEvent) scheduledEvent);
        }

        promise.resolve();
    }

    @Override
    public void addPalaceLoot(Player player, EPalaceLootTier tier, int minAmount, int maxAmount, int probability, Promise promise) {
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().equals(Material.AIR)) {
            promise.reject("You are not holding an item");
            return;
        }


        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();

        if (hand.getType().equals(Material.ENCHANTED_BOOK)) {
            final EnchantmentStorageMeta encMeta = (EnchantmentStorageMeta) hand.getItemMeta();

            if (encMeta != null) {
                enchantments.putAll(encMeta.getStoredEnchants());
            }
        } else if (hand.getItemMeta() != null) {
            enchantments.putAll(hand.getItemMeta().getEnchants());
        }

        final PalaceLootable lootable = new PalaceLootable(
                UUID.randomUUID().toString(),
                hand.getItemMeta() != null ? hand.getItemMeta().getDisplayName() : null,
                hand.getType(),
                hand.getItemMeta().getLore() != null ? hand.getItemMeta().getLore() : Lists.newArrayList(),
                enchantments,
                minAmount,
                maxAmount,
                probability,
                tier
        );

        manager.getPalaceLootManager().saveItem(lootable);
        manager.getPalaceLootManager().getLootRepository().add(lootable);
        promise.resolve();
    }

    @Override
    public void addPalaceLootChest(Player player, String eventName, EPalaceLootTier tier, Promise promise) {
        final Block block = player.getTargetBlockExact(4);

        if (block == null || !block.getType().equals(Material.CHEST)) {
            promise.reject("You are not looking at a chest");
            return;
        }

        if (block.getLocation().getWorld() == null) {
            promise.reject("Invalid block data");
            return;
        }

        final Optional<IEvent> event = manager.getEvent(eventName);

        if (event.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        if (!(event.get() instanceof final PalaceEvent palaceEvent)) {
            promise.reject("Event is not a Palace Event");
            return;
        }

        if (palaceEvent.getLootChests().stream().anyMatch(pc ->
                pc.getLocation().getX() == block.getLocation().getX()
                        && pc.getLocation().getY() == block.getLocation().getY()
                        && pc.getLocation().getZ() == block.getLocation().getZ()
                        && pc.getLocation().getWorldName().equalsIgnoreCase(block.getLocation().getWorld().getName()))) {
            promise.reject("This block is already a Palace Loot Chest");
            return;
        }

        final BLocatable location = new BLocatable(block);
        final PalaceLootChest chest = new PalaceLootChest(manager, location, tier);

        palaceEvent.getLootChests().add(chest);
        manager.saveCaptureEvent(palaceEvent);
        promise.resolve();
    }

    @Override
    public void openEventsMenu(Player player, Promise promise) {
        if (manager.getEventRepository().isEmpty()) {
            promise.reject("No events found");
            return;
        }

        final EventMenu menu = new EventMenu(manager.getPlugin(), player);
        menu.open();
        promise.resolve();
    }

    @Override
    public void openPalaceLootMenu(Player player, EPalaceLootTier tier, Promise promise) {
        final List<PalaceLootable> res = Lists.newArrayList();

        manager.getPalaceLootManager().getLootRepository()
                .stream()
                .filter(gl -> gl instanceof PalaceLootable)
                .filter(pl -> ((PalaceLootable)pl).getLootTier().equals(tier))
                .forEach(item -> res.add((PalaceLootable) item));

        final LootTableMenu<PalaceLootable> menu = new LootTableMenu<>(manager.getPlugin(), player, manager.getPalaceLootManager(), res);
        menu.open();
    }

    @Override
    public void restockPalaceEvent(Player player, String eventName, boolean broadcast, Promise promise) {
        final PalaceEvent event = manager.getPalaceEvents().stream().filter(pe -> pe.getName().equalsIgnoreCase(eventName)).findFirst().orElse(null);

        if (event == null) {
            promise.reject("Event not found");
            return;
        }

        if (event.getLootChests().isEmpty()) {
            promise.reject("This event does not have any loot chests");
            return;
        }

        event.restock(broadcast);
        promise.resolve();
    }
}

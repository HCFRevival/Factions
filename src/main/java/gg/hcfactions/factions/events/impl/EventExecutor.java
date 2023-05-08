package gg.hcfactions.factions.events.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.events.IEventExecutor;
import gg.hcfactions.factions.events.menu.EventMenu;
import gg.hcfactions.factions.models.events.EPalaceLootTier;
import gg.hcfactions.factions.models.events.ICaptureEvent;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootable;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.loot.impl.LootTableMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public final class EventExecutor implements IEventExecutor {
    @Getter public EventManager manager;

    @Override
    public void startCaptureEvent(Player player, String eventName, int ticketsToWin, int timerDuration, int tokenReward, Promise promise) {
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

        captureEvent.startEvent(ticketsToWin, timerDuration, tokenReward);
        promise.resolve();
    }

    @Override
    public void setCaptureEventConfig(Player player, String eventName, int ticketsToWin, int timerDuration, int tokenReward, Promise promise) {
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

        koth.setEventConfig(new CaptureEventConfig(ticketsToWin, timerDuration, koth.getEventConfig().getMaxLifespan(), tokenReward));
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

        promise.reject("Unknown event type");
    }

    @Override
    public void deleteEvent(Player player, String eventName, Promise promise) {

    }

    @Override
    public void addEventSchedule(Player player, String eventName, int day, int hour, int minute, Promise promise) {

    }

    @Override
    public void addPalaceLoot(Player player, EPalaceLootTier tier, int minAmount, int maxAmount, int probability, Promise promise) {
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().equals(Material.AIR)) {
            promise.reject("You are not holding an item");
            return;
        }

        final PalaceLootable lootable = new PalaceLootable(
                UUID.randomUUID().toString(),
                hand.getItemMeta() != null ? hand.getItemMeta().getDisplayName() : null,
                hand.getType(),
                hand.getItemMeta().getLore() != null ? hand.getItemMeta().getLore() : Lists.newArrayList(),
                hand.getItemMeta().getEnchants(),
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

        final Optional<IEvent> event = manager.getEvent(eventName);

        if (event.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        if (!(event.get() instanceof final PalaceEvent palaceEvent)) {
            promise.reject("Event is not a Palace Event");
            return;
        }

        final BLocatable location = new BLocatable(block);
        final PalaceLootChest chest = new PalaceLootChest(manager, location, tier);

        palaceEvent.getLootChests().add(chest);
        manager.saveEvent(palaceEvent);
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
}

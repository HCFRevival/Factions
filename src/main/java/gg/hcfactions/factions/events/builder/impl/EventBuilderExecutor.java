package gg.hcfactions.factions.events.builder.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.events.builder.EventBuilderManager;
import gg.hcfactions.factions.events.builder.IEventBuilderExecutor;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.builder.IEventBuilder;
import gg.hcfactions.factions.models.events.impl.ConquestEventConfig;
import gg.hcfactions.factions.models.events.impl.builder.ConquestZoneBuilder;
import gg.hcfactions.factions.models.events.impl.builder.KOTHBuilder;
import gg.hcfactions.factions.models.events.impl.builder.PalaceBuilder;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Optional;

@AllArgsConstructor
public final class EventBuilderExecutor implements IEventBuilderExecutor {
    @Getter public EventBuilderManager manager;

    @Override
    public void buildCaptureEvent(Player player, String eventName, boolean isPalace, Promise promise) {
        final Optional<IEventBuilder> existing = manager.getBuilder(player);

        if (existing.isPresent()) {
            promise.reject("You are already building an event");
            return;
        }

        if (manager.getEventManager().getEvent(eventName).isPresent()) {
            promise.reject("Event name is already in use");
            return;
        }

        if (isPalace) {
            final PalaceBuilder builder = new PalaceBuilder(manager.getEventManager().getPlugin(), player.getUniqueId(), eventName);
            manager.getBuilderRepository().add(builder);
            promise.resolve();
            return;
        }

        final KOTHBuilder builder = new KOTHBuilder(manager.getEventManager().getPlugin(), player.getUniqueId(), eventName);
        manager.getBuilderRepository().add(builder);
        promise.resolve();
    }

    @Override
    public void buildConquestEvent(Player player, String eventName, String displayName, String serverFactionName, Promise promise) {
        final Optional<IEvent> eventQuery = manager.getEventManager().getEvent(eventName);

        if (eventQuery.isPresent()) {
            promise.reject("Event name is already in use");
            return;
        }

        final ServerFaction serverFaction = manager.getEventManager().getPlugin().getFactionManager().getServerFactionByName(serverFactionName);

        if (serverFaction == null) {
            promise.reject("Server faction not found. This event requires a Server Claim to be associated with it");
            return;
        }

        final ConquestEvent conquest = new ConquestEvent(
                manager.getEventManager().getPlugin(),
                serverFaction.getUniqueId(),
                eventName,
                ChatColor.translateAlternateColorCodes('&', displayName),
                new ConquestEventConfig(1000, 60, (3600*8), 100, 25),
                Lists.newArrayList()
        );

        manager.getEventManager().getEventRepository().add(conquest);
        manager.getEventManager().saveConquestEvent(conquest);
        promise.resolve();
    }

    @Override
    public void buildConquestZone(Player player, String eventName, String zoneName, Promise promise) {
        final Optional<IEventBuilder> existing = manager.getBuilder(player);

        if (existing.isPresent()) {
            promise.reject("You are already building an event");
            return;
        }

        final Optional<IEvent> eventQuery = manager.getEventManager().getEvent(eventName);

        if (eventQuery.isEmpty()) {
            promise.reject("Event not found");
            return;
        }

        final IEvent event = eventQuery.get();

        if (!(event instanceof final ConquestEvent conqEvent)) {
            promise.reject("This event is not a Conquest Event");
            return;
        }

        final ConquestZoneBuilder builder = new ConquestZoneBuilder(manager.getEventManager().getPlugin(), conqEvent, player.getUniqueId(), zoneName);
        manager.getBuilderRepository().add(builder);
        promise.resolve();
    }

    @Override
    public void cancelBuilding(Player player, Promise promise) {
        final Optional<IEventBuilder> existing = manager.getBuilder(player);

        if (existing.isEmpty()) {
            promise.reject("You are not building an event");
            return;
        }

        manager.getBuilderRepository().remove(existing.get());
        promise.resolve();
    }
}

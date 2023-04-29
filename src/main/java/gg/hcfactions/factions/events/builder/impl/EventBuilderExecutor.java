package gg.hcfactions.factions.events.builder.impl;

import gg.hcfactions.factions.events.builder.EventBuilderManager;
import gg.hcfactions.factions.events.builder.IEventBuilderExecutor;
import gg.hcfactions.factions.models.events.builder.IEventBuilder;
import gg.hcfactions.factions.models.events.impl.builder.KOTHBuilder;
import gg.hcfactions.factions.models.events.impl.builder.PalaceBuilder;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
        promise.resolve();;
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

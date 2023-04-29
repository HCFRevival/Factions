
package gg.hcfactions.factions.events.builder;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.events.builder.impl.EventBuilderExecutor;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.events.builder.IEventBuilder;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class EventBuilderManager implements IManager {
    @Getter public EventManager eventManager;
    @Getter public EventBuilderExecutor executor;
    @Getter public List<IEventBuilder> builderRepository;

    public EventBuilderManager(EventManager eventManager) {
        this.eventManager = eventManager;
        this.executor = new EventBuilderExecutor(this);
        this.builderRepository = Lists.newArrayList();
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        builderRepository.clear();
        builderRepository = null;
    }

    public Optional<IEventBuilder> getBuilder(Player player) {
        return builderRepository.stream().filter(b -> b.getBuilderId().equals(player.getUniqueId())).findAny();
    }
}

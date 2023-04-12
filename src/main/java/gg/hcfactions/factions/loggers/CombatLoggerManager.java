package gg.hcfactions.factions.loggers;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.logger.impl.CombatLogger;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;

import java.util.Set;
import java.util.UUID;

public final class CombatLoggerManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public Set<CombatLogger> loggerRepository;

    public CombatLoggerManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        loggerRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public void onDisable() {
        loggerRepository = null;
    }

    public CombatLogger getLoggerById(UUID uniqueId) {
        return loggerRepository.stream().filter(l -> l.getOwnerId().equals(uniqueId)).findFirst().orElse(null);
    }

    public CombatLogger getLoggerByEntity(LivingEntity entity) {
        return loggerRepository.stream().filter(l -> l.getUUID().equals(entity.getUniqueId())).findFirst().orElse(null);
    }
}

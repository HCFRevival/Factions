package gg.hcfactions.factions.faction;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.faction.impl.FactionExecutor;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.faction.IFaction;
import lombok.Getter;

import java.util.Set;

public final class FactionManager implements IManager {
    @Getter public Factions plugin;
    @Getter public FactionExecutor executor;
    @Getter public Set<IFaction> factionRepository;

    public FactionManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        this.executor = new FactionExecutor(this);
        this.factionRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public void onDisable() {
        this.plugin = null;
        this.executor = null;
        this.factionRepository = null;
    }
}

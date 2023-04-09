package gg.hcfactions.factions.faction;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.faction.impl.FactionExecutor;
import gg.hcfactions.factions.faction.impl.FactionValidator;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class FactionManager implements IManager {
    @Getter public Factions plugin;
    @Getter public FactionValidator validator;
    @Getter public FactionExecutor executor;
    @Getter public Set<IFaction> factionRepository;

    public FactionManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        this.executor = new FactionExecutor(this);
        this.validator = new FactionValidator(this);
        this.factionRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public void onDisable() {
        this.plugin = null;
        this.executor = null;
        this.validator = null;
        this.factionRepository = null;
    }

    public IFaction getFactionById(UUID uniqueId) {
        return factionRepository.stream().filter(f -> f.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public IFaction getFactionByName(String name) {
        return factionRepository.stream().filter(f -> f.getName()
                .equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionById(UUID uniqueId) {
        return (PlayerFaction) factionRepository.stream().filter(f -> f instanceof PlayerFaction && f.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionByName(String name) {
        return (PlayerFaction) factionRepository.stream().filter(f -> f instanceof PlayerFaction && f.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionByPlayer(UUID uniqueId) {
        return (PlayerFaction) factionRepository.stream().filter(f -> f instanceof PlayerFaction && ((PlayerFaction) f).isMember(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionByPlayer(Player player) {
        return getPlayerFactionByPlayer(player.getUniqueId());
    }

    public ServerFaction getServerFactionByName(String name) {
        return (ServerFaction) factionRepository.stream().filter(f -> f instanceof ServerFaction && f.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

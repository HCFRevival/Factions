package gg.hcfactions.factions.player;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class PlayerManager implements IManager {
    @Getter public Factions plugin;
    @Getter public Set<IFactionPlayer> playerRepository;

    public PlayerManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        this.playerRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public void onDisable() {
        this.plugin = null;
        this.playerRepository = null;
    }

    public IFactionPlayer getPlayer(UUID uniqueId) {
        return playerRepository.stream().filter(p -> p.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public IFactionPlayer getPlayer(String username) {
        return playerRepository.stream().filter(p -> p.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public IFactionPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }
}

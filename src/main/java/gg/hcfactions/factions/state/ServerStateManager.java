package gg.hcfactions.factions.state;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.state.EServerState;
import lombok.Getter;

public final class ServerStateManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public EServerState currentState;

    public ServerStateManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        currentState = EServerState.NORMAL;
    }

    @Override
    public void onDisable() {
        currentState = null;
    }
}

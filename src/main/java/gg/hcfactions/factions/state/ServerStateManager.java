package gg.hcfactions.factions.state;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.state.impl.ServerStateExecutor;
import lombok.Getter;
import lombok.Setter;

public final class ServerStateManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public ServerStateExecutor executor;
    @Getter @Setter public EServerState currentState;

    public ServerStateManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        currentState = plugin.getConfiguration().getInitialServerState();
        executor = new ServerStateExecutor(this);
    }

    @Override
    public void onDisable() {
        currentState = null;
        executor = null;
    }
}

package gg.hcfactions.factions.state.impl;

import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.state.IServerStateExecutor;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class ServerStateExecutor implements IServerStateExecutor {
    @Getter public ServerStateManager manager;

    @Override
    public void updateState(EServerState state, Promise promise) {
        if (manager.getCurrentState().equals(state)) {
            promise.reject(FError.A_SERVER_STATE_SAME.getErrorDescription());
            return;
        }
    }
}

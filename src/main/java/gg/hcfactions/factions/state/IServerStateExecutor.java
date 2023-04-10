package gg.hcfactions.factions.state;

import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.libs.base.consumer.Promise;

public interface IServerStateExecutor {
    /**
     * Update the server state to the newly provided state
     * @param state Server State
     * @param promise Promise
     */
    void updateState(EServerState state, Promise promise);
}

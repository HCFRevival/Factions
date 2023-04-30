package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.factions.models.events.IEventSession;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public final class MonsterEventSession implements IEventSession {
    @Getter @Setter public boolean active;
    @Getter @Setter public int tokenReward;
    @Getter @Setter public int playerDeathsInClaim;
    @Getter @Setter public Player capturingPlayer;

    public MonsterEventSession() {
        this.active = false;
        this.tokenReward = 0;
        this.playerDeathsInClaim = 0;
    }

    public MonsterEventSession(int tokenReward) {
        this.active = false;
        this.tokenReward = tokenReward;
        this.playerDeathsInClaim = 0;
        this.capturingPlayer = null;
    }
}

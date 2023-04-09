package gg.hcfactions.factions.models.player.impl;

import gg.hcfactions.factions.models.player.IFactionPlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public final class FactionPlayer implements IFactionPlayer {
    @Getter public UUID uniqueId;
    @Getter public String username;
    @Getter @Setter public double balance;
}

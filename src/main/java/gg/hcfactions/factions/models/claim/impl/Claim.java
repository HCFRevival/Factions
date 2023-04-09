package gg.hcfactions.factions.models.claim.impl;

import gg.hcfactions.factions.models.claim.IClaim;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;

import java.util.UUID;

public final class Claim implements IClaim {
    @Getter public UUID uniqueId;
    @Getter public BLocatable cornerA;
    @Getter public BLocatable cornerB;
}

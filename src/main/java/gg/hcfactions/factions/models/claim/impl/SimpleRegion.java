package gg.hcfactions.factions.models.claim.impl;

import gg.hcfactions.libs.bukkit.location.IRegion;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public final class SimpleRegion implements IRegion {
    @Getter @Setter public BLocatable cornerA;
    @Getter @Setter public BLocatable cornerB;
}

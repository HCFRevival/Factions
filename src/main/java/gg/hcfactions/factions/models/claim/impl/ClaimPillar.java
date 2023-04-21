package gg.hcfactions.factions.models.claim.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.models.claim.EClaimPillarType;
import gg.hcfactions.factions.models.claim.IPillar;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class ClaimPillar implements IPillar {
    @Getter public final Player viewer;
    @Getter public final Material material = Material.GOLD_BLOCK;
    @Getter public final BLocatable position;
    @Getter public EClaimPillarType type;
    @Getter public List<BLocatable> blocks;
    @Getter @Setter public boolean drawn;

    public ClaimPillar(Player viewer, BLocatable position, EClaimPillarType type) {
        this.viewer = viewer;
        this.position = position;
        this.type = type;
        this.blocks = Lists.newArrayList();
        this.drawn = false;
    }
}

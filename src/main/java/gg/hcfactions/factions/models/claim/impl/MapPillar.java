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

public final class MapPillar implements IPillar {
    @Getter public final Player viewer;
    @Getter public final Material material;
    @Getter public final BLocatable position;
    @Getter public EClaimPillarType type;
    @Getter public List<BLocatable> blocks;
    @Getter @Setter public boolean drawn;

    public MapPillar(Player viewer, Material material, BLocatable position, EClaimPillarType type) {
        this.viewer = viewer;
        this.material = material;
        this.position = position;
        this.type = type;
        this.blocks = Lists.newArrayList();
        this.drawn = false;
    }
}

package gg.hcfactions.factions.models.outpost.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.outpost.IOutpostBlock;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

public final class OutpostBlock implements IOutpostBlock {
    @Getter public final Factions plugin;
    @Getter public final Material material;
    @Getter public final List<BLocatable> minedBlocks;

    public OutpostBlock(Factions plugin, Material material) {
        this.plugin = plugin;
        this.material = material;
        this.minedBlocks = Lists.newArrayList();
    }
}

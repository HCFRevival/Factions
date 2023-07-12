package gg.hcfactions.factions.models.outpost;

import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import org.bukkit.Material;

import java.util.List;

public interface IOutpostBlock {
    Material getMaterial();
    List<BLocatable> getMinedBlocks();

    default void reset() {
        if (getMinedBlocks().isEmpty()) {
            return;
        }

        getMinedBlocks().forEach(block -> block.getBukkitBlock().setType(getMaterial()));
        getMinedBlocks().clear();
    }
}

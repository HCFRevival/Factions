package gg.hcfactions.factions.models.claim;

import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface IShield {
    Player getViewer();
    Material getMaterial();
    BLocatable getLocation();
    boolean isDrawn();

    void setDrawn(boolean b);

    default void draw() {
        if (isDrawn()) {
            return;
        }

        getViewer().sendBlockChange(getLocation().getBukkitBlock().getLocation(), getMaterial().createBlockData());
        setDrawn(true);
    }

    default void hide() {
        if (!isDrawn()) {
            return;
        }

        final Block block = getLocation().getBukkitBlock();
        getViewer().sendBlockChange(block.getLocation(), block.getBlockData());
        setDrawn(false);
    }
}

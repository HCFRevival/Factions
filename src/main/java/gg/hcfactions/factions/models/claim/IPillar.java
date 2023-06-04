package gg.hcfactions.factions.models.claim;

import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public interface IPillar {
    Player getViewer();
    Material getMaterial();
    BLocatable getPosition();
    List<BLocatable> getBlocks();
    boolean isDrawn();

    void setDrawn(boolean b);

    default void draw() {
        if (isDrawn()) {
            return;
        }

        // set immediately
        setDrawn(true);

        final int startY = (int)getPosition().getY();
        final int finishY = (startY + 32);

        for (int y = startY; y < finishY; y++) {
            final Block block = getPosition().getBukkitBlock().getWorld().getBlockAt((int)getPosition().getX(), y, (int)getPosition().getZ());

            if (!block.getType().equals(Material.AIR)) {
                continue;
            }

            final BLocatable location = new BLocatable(block);
            getViewer().sendBlockChange(location.getBukkitBlock().getLocation(), (y % 3 == 0 ? getMaterial().createBlockData() : Material.GLASS.createBlockData()));
            getBlocks().add(location);
        }
    }

    default void hide() {
        if (!isDrawn() || getBlocks().isEmpty()) {
            return;
        }

        getBlocks().forEach(b -> {
            final Block bukkitBlock = b.getBukkitBlock();
            getViewer().sendBlockChange(bukkitBlock.getLocation(), bukkitBlock.getBlockData());
        });

        setDrawn(false);
    }
}

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

        final int startY = (int)getPosition().getY();
        final int finishY = (startY + 32);
        int cursor = startY;

        while (cursor < finishY) {
            final Block block = getPosition().getBukkitBlock().getWorld().getBlockAt((int)getPosition().getX(), cursor, (int)getPosition().getZ());

            if (!block.getType().equals(Material.AIR)) {
                cursor += 1;
                continue;
            }

            final BLocatable location = new BLocatable(block);

            getViewer().sendBlockChange(
                    location.getBukkitBlock().getLocation(),
                    (cursor % 3 == 0 ? getMaterial() : Material.GLASS),
                    (byte)0
            );

            getBlocks().add(location);
            cursor += 1;
        }

        setDrawn(true);
    }

    default void hide() {
        if (!isDrawn() || getBlocks().isEmpty()) {
            return;
        }

        getBlocks().forEach(b -> {
            final Block bukkitBlock = b.getBukkitBlock();
            getViewer().sendBlockChange(bukkitBlock.getLocation(), bukkitBlock.getType(), (byte)0);
        });

        setDrawn(false);
    }
}

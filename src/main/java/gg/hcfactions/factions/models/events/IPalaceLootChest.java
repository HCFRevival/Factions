package gg.hcfactions.factions.models.events;

import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

public interface IPalaceLootChest {
    BLocatable getLocation();
    EPalaceLootTier getLootTier();

    void restock();

    default Chest getChest() {
        final Block block = getLocation().getBukkitBlock();

        if (block == null || !block.getType().equals(Material.CHEST)) {
            return null;
        }

        return (Chest) block.getState();
    }
}

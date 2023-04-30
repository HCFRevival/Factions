package gg.hcfactions.factions.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerUtil {
    /**
     * Searches a players inventory to find the first ItemStack matching the provided Material
     * @param player Player
     * @param material Material to search for
     * @return ItemStack matching material in Players inventory
     */
    public static ItemStack getFirstItemStackByMaterial(Player player, Material material) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            if (item.getType().equals(material)) {
                return item;
            }
        }

        return null;
    }
}

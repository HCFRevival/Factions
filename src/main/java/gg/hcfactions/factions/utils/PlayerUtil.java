package gg.hcfactions.factions.utils;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

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

    /**
     * Searches a players inventory to find the first ItemStack that is a Mythic Item
     * and returns true if the Mythic Item is found.
     * @param plugin Factions Plugin
     * @param player Player
     * @return True if player has a Mythic in their inventory
     */
    public static boolean hasMythicItem(Factions plugin, Player player) {
        final CustomItemService cis = (CustomItemService) plugin.getService(CustomItemService.class);

        if (cis == null) {
            return false;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }

            final Optional<ICustomItem> customItemQuery = cis.getItem(item);

            if (customItemQuery.isEmpty()) {
                continue;
            }

            final ICustomItem customItem = customItemQuery.get();

            if (customItem instanceof IMythicItem) {
                return true;
            }
        }

        return false;
    }
}

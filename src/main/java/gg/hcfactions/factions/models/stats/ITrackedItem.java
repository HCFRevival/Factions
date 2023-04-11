package gg.hcfactions.factions.models.stats;

import org.bukkit.inventory.ItemStack;

public interface ITrackedItem<T> {
    /**
     * Returns Bukkit ItemStack being tracked
     * @return ItemStack
     */
    ItemStack getItem();

    /**
     * Marshals data from the item to memory
     * @param item ItemStack
     * @return Tracked Type
     */
    T fromItem(ItemStack item);

    /**
     * Handles updating Bukkit ItemStack with information being tracked
     */
    void updateItem();
}

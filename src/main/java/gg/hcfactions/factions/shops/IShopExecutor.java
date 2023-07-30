package gg.hcfactions.factions.shops;

import gg.hcfactions.factions.models.shop.impl.GenericMerchant;
import gg.hcfactions.factions.models.shop.impl.GenericShop;
import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IShopExecutor {
    /**
     * @return Shop Manager instance
     */
    ShopManager manager();

    /**
     * Trigger onDisable and onEnable for Shops
     */
    void reloadMerchants();

    /**
     * Creates a new merchant
     * @param player Player
     * @param merchantName Merchant name
     * @param isEventMerchant If true the merchant will sell items using event token pricing
     * @param promise Promise
     */
    void createMerchant(Player player, String merchantName, boolean isEventMerchant, Promise promise);

    /**
     * Deletes an existing merchant
     * @param player Player
     * @param merchantName Merchant name
     * @param promise Promise
     */
    void deleteMerchant(Player player, String merchantName, Promise promise);

    /**
     * Open merchant inventory for provided merchant
     * @param player Player
     * @param merchant Merchant
     */
    void openMerchant(Player player, GenericMerchant<?> merchant);

    /**
     * Open a shop inventory for the provided merchant/shop
     * @param player Player
     * @param merchant Merchant (used for backwards navigation)
     * @param shop Shop
     */
    void openShop(Player player, GenericMerchant<?> merchant, GenericShop<?> shop);

    /**
     * Add a new shop to a merchant
     * @param player Player
     * @param merchantName Merchant name
     * @param shopName Shop name
     * @param item ItemStack (material used only)
     * @param position Position
     * @param promise Promise
     */
    void addToMerchant(Player player, String merchantName, String shopName, ItemStack item, int position, Promise promise);

    /**
     * Remove an existing shop from a merchant
     * @param player Player
     * @param merchantName Merchant name
     * @param shopName Shop name
     * @param promise Promise
     */
    void removeFromMerchant(Player player, String merchantName, String shopName, Promise promise);

    /**
     * Adds a new item to a specific shop
     * @param player Player
     * @param merchantName Merchant name
     * @param shopName Shop name
     * @param item Item
     * @param position Shop inventory slot
     * @param buyAmount Buy amount
     * @param sellAmount Sell amount
     * @param promise Promise
     */
    void addToShop(Player player, String merchantName, String shopName, ItemStack item, int position, double buyAmount, double sellAmount, Promise promise);

    /**
     * Adds a new event item to an event merchant's shop
     * @param player Player
     * @param merchantName Merchant name
     * @param shopName Shop name
     * @param item Item to add
     * @param position Position in shop inventory
     * @param tokenAmount Token cost
     * @param promise Promise
     */
    void addToEventShop(Player player, String merchantName, String shopName, ItemStack item, int position, int tokenAmount, Promise promise);

    /**
     * Remove an existing shop item from a shop
     * @param player Player
     * @param merchantName Merchant name
     * @param shopName Shop name
     * @param index Index position
     * @param promise Promise
     */
    void removeFromShop(Player player, String merchantName, String shopName, int index, Promise promise);
}

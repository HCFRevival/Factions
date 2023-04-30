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
     * Creates a new merchant
     * @param player Player
     * @param merchantName Merchant name
     * @param promise Promise
     */
    void createMerchant(Player player, String merchantName, Promise promise);

    /**
     * Deletes an existing merchant
     * @param player Player
     * @param merchant Merchant
     * @param promise Promise
     */
    void deleteMerchant(Player player, GenericMerchant merchant, Promise promise);

    /**
     * Open merchant inventory for provided merchant
     * @param player Player
     * @param merchant Merchant
     */
    void openMerchant(Player player, GenericMerchant merchant);

    /**
     * Open a shop inventory for the provided merchant/shop
     * @param player Player
     * @param merchant Merchant (used for backwards navigation)
     * @param shop Shop
     */
    void openShop(Player player, GenericMerchant merchant, GenericShop shop);

    /**
     * Ad a new shop to a merchant
     * @param player Player
     * @param merchantName Merchant name
     * @param shopName Shop name
     * @param item ItemStack (material used only)
     * @param position Position
     * @param promise Promise
     */
    void addToMerchant(Player player, String merchantName, String shopName, ItemStack item, int position, Promise promise);

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
}

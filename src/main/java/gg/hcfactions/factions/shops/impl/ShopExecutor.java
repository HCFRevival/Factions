package gg.hcfactions.factions.shops.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.models.shop.IMerchant;
import gg.hcfactions.factions.models.shop.impl.GenericMerchant;
import gg.hcfactions.factions.models.shop.impl.GenericShop;
import gg.hcfactions.factions.models.shop.impl.GenericShopItem;
import gg.hcfactions.factions.models.shop.impl.MerchantVillager;
import gg.hcfactions.factions.shops.IShopExecutor;
import gg.hcfactions.factions.shops.ShopManager;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

public record ShopExecutor(@Getter ShopManager manager) implements IShopExecutor {
    @Override
    public void createMerchant(Player player, String merchantName, Promise promise) {
        if (manager.getMerchantByName(ChatColor.stripColor(merchantName)).isPresent()) {
            promise.reject("Merchant name is already in use");
            return;
        }

        final GenericMerchant merchant = new GenericMerchant(
                UUID.randomUUID(),
                ChatColor.translateAlternateColorCodes('&', merchantName),
                new BLocatable(player.getLocation().getBlock()),
                Lists.newArrayList()
        );

        final MerchantVillager villager = new MerchantVillager(manager.getPlugin(), merchant);
        villager.spawn();

        manager.getMerchantRepository().add(merchant);
        manager.getMerchantVillagers().add(villager);
        manager.saveMerchant(merchant);
        promise.resolve();
    }

    @Override
    public void deleteMerchant(Player player, String merchantName, Promise promise) {
        final Optional<IMerchant> merchantQuery = manager.getMerchantByName(merchantName);

        if (merchantQuery.isEmpty()) {
            promise.reject("Merchant not found");
            return;
        }

        final GenericMerchant merchant = (GenericMerchant) merchantQuery.get();
        final MerchantVillager villager = manager.getMerchantVillagers().stream().filter(v -> v.getMerchantId().equals(merchant.getId())).findFirst().orElse(null);

        manager.getMerchantRepository().remove(merchant);

        if (villager != null) {
            villager.remove(Entity.RemovalReason.DISCARDED);
            manager.getMerchantVillagers().remove(villager);
        }

        manager.deleteMerchant(merchant);
        promise.resolve();
    }

    @Override
    public void openMerchant(Player player, GenericMerchant merchant) {
        final MerchantMenu menu = new MerchantMenu(manager.getPlugin(), player, merchant);
        menu.open();
    }

    @Override
    public void openShop(Player player, GenericMerchant merchant, GenericShop shop) {
        final ShopMenu menu = new ShopMenu(manager.getPlugin(), player, merchant, shop);
        menu.open();
    }

    @Override
    public void addToMerchant(Player player, String merchantName, String shopName, ItemStack item, int position, Promise promise) {
        final Optional<IMerchant> merchantQuery = manager.getMerchantByName(merchantName);

        if (merchantQuery.isEmpty()) {
            promise.reject("Merchant not found");
            return;
        }

        final GenericMerchant merchant = (GenericMerchant) merchantQuery.get();

        if (merchant.getShops().stream().anyMatch(s -> ChatColor.stripColor(s.getShopName()).equalsIgnoreCase(ChatColor.stripColor(shopName)))) {
            promise.reject("Shop name is already in use");
            return;
        }

        if (merchant.getShops().stream().anyMatch(s -> s.getPosition() == position)) {
            promise.reject("Inventory position is already in use");
            return;
        }

        final GenericShop shop = new GenericShop(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&', shopName), item.getType(), position, Lists.newArrayList());
        merchant.getShops().add(shop);
        manager.saveMerchant(merchant);
        promise.resolve();
    }

    @Override
    public void addToShop(Player player, String merchantName, String shopName, ItemStack item, int position, double buyAmount, double sellAmount, Promise promise) {
        final Optional<IMerchant> merchantQuery = manager.getMerchantByName(merchantName);

        if (merchantQuery.isEmpty()) {
            promise.reject("Merchant not found");
            return;
        }

        final GenericMerchant merchant = (GenericMerchant) merchantQuery.get();
        final GenericShop shop = merchant.getShops()
                .stream()
                .filter(s -> ChatColor.stripColor(s.getShopName()).equalsIgnoreCase(shopName))
                .findFirst()
                .orElse(null);

        if (shop == null) {
            promise.reject("Shop not found");
            return;
        }

        final String itemDisplayName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : null;

        if (shop.getItems().stream().anyMatch(i -> i.getPosition() == position)) {
            promise.reject("Inventory position is already in use");
            return;
        }

        final GenericShopItem shopItem = new GenericShopItem(
                UUID.randomUUID(),
                itemDisplayName,
                item.getType(),
                item.getAmount(),
                item.getItemMeta().getEnchants(),
                position,
                buyAmount,
                sellAmount
        );

        shop.getItems().add(shopItem);
        manager.saveMerchant(merchant);
        promise.resolve();
    }
}

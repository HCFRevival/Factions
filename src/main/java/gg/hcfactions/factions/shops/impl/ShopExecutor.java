package gg.hcfactions.factions.shops.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.models.shop.IMerchant;
import gg.hcfactions.factions.models.shop.impl.GenericMerchant;
import gg.hcfactions.factions.models.shop.impl.GenericShop;
import gg.hcfactions.factions.models.shop.impl.GenericShopItem;
import gg.hcfactions.factions.models.shop.impl.MerchantVillager;
import gg.hcfactions.factions.models.shop.impl.events.EventMerchant;
import gg.hcfactions.factions.models.shop.impl.events.EventShop;
import gg.hcfactions.factions.models.shop.impl.events.EventShopItem;
import gg.hcfactions.factions.shops.IShopExecutor;
import gg.hcfactions.factions.shops.ShopManager;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record ShopExecutor(@Getter ShopManager manager) implements IShopExecutor {
    @Override
    public void reloadMerchants() {
        manager.getMerchantVillagers().forEach(villager -> villager.remove(Entity.RemovalReason.DISCARDED));
        manager.getMerchantVillagers().clear();

        manager.loadMerchants();
        manager.spawnMerchants();
    }

    @Override
    public void createMerchant(Player player, String merchantName, boolean isEventMerchant, Promise promise) {
        if (manager.getMerchantByName(ChatColor.stripColor(merchantName)).isPresent()) {
            promise.reject("Merchant name is already in use");
            return;
        }

        if (isEventMerchant) {
            final EventMerchant merchant = new EventMerchant(
                    UUID.randomUUID(),
                    ChatColor.translateAlternateColorCodes('&', merchantName),
                    new PLocatable(player),
                    Lists.newArrayList()
            );

            final MerchantVillager villager = new MerchantVillager(manager.getPlugin(), merchant);
            villager.spawn();

            manager.getMerchantRepository().add(merchant);
            manager.getMerchantVillagers().add(villager);
            manager.saveMerchant(merchant);
            promise.resolve();
            return;
        }

        final GenericMerchant<?> merchant = new GenericMerchant<>(
                UUID.randomUUID(),
                ChatColor.translateAlternateColorCodes('&', merchantName),
                new PLocatable(player),
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

        final GenericMerchant<?> merchant = (GenericMerchant<?>) merchantQuery.get();
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
    public void openMerchant(Player player, GenericMerchant<?> merchant) {
        final MerchantMenu menu = new MerchantMenu(manager.getPlugin(), player, merchant);
        menu.open();
    }

    @Override
    public void openShop(Player player, GenericMerchant<?> merchant, GenericShop<?> shop) {
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

        final IMerchant merchant = merchantQuery.get();

        if (merchant.getShops().stream().anyMatch(s -> ChatColor.stripColor(s.getShopName()).equalsIgnoreCase(ChatColor.stripColor(shopName)))) {
            promise.reject("Shop name is already in use");
            return;
        }

        if (merchant.getShops().stream().anyMatch(s -> s.getPosition() == position)) {
            promise.reject("Inventory position is already in use");
            return;
        }

        if (merchant instanceof final EventMerchant eventMerchant) {
            final EventShop shop = new EventShop(
                    UUID.randomUUID(),
                    ChatColor.translateAlternateColorCodes('&', shopName),
                    item.getType(),
                    position,
                    Lists.newArrayList()
            );

            eventMerchant.getShops().add(shop);
            manager.saveMerchant(eventMerchant);
        } else {
            final GenericMerchant<GenericShop> genericMerchant = (GenericMerchant<GenericShop>) merchant;
            final GenericShop<?> shop = new GenericShop<>(
                    UUID.randomUUID(),
                    ChatColor.translateAlternateColorCodes('&', shopName),
                    item.getType(),
                    position,
                    Lists.newArrayList()
            );

            genericMerchant.getShops().add(shop);
            manager.saveMerchant(genericMerchant);
        }

        promise.resolve();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeFromMerchant(Player player, String merchantName, String shopName, Promise promise) {
        final Optional<IMerchant> merchantQuery = manager.getMerchantByName(merchantName);

        if (merchantQuery.isEmpty()) {
            promise.reject("Merchant not found");
            return;
        }

        final IMerchant merchantResult = merchantQuery.get();
        final GenericMerchant<?> merchant = (GenericMerchant<?>) merchantResult;
        final GenericShop<GenericShopItem> shop = (GenericShop<GenericShopItem>) merchant.getShops()
                .stream()
                .filter(s -> ChatColor.stripColor(s.getShopName()).equalsIgnoreCase(shopName) || ChatColor.stripColor(s.getShopName()).startsWith(shopName))
                .findFirst()
                .orElse(null);

        if (shop == null) {
            promise.reject("Shop not found");
            return;
        }

        merchant.getShops().remove(shop);
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

        final IMerchant merchantResult = merchantQuery.get();
        if (merchantResult instanceof EventMerchant) {
            promise.reject("Can not add normal items to an Event Merchant");
            return;
        }

        final GenericMerchant<?> merchant = (GenericMerchant<?>) merchantResult;
        final GenericShop<GenericShopItem> shop = (GenericShop<GenericShopItem>) merchant.getShops()
                .stream()
                .filter(s -> ChatColor.stripColor(s.getShopName()).equalsIgnoreCase(shopName) || ChatColor.stripColor(s.getShopName()).startsWith(shopName))
                .filter(ms -> (!(ms instanceof EventShop)))
                .findFirst()
                .orElse(null);

        if (shop == null) {
            promise.reject("Shop not found");
            return;
        }

        if (shop.getItems().stream().anyMatch(i -> i.getPosition() == position)) {
            promise.reject("Inventory position is already in use");
            return;
        }

        final String itemDisplayName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : null;

        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.putAll(item.getItemMeta().getEnchants());
        if (item.getType().equals(Material.ENCHANTED_BOOK)) {
            final EnchantmentStorageMeta encMeta = (EnchantmentStorageMeta) item.getItemMeta();
            enchantments.putAll(encMeta.getStoredEnchants());
        }

        final GenericShopItem shopItem = new GenericShopItem(
                UUID.randomUUID(),
                itemDisplayName,
                item.getType(),
                item.getAmount(),
                item.getItemMeta().getLore(),
                enchantments,
                position,
                false,
                buyAmount,
                sellAmount
        );

        shop.getItems().add(shopItem);
        manager.saveMerchant(merchant);
        promise.resolve();
    }

    @Override
    public void addToEventShop(Player player, String merchantName, String shopName, ItemStack item, int position, int tokenAmount, Promise promise) {
        final Optional<IMerchant> merchantQuery = manager.getMerchantByName(merchantName);

        if (merchantQuery.isEmpty()) {
            promise.reject("Merchant not found");
            return;
        }

        final IMerchant merchantResult = merchantQuery.get();

        if (!(merchantResult instanceof final EventMerchant merchant)) {
            promise.reject("Merchant is not an event merchant");
            return;
        }

        final EventShop shop = merchant.getShops()
                .stream()
                .filter(s -> ChatColor.stripColor(s.getShopName()).equalsIgnoreCase(shopName))
                .findFirst()
                .orElse(null);

        if (shop == null) {
            promise.reject("Shop not found");
            return;
        }

        if (shop.getItems().stream().anyMatch(i -> i.getPosition() == position)) {
            promise.reject("Inventory position is already in use");
            return;
        }

        final String itemDisplayName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : null;

        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.putAll(item.getItemMeta().getEnchants());
        if (item.getType().equals(Material.ENCHANTED_BOOK)) {
            final EnchantmentStorageMeta encMeta = (EnchantmentStorageMeta) item.getItemMeta();
            enchantments.putAll(encMeta.getStoredEnchants());
        }

        final EventShopItem shopItem = new EventShopItem(
                UUID.randomUUID(),
                itemDisplayName,
                item.getType(),
                item.getAmount(),
                item.getItemMeta().getLore(),
                enchantments,
                false,
                position,
                tokenAmount
        );

        shop.getItems().add(shopItem);
        manager.saveMerchant(merchant);
        promise.resolve();
    }

    @Override
    public void removeFromShop(Player player, String merchantName, String shopName, int index, Promise promise) {
        final Optional<IMerchant> merchantQuery = manager.getMerchantByName(merchantName);

        if (merchantQuery.isEmpty()) {
            promise.reject("Merchant not found");
            return;
        }

        final IMerchant merchantResult = merchantQuery.get();
        final GenericMerchant<?> merchant = (GenericMerchant<?>) merchantResult;
        final GenericShop<GenericShopItem> shop = (GenericShop<GenericShopItem>) merchant.getShops()
                .stream()
                .filter(s -> ChatColor.stripColor(s.getShopName()).equalsIgnoreCase(shopName) || ChatColor.stripColor(s.getShopName()).startsWith(shopName))
                .findFirst()
                .orElse(null);

        if (shop == null) {
            promise.reject("Shop not found");
            return;
        }

        final GenericShopItem item = shop.getItems().stream().filter(i -> i.getPosition() == index).findFirst().orElse(null);

        if (item == null) {
            promise.reject("Item not at position: " + index);
            return;
        }

        shop.getItems().remove(item);
        manager.deleteShopItem(merchant, shop, item);
        promise.resolve();
    }
}

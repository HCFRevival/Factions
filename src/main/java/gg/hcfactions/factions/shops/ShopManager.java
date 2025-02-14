package gg.hcfactions.factions.shops;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.shop.IMerchant;
import gg.hcfactions.factions.models.shop.impl.GenericMerchant;
import gg.hcfactions.factions.models.shop.impl.GenericShop;
import gg.hcfactions.factions.models.shop.impl.GenericShopItem;
import gg.hcfactions.factions.models.shop.impl.MerchantVillager;
import gg.hcfactions.factions.models.shop.impl.events.EventMerchant;
import gg.hcfactions.factions.models.shop.impl.events.EventShop;
import gg.hcfactions.factions.models.shop.impl.events.EventShopItem;
import gg.hcfactions.factions.shops.impl.ShopExecutor;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import gg.hcfactions.libs.bukkit.utils.Enchants;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.world.entity.Entity;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;

import java.util.*;

@Getter
public final class ShopManager implements IManager {
    public final Factions plugin;
    public final ShopExecutor executor;
    public final List<IMerchant> merchantRepository;
    public final List<MerchantVillager> merchantVillagers;

    public ShopManager(Factions plugin) {
        this.plugin = plugin;
        this.executor = new ShopExecutor(this);
        this.merchantRepository = Lists.newArrayList();
        this.merchantVillagers = Lists.newArrayList();
    }

    @Override
    public void onEnable() {
        loadMerchants();
        spawnMerchants();

        plugin.getCommandManager().getCommandCompletions().registerAsyncCompletion("merchants", ctx -> {
            final List<String> merchantNames = Lists.newArrayList();

            merchantRepository.forEach(m -> {
                String plaintext = PlainTextComponentSerializer.plainText().serialize(m.getMerchantName());
                merchantNames.add(plaintext);
            });

            return merchantNames;
        });
    }

    @Override
    public void onDisable() {
        merchantVillagers.forEach(villager -> villager.remove(Entity.RemovalReason.DISCARDED));
    }

    public void loadMerchants() {
        final YamlConfiguration conf = plugin.loadConfiguration("shops");

        merchantRepository.clear();

        if (conf.getConfigurationSection("shops") == null) {
            plugin.getAresLogger().warn("no shops found in shops.yml. skipping...");
            return;
        }

        for (String merchantId : Objects.requireNonNull(conf.getConfigurationSection("shops")).getKeys(false)) {
            final String merchantPath = "shops." + merchantId + ".";
            final Component merchantDisplayName = plugin.getMiniMessage().deserialize(Objects.requireNonNull(conf.getString(merchantPath + "display_name")));
            final double merchantX = conf.getDouble(merchantPath + "location.x");
            final double merchantY = conf.getDouble(merchantPath + "location.y");
            final double merchantZ = conf.getDouble(merchantPath + "location.z");
            final String merchantWorld = conf.getString(merchantPath + "location.world");
            final boolean isEventShop = conf.get(merchantPath + "variant") != null && Objects.requireNonNull(conf.getString(merchantPath + "variant")).equalsIgnoreCase("event");
            final PLocatable merchantPosition = new PLocatable(merchantWorld, merchantX, merchantY, merchantZ, 0.0f, 0.0f);
            final List<GenericShop<?>> shops = Lists.newArrayList();
            final List<EventShop> eventShops = Lists.newArrayList();

            if (conf.getConfigurationSection(merchantPath + "shops") != null) {
                for (String shopId : Objects.requireNonNull(conf.getConfigurationSection(merchantPath + "shops")).getKeys(false)) {
                    final String shopPath = merchantPath + "shops." + shopId + ".";
                    final Component shopName = plugin.getMiniMessage().deserialize(Objects.requireNonNull(conf.getString(shopPath + "display_name")));
                    final String shopIconName = conf.getString(shopPath + "icon");
                    final int shopIconPosition = conf.getInt(shopPath + "position");
                    final List<GenericShopItem> items = Lists.newArrayList();
                    final List<EventShopItem> eventShopItems = Lists.newArrayList();
                    
                    Material shopIcon = null;
                    if (shopIconName == null) {
                        plugin.getAresLogger().error("Shop icon null: {}", shopId);
                        continue;
                    }

                    try {
                        shopIcon = Material.getMaterial(shopIconName);
                    } catch (IllegalArgumentException e) {
                        plugin.getAresLogger().error("Bad shop icon material: {}", shopIconName);
                        continue;
                    }

                    if (conf.getConfigurationSection(shopPath + "items") != null) {
                        for (String shopItemId : Objects.requireNonNull(conf.getConfigurationSection(shopPath + "items")).getKeys(false)) {
                            final String itemPath = shopPath + "items." + shopItemId + ".";
                            final Component itemDisplayName = (conf.get(itemPath + "display_name") != null) ? plugin.getMiniMessage().deserialize(Objects.requireNonNull(conf.getString(itemPath + "display_name"))) : null;
                            final String itemMaterialName = conf.getString(itemPath + "material");
                            final int itemAmount = conf.getInt(itemPath + "amount");
                            final int itemPosition = conf.getInt(itemPath + "position");
                            final double itemBuyPrice = conf.getDouble(itemPath + "buy_price");
                            final double itemSellPrice = conf.getDouble(itemPath + "sell_price");
                            final boolean itemDisabled = conf.get(itemPath + "disabled") != null && conf.getBoolean(itemPath + "disabled");
                            final int itemTokenPrice = conf.get(itemPath + "token_price") != null ? conf.getInt(itemPath + "token_price") : 0;
                            final Map<Enchantment, Integer> itemEnchantments = Maps.newHashMap();
                            final List<Component> itemLore = Lists.newArrayList();
                            Material itemMaterial = null;
                            ICustomItem customItem = null;

                            if (itemMaterialName == null) {
                                plugin.getAresLogger().error("No item material name: {}", shopItemId);
                                continue;
                            }

                            try {
                                itemMaterial = Material.getMaterial(itemMaterialName);
                            } catch (IllegalArgumentException e) {
                                plugin.getAresLogger().error("Bad material: {}", itemMaterialName);
                                continue;
                            }

                            if (conf.get(itemPath + "enchantments") != null) {
                                for (String enchantmentName : Objects.requireNonNull(conf.getConfigurationSection(itemPath + "enchantments")).getKeys(false)) {
                                    final Enchantment enchantment = Enchants.getEnchantment(enchantmentName);
                                    final int level = conf.getInt(itemPath + "enchantments." + enchantmentName);

                                    if (enchantment == null) {
                                        plugin.getAresLogger().error("Bad enchantment: {}", enchantmentName);
                                        continue;
                                    }

                                    itemEnchantments.put(enchantment, level);
                                }
                            }

                            if (conf.get(itemPath + "lore") != null) {
                                for (String loreLine : conf.getStringList(itemPath + "lore")) {
                                    itemLore.add(plugin.getMiniMessage().deserialize(loreLine));
                                }
                            }

                            if (conf.get(itemPath + "custom_item_class") != null) {
                                CustomItemService cis = (CustomItemService) plugin.getService(CustomItemService.class);

                                if (cis != null) {
                                    Optional<ICustomItem> customItemQuery = cis.getItemByClassName(conf.getString(itemPath + "custom_item_class"));

                                    if (customItemQuery.isEmpty()) {
                                        plugin.getAresLogger().error("Invalid custom item class: {}", conf.get(itemPath + "custom_item_class"));
                                        plugin.getAresLogger().error("Here are the custom items I see:");
                                        cis.getItemRegistry().keySet().forEach(validCustomItem -> plugin.getAresLogger().error(validCustomItem.getSimpleName()));
                                        continue;
                                    }

                                    customItem = customItemQuery.get();
                                }
                            }

                            if (itemTokenPrice > 0) {
                                final EventShopItem item = new EventShopItem(
                                        UUID.fromString(shopItemId),
                                        itemDisplayName,
                                        itemMaterial,
                                        itemAmount,
                                        itemLore,
                                        itemEnchantments,
                                        itemDisabled,
                                        itemPosition,
                                        itemTokenPrice,
                                        customItem
                                );

                                eventShopItems.add(item);
                                continue;
                            }
                            
                            final GenericShopItem item = new GenericShopItem(
                                    UUID.fromString(shopItemId),
                                    itemDisplayName,
                                    itemMaterial,
                                    itemAmount,
                                    itemLore,
                                    itemEnchantments,
                                    itemPosition,
                                    itemDisabled,
                                    itemBuyPrice,
                                    itemSellPrice,
                                    customItem
                            );
                            
                            items.add(item);
                        }
                    }

                    if (isEventShop) {
                        eventShops.add(new EventShop(UUID.fromString(shopId), shopName, shopIcon, shopIconPosition, eventShopItems));
                        continue;
                    }
                    
                    final GenericShop<?> shop = new GenericShop<>(UUID.fromString(shopId), shopName, shopIcon, shopIconPosition, items);
                    shops.add(shop);
                }
            }

            if (isEventShop) {
                merchantRepository.add(new EventMerchant(UUID.fromString(merchantId), merchantDisplayName, merchantPosition, eventShops));
                plugin.getAresLogger().info("Loaded Merchant: " + merchantDisplayName + " as an event merchant with " + eventShops.size() + " shops");
                continue;
            }

            final GenericMerchant<?> merchant = new GenericMerchant<>(UUID.fromString(merchantId), merchantDisplayName, merchantPosition, shops);
            plugin.getAresLogger().info("Loaded Merchant: {} as a generic merchant with {} shops", merchantDisplayName, shops.size());
            merchantRepository.add(merchant);
        }
    }

    public void saveMerchant(GenericMerchant<?> merchant) {
        final YamlConfiguration conf = plugin.loadConfiguration("shops");
        final String merchantPath = "shops." + merchant.getId().toString() + ".";

        conf.set(merchantPath + "display_name", plugin.getMiniMessage().serialize(merchant.getMerchantName()));
        conf.set(merchantPath + "location.x", merchant.getMerchantLocation().getX());
        conf.set(merchantPath + "location.y", merchant.getMerchantLocation().getY());
        conf.set(merchantPath + "location.z", merchant.getMerchantLocation().getZ());
        conf.set(merchantPath + "location.world", merchant.getMerchantLocation().getWorldName());

        if (merchant instanceof EventMerchant) {
            conf.set(merchantPath + "variant", "event");
        }
        
        for (GenericShop<?> shop : merchant.getShops()) {
            final String shopPath = merchantPath + "shops." + shop.getId().toString() + ".";
            conf.set(shopPath + "display_name", plugin.getMiniMessage().serialize(shop.getShopName()));
            conf.set(shopPath + "icon", shop.getIconMaterial().name());
            conf.set(shopPath + "position", shop.getPosition());

            for (GenericShopItem item : shop.getItems()) {
                final String itemPath = shopPath + "items." + item.getId().toString() + ".";

                if (item.displayName != null) {
                    conf.set(itemPath + "display_name", plugin.getMiniMessage().serialize(item.displayName));
                }

                conf.set(itemPath + "material", item.getMaterial().name());
                conf.set(itemPath + "amount", item.getAmount());
                conf.set(itemPath + "position", item.getPosition());
                conf.set(itemPath + "buy_price", item.getBuyPrice());
                conf.set(itemPath + "sell_price", item.getSellPrice());
                
                if (item instanceof EventShopItem) {
                    conf.set(itemPath + "token_price", ((EventShopItem)item).getTokenPrice());
                }

                if (item.getLore() != null && !item.getLore().isEmpty()) {
                    List<String> serialized = Lists.newArrayList();
                    item.getLore().forEach(line -> serialized.add(plugin.getMiniMessage().serialize(line)));
                    conf.set(itemPath + "lore", serialized);
                }

                if (item.getEnchantments() != null && !item.getEnchantments().isEmpty()) {
                    item.getEnchantments().forEach((enchantment, level) -> conf.set(itemPath + "enchantments." + enchantment.getKey().getKey(), level));
                }

                if (item.getCustomItemClass() != null) {
                    conf.set(itemPath + "custom_item_class", item.getCustomItemClass().getClass().getSimpleName());
                }
            }
        }

        plugin.saveConfiguration("shops", conf);
    }

    public void deleteMerchant(GenericMerchant<?> merchant) {
        final YamlConfiguration conf = plugin.loadConfiguration("shops");
        conf.set("shops." + merchant.getId().toString(), null);
        plugin.saveConfiguration("shops", conf);
    }

    public void deleteShopItem(GenericMerchant<?> merchant, GenericShop<?> shop, GenericShopItem item) {
        final YamlConfiguration conf = plugin.loadConfiguration("shops");
        conf.set("shops." + merchant.getId().toString() + ".shops." + shop.getId().toString() + ".items." + item.getId().toString(), null);
        plugin.saveConfiguration("shops", conf);
    }

    public void spawnMerchants() {
        if (merchantRepository.isEmpty()) {
            plugin.getAresLogger().warn("no merchants found in the repository, skipping spawn merchants...");
            return;
        }

        merchantRepository.forEach(merchant -> {
            final PLocatable loc = merchant.getMerchantLocation();
            final World world = loc.getBukkitLocation().getWorld();

            if (world != null) {
                Chunk chunk = loc.getBukkitLocation().getChunk();
                if (!chunk.isLoaded()) {
                    chunk.load();
                }

                world.getNearbyEntities(loc.getBukkitLocation(), 1.5, 1.5, 1.5).forEach(entity -> {
                    if (entity instanceof final Villager villager) {
                        villager.remove();
                    }
                });
            }

            final MerchantVillager villager = new MerchantVillager(plugin, (GenericMerchant<?>) merchant);
            merchantVillagers.add(villager);
            villager.spawn();
            villager.setNoAi(true);
        });
    }

    public Optional<IMerchant> getMerchantById(UUID uniqueId) {
        return merchantRepository.stream().filter(m -> m.getId().equals(uniqueId)).findFirst();
    }

    public Optional<IMerchant> getMerchantByLocation(BLocatable location) {
        return merchantRepository.stream().filter(m ->
                m.getMerchantLocation().getWorldName().equals(location.getWorldName()) && m.getMerchantLocation().getDistance(location) < 2.0).findFirst();
    }

    public Optional<IMerchant> getMerchantByName(String name) {
        for (IMerchant merchant : merchantRepository) {
            String displayNamePlaintext = PlainTextComponentSerializer.plainText().serialize(merchant.getMerchantName());

            if (displayNamePlaintext.equalsIgnoreCase(name) || displayNamePlaintext.replaceAll(" ", "").equalsIgnoreCase(name)) {
                return Optional.of(merchant);
            }
        }

        return Optional.empty();
    }
}

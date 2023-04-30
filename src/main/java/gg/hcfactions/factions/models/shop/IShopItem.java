package gg.hcfactions.factions.models.shop;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IShopItem {
    /**
     * @return Unique Identifier
     */
    UUID getId();

    /**
     * @return Bukkit display name
     */
    String getDisplayName();

    /**
     * @return Bukkit material
     */
    Material getMaterial();

    /**
     * @return Amount for transaction
     */
    int getAmount();

    /**
     * @return Enchantments to apply
     */
    Map<Enchantment, Integer> getEnchantments();

    /**
     * @return Shop slot position
     */
    int getPosition();

    /**
     * @return Cost to buy
     */
    double getBuyPrice();

    /**
     * @return Amount to sell for
     */
    double getSellPrice();

    /**
     * @return If true this item can be purchased
     */
    default boolean isBuyable() {
        return getBuyPrice() > 0.0;
    }

    /**
     * @return If true this item can be solc
     */
    default boolean isSellable() {
        return getSellPrice() > 0.0;
    }

    /**
     * @param asDisplay If true buy/sell info will be added to lore
     * @return ItemStack
     */
    default ItemStack getItem(boolean asDisplay) {
        final ItemBuilder builder = new ItemBuilder();

        builder.setMaterial(getMaterial());
        builder.setAmount(getAmount());
        builder.addFlag(ItemFlag.HIDE_ATTRIBUTES);

        if (getDisplayName() != null) {
            builder.setName(getDisplayName());
        }

        if (getEnchantments() != null && !getEnchantments().isEmpty()) {
            builder.addEnchant(getEnchantments());
        }

        if (asDisplay) {
            final List<String> lore = Lists.newArrayList();

            if (isBuyable()) {
                lore.add(ChatColor.GREEN + "Buy" + ChatColor.WHITE + ": $" + String.format("%.2f", getBuyPrice()) + ChatColor.YELLOW + " (Left-click)");
            }

            if (isSellable()) {
                lore.add(ChatColor.RED + "Sell" + ChatColor.WHITE + ": $" + String.format("%.2f", getSellPrice()) + ChatColor.YELLOW + " (Right-click)");
            }

            builder.addLore(lore);
        }

        return builder.build();
    }
}

package gg.hcfactions.factions.models.shop;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IShopItem {
    /**
     * @return Unique Identifier
     */
    UUID getId();

    /**
     * @return Display name component
     */
    Component getDisplayName();

    /**
     * @return Bukkit material
     */
    Material getMaterial();

    /**
     * @return Amount for transaction
     */
    int getAmount();

    /**
     * @deprecated Use getLoreComponents()
     * @return Item lore to apply
     */
    List<Component> getLore();

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
     * @return If true the item can not be purchased/sold
     */
    boolean isDisabled();

    /**
     * @return Custom Item associated with this item
     */
    ICustomItem getCustomItemClass();

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
     * Sets the disabled state for this shop item
     * @param b If true the item can not be purchased/sold
     */
    void setDisabled(boolean b);

    /**
     * @param asDisplay If true buy/sell info will be added to lore
     * @return ItemStack
     */
    default ItemStack getItem(boolean asDisplay) {
        final ItemBuilder builder = new ItemBuilder();
        final List<Component> lore = Lists.newArrayList();

        if (getCustomItemClass() != null && !asDisplay) {
            return getCustomItemClass().getItem();
        }

        builder.setMaterial(getMaterial());
        builder.setAmount(getAmount());
        builder.addFlag(ItemFlag.HIDE_ARMOR_TRIM);
        builder.addFlag(ItemFlag.HIDE_ATTRIBUTES);
        builder.addFlag(ItemFlag.HIDE_PLACED_ON);

        if (getDisplayName() != null) {
            builder.setName(getDisplayName());
        }

        if (!getMaterial().equals(Material.ENCHANTED_BOOK) && getEnchantments() != null && !getEnchantments().isEmpty()) {
            builder.addEnchant(getEnchantments());
        }

        if (getLore() != null && !getLore().isEmpty()) {
            lore.addAll(getLore());
        }

        if (asDisplay) {
            if (isDisabled()) {
                builder.setName(Component.text("NOT FOR SALE", NamedTextColor.DARK_RED));
            }

            lore.add(Component.text(" "));

            if (isBuyable()) {
                lore.add(Component.text("Buy", NamedTextColor.GREEN)
                        .append(Component.text(": $" + String.format("%.2f", getBuyPrice()), NamedTextColor.WHITE))
                        .appendSpace().append(Component.text("(", NamedTextColor.GRAY))
                        .append(Component.keybind("key.attack", NamedTextColor.GRAY))
                        .append(Component.text(")", NamedTextColor.GRAY)));
            }

            if (isSellable()) {
                lore.add(Component.text("Sell", NamedTextColor.RED)
                        .append(Component.text(": $" + String.format("%.2f", getSellPrice()), NamedTextColor.WHITE))
                        .appendSpace().append(Component.text("(", NamedTextColor.GRAY))
                        .append(Component.keybind("key.use", NamedTextColor.GRAY))
                        .append(Component.text(")", NamedTextColor.GRAY)));
            }
        }

        builder.addLoreComponents(lore);

        final ItemStack item = builder.build();

        if (getMaterial().equals(Material.ENCHANTED_BOOK) && !getEnchantments().isEmpty()) {
            final EnchantmentStorageMeta encMeta = (EnchantmentStorageMeta) item.getItemMeta();

            if (encMeta != null) {
                getEnchantments().forEach((enchantment, level) -> encMeta.addStoredEnchant(enchantment, level, true));
            }

            item.setItemMeta(encMeta);
        }

        return item;
    }
}

package gg.hcfactions.factions.models.shop;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface IShop {
    /**
     * @return Unique Identifier
     */
    UUID getId();

    /**
     * @return Shop display name component
     */
    Component getShopName();

    /**
     * @return Bukkit material
     */
    Material getIconMaterial();

    /**
     * @return Inventory slot position
     */
    int getPosition();

    /**
     * @return Shop items for sale
     */
    List<? extends IShopItem> getItems();

    /**
     * @return Icon used for display in merchant inventories
     */
    default ItemStack getIcon() {
        final ItemBuilder builder = new ItemBuilder();
        builder.setMaterial(getIconMaterial());
        builder.setName(getShopName());
        builder.addFlag(Lists.newArrayList(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON));
        return builder.build();
    }
}

package gg.hcfactions.factions.models.shop;

import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface IShop {
    /**
     * @return Unique Identifier
     */
    UUID getId();

    /**
     * @return Shop display name
     */
    String getShopName();

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
        return builder.build();
    }
}

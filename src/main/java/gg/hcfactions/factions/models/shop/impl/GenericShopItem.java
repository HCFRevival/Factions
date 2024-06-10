package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.models.shop.IShopItem;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class GenericShopItem implements IShopItem {
    public final UUID id;
    public final Component displayName;
    public final Material material;
    public final int amount;
    public final List<Component> lore;
    public final Map<Enchantment, Integer> enchantments;
    public final int position;
    @Setter public boolean disabled;
    public final double buyPrice;
    public final double sellPrice;
    @Setter public ICustomItem customItemClass;

    public GenericShopItem(
            UUID id,
            Component displayName,
            Material material,
            int amount,
            List<Component> lore,
            Map<Enchantment, Integer> enchantments,
            int position,
            boolean disabled,
            double buyPrice,
            double sellPrice
    ) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.lore = lore;
        this.enchantments = enchantments;
        this.amount = amount;
        this.position = position;
        this.disabled = disabled;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.customItemClass = null;
    }

    public GenericShopItem(
            UUID id,
            Component displayName,
            Material material,
            int amount,
            List<Component> lore,
            Map<Enchantment, Integer> enchantments,
            int position,
            boolean disabled,
            double buyPrice,
            double sellPrice,
            ICustomItem customItemClass
    ) {
        this(
                id,
                displayName,
                material,
                amount,
                lore,
                enchantments,
                position,
                disabled,
                buyPrice,
                sellPrice
        );

        this.customItemClass = customItemClass;
    }
}

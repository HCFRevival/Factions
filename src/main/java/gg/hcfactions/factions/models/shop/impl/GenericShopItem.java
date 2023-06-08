package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.models.shop.IShopItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class GenericShopItem implements IShopItem {
    @Getter public final UUID id;
    @Getter public final String displayName;
    @Getter public final Material material;
    @Getter public final int amount;
    @Getter public final List<String> lore;
    @Getter public final Map<Enchantment, Integer> enchantments;
    @Getter public final int position;
    @Getter @Setter public boolean disabled;
    @Getter public final double buyPrice;
    @Getter public final double sellPrice;
}

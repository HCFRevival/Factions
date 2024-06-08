package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.models.shop.IShopItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
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
}

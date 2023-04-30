package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.models.shop.IShopItem;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;
import java.util.UUID;

public record GenericShopItem(@Getter UUID id,
                              @Getter String displayName,
                              @Getter Material material,
                              @Getter int amount,
                              @Getter Map<Enchantment, Integer> enchantments,
                              @Getter int position,
                              @Getter double buyPrice,
                              @Getter double sellPrice) implements IShopItem {}

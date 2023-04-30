package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.models.shop.IShop;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

public record GenericShop(@Getter UUID id,
                          @Getter String shopName,
                          @Getter Material iconMaterial,
                          @Getter int position,
                          @Getter List<GenericShopItem> items
) implements IShop {}

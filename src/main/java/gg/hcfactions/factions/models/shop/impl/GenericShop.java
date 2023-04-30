package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.models.shop.IShop;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class GenericShop<T extends GenericShopItem> implements IShop {
    @Getter public final UUID id;
    @Getter public final String shopName;
    @Getter public final Material iconMaterial;
    @Getter public final int position;
    @Getter public final List<T> items;
}

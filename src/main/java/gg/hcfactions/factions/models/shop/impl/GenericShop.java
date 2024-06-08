package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.models.shop.IShop;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GenericShop<T extends GenericShopItem> implements IShop {
    public final UUID id;
    public final Component shopName;
    public final Material iconMaterial;
    public final int position;
    public final List<T> items;
}

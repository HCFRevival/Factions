package gg.hcfactions.factions.models.shop.impl.events;

import gg.hcfactions.factions.models.shop.impl.GenericShop;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

public final class EventShop extends GenericShop<EventShopItem> {
    public EventShop(UUID id, Component shopName, Material iconMaterial, int position, List<EventShopItem> items) {
        super(id, shopName, iconMaterial, position, items);
    }
}

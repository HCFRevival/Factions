package gg.hcfactions.factions.models.shop.impl.events;

import gg.hcfactions.factions.models.shop.impl.GenericMerchant;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;

import java.util.List;
import java.util.UUID;

public final class EventMerchant extends GenericMerchant<EventShop> {
    public EventMerchant(UUID id, String merchantName, BLocatable merchantLocation, List<EventShop> shops) {
        super(id, merchantName, merchantLocation, shops);
    }
}
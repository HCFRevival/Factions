package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.models.shop.IMerchant;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class GenericMerchant<T extends GenericShop> implements IMerchant {
    @Getter public final UUID id;
    @Getter public final Component merchantName;
    @Getter public final PLocatable merchantLocation;
    @Getter public final List<T> shops;
}

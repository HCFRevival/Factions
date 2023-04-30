package gg.hcfactions.factions.models.shop.impl;

import gg.hcfactions.factions.models.shop.IMerchant;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public record GenericMerchant(@Getter UUID id, @Getter String merchantName, @Getter BLocatable merchantLocation,
                              @Getter List<GenericShop> shops) implements IMerchant {}

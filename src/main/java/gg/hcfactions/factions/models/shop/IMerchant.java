package gg.hcfactions.factions.models.shop;

import gg.hcfactions.libs.bukkit.location.impl.BLocatable;

import java.util.List;
import java.util.UUID;

public interface IMerchant {
    /**
     * @return Unique identifier
     */
    UUID getId();

    /**
     * @return Display name
     */
    String getMerchantName();

    /**
     * @return Location to spawn the merchant at
     */
    BLocatable getMerchantLocation();

    /**
     * @return Shops available in this merchant
     */
    List<? extends IShop> getShops();
}
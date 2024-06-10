package gg.hcfactions.factions.models.shop;

import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.UUID;

public interface IMerchant {
    /**
     * @return Unique identifier
     */
    UUID getId();

    /**
     * @return Display Name Component
     */
    Component getMerchantName();

    /**
     * @return Location to spawn the merchant at
     */
    PLocatable getMerchantLocation();

    /**
     * @return Shops available in this merchant
     */
    List<? extends IShop> getShops();
}

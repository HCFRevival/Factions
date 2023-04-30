package gg.hcfactions.factions.models.shop;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;

import java.util.UUID;

public interface IMerchantVillager {
    /**
     * @return Owner Plugin
     */
    Factions getPlugin();

    /**
     * @return Merchant ID used for queries
     */
    UUID getMerchantId();

    /**
     * @return Location to spawn merchant
     */
    BLocatable getPosition();

    /**
     * Spawn the merchant in the world
     */
    void spawn();
}

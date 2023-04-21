package gg.hcfactions.factions.models.claim;

import gg.hcfactions.factions.claims.ClaimManager;
import gg.hcfactions.libs.bukkit.location.IRegion;

import java.util.UUID;

public interface IClaim extends IRegion {
    ClaimManager getManager();
    UUID getUniqueId();
    UUID getOwner();
    double getCost();

    void setCost(double d);
}

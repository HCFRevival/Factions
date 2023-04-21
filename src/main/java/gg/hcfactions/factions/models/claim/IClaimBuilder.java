package gg.hcfactions.factions.models.claim;

import gg.hcfactions.factions.claims.ClaimManager;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import org.bukkit.entity.Player;

public interface IClaimBuilder {
    ClaimManager getManager();

    /**
     * @return Claim builder owning faction
     */
    IFaction getFaction();

    /**
     * @return Bukkit Player
     */
    Player getPlayer();

    /**
     * @return Claim corner pos A
     */
    BLocatable getCornerA();

    /**
     * @return Claim corner pos B
     */
    BLocatable getCornerB();

    /**
     * Update a claim corner in the building process
     * @param location Corner pos
     * @param pillarType Pillar A or B
     */
    void setCorner(BLocatable location, EClaimPillarType pillarType);

    /**
     * Reset both claim corners
     */
    void reset();

    /**
     * @return Calculated claim value
     */
    double calculateCost();

    /**
     * Attempts to build the claim
     * @param promise Failable Promise containing a Claim
     */
    void build(FailablePromise<Claim> promise);
}

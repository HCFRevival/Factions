package gg.hcfactions.factions.models.subclaim;

import gg.hcfactions.factions.claims.subclaims.SubclaimManager;
import gg.hcfactions.factions.models.claim.EClaimPillarType;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.claim.impl.ClaimPillar;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class SubclaimBuilder {
    @Getter public final SubclaimManager subclaimManager;
    @Getter public final PlayerFaction owner;
    @Getter public final Player player;
    @Getter public final String name;
    @Getter public BLocatable cornerA;
    @Getter public BLocatable cornerB;

    public SubclaimBuilder(SubclaimManager subclaimManager, PlayerFaction owner, Player player, String name) {
        this.subclaimManager = subclaimManager;
        this.owner = owner;
        this.player = player;
        this.name = name;
        this.cornerA = null;
        this.cornerB = null;
    }

    /**
     * Handles setting the corner of the subclaim builder
     * @param location Subclaim corner location
     * @param type Subclaim corner type
     */
    public void setCorner(BLocatable location, EClaimPillarType type) {
        final FactionPlayer factionPlayer = (FactionPlayer) subclaimManager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (type.equals(EClaimPillarType.A)) {
            this.cornerA = location;
        } else if (type.equals(EClaimPillarType.B)) {
            this.cornerB = location;
        }

        if (factionPlayer != null) {
            final ClaimPillar pillar = new ClaimPillar(player, location, type);
            final ClaimPillar previous = factionPlayer.getExistingClaimPillar(type);

            if (previous != null) {
                previous.hide();
                factionPlayer.getPillars().remove(previous);
            }

            factionPlayer.getPillars().add(pillar);

            new Scheduler(subclaimManager.getPlugin()).sync(pillar::draw).delay(5L).run();
        }

        player.sendMessage(FMessage.LAYER_2 + "Subclaim point " + FMessage.LAYER_1 + type.name() + FMessage.LAYER_2 + " set at " + FMessage.LAYER_1 + location.toString());
    }

    /**
     * Reset the subclaim builder
     */
    public void reset() {
        final FactionPlayer factionPlayer = (FactionPlayer) subclaimManager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        this.cornerA = null;
        this.cornerB = null;

        if (factionPlayer != null) {
            factionPlayer.hideClaimPillars();
        }

        player.sendMessage(FMessage.LAYER_1 + "Subclaim reset");
    }

    /**
     * Handles building the subclaim and returns it in a consumer
     * @param promise Promise
     */
    public void build(FailablePromise<Subclaim> promise) {
        if (cornerA == null) {
            promise.reject("Corner A has not been set (Left-click)");
            return;
        }

        if (cornerB == null) {
            promise.reject("Corner B has not been set (Right-click)");
            return;
        }

        if (!cornerA.getWorldName().equalsIgnoreCase(cornerB.getWorldName())) {
            promise.reject("Claim corner locations are not in the same world");
            return;
        }

        final Subclaim subclaim = new Subclaim(subclaimManager, owner.getUniqueId(), name, cornerA, cornerB);

        new Scheduler(subclaimManager.getPlugin()).async(() -> {
            // Checking to see if any of the perimeter blocks leaves the claim
            for (BLocatable perimeter : subclaim.getPerimeter(64)) {
                final Claim inside = subclaimManager.getPlugin().getClaimManager().getClaimAt(perimeter);

                if (inside == null || !inside.getOwner().equals(owner.getUniqueId())) {
                    if (inside == null) {
                        subclaimManager.getPlugin().getAresLogger().error("Inside is null at " + perimeter.toString());
                    }

                    // INSIDE ID IS NOT EQUAL TO OWNER ID
                    if (inside != null && !inside.getOwner().equals(owner.getUniqueId())) {
                        System.out.println("Inside ID: " + inside.getOwner().toString() + ", Owner ID: " + owner.getUniqueId().toString());
                    }

                    new Scheduler(subclaimManager.getPlugin()).sync(() ->
                            promise.reject("Subclaim is not inside " + owner.getName() + "'s Claims")).run();

                    return;
                }
            }

            for (Subclaim existing : subclaimManager.getSubclaimsByOwner(owner.getUniqueId())) {
                if (existing.isOverlapping(subclaim)) {
                    new Scheduler(subclaimManager.getPlugin()).sync(() -> promise.reject("Subclaim is overlapping an existing subclaim")).run();
                    return;
                }
            }

            new Scheduler(subclaimManager.getPlugin()).sync(() -> promise.resolve(subclaim)).run();
        }).run();
    }
}

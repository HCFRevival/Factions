package gg.hcfactions.factions.models.claim.impl;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.claims.ClaimManager;
import gg.hcfactions.factions.models.claim.EClaimPillarType;
import gg.hcfactions.factions.models.claim.IClaimBuilder;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public final class ClaimBuilder implements IClaimBuilder {
    @Getter public final ClaimManager manager;
    @Getter public final IFaction faction;
    @Getter public final Player player;
    @Getter public BLocatable cornerA;
    @Getter public BLocatable cornerB;

    public ClaimBuilder(ClaimManager manager, IFaction faction, Player player) {
        this.manager = manager;
        this.faction = faction;
        this.player = player;
        this.cornerA = null;
        this.cornerB = null;
    }

    @Override
    public void setCorner(BLocatable location, EClaimPillarType pillarType) {
        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (pillarType.equals(EClaimPillarType.A)) {
            this.cornerA = location;
        } else if (pillarType.equals(EClaimPillarType.B)) {
            this.cornerB = location;
        }

        if (factionPlayer != null) {
            final ClaimPillar pillar = new ClaimPillar(player, location, pillarType);
            final ClaimPillar previous = factionPlayer.getExistingClaimPillar(pillarType);

            if (previous != null) {
                previous.hide();
                factionPlayer.getPillars().remove(previous);
            }

            factionPlayer.getPillars().add(pillar);
            new Scheduler(manager.getPlugin()).sync(pillar::draw).run();
        }

        player.sendMessage(FMessage.LAYER_2 + "Claim point " + FMessage.LAYER_1 + pillarType.name() + FMessage.LAYER_2 + " set at " + FMessage.LAYER_1 + location.toString());

        if (cornerA != null && cornerB != null) {
            final double xMin = Math.min(cornerA.getX(), cornerB.getX());
            final double zMin = Math.min(cornerA.getZ(), cornerB.getZ());
            final double xMax = Math.max(cornerA.getX(), cornerB.getX());
            final double zMax = Math.max(cornerA.getZ(), cornerB.getZ());
            final double a = (int)Math.round(Math.abs(xMax - xMin));
            final double b = (int)Math.round(Math.abs(zMax - zMin));
            final double area = (a * b);
            final boolean isLargeClaim = (area >= manager.getPlugin().getConfiguration().getLargeClaimThreshold());

            player.sendMessage(ChatColor.AQUA + "Claim value" + ChatColor.YELLOW + ": $" + String.format("%.2f", calculateCost()) + (isLargeClaim ? ChatColor.RED + " (Large Claim)" : ""));
        }
    }

    @Override
    public void reset() {
        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        this.cornerA = null;
        this.cornerB = null;

        if (factionPlayer != null) {
            factionPlayer.hideClaimPillars();
        }

        player.sendMessage(FMessage.LAYER_1 + "Claim reset");
    }

    @Override
    public double calculateCost() {
        final double xMin = Math.min(cornerA.getX(), cornerB.getX());
        final double zMin = Math.min(cornerA.getZ(), cornerB.getZ());
        final double xMax = Math.max(cornerA.getX(), cornerB.getX());
        final double zMax = Math.max(cornerA.getZ(), cornerB.getZ());

        final double a = (int)Math.round(Math.abs(xMax - xMin));
        final double b = (int)Math.round(Math.abs(zMax - zMin));
        final double area = (a * b);

        if (area >= manager.getPlugin().getConfiguration().getLargeClaimThreshold()) {
            final double pre = a * b * manager.getPlugin().getConfiguration().getClaimBlockValue();
            final double tax = pre * manager.getPlugin().getConfiguration().getLargeClaimTax();
            return (pre + tax);
        }

        return a * b * manager.getPlugin().getConfiguration().getClaimBlockValue();
    }

    @Override
    public void build(FailablePromise<Claim> promise) {
        final boolean hasBypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);
        final boolean isEOTW = (manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_1)
                || manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2));

        if (cornerA == null) {
            promise.reject("Corner A is not set");
            return;
        }

        if (cornerB == null) {
            promise.reject("Corner B is not set");
            return;
        }

        if (!cornerA.getWorldName().equals(cornerB.getWorldName())) {
            promise.reject("Claim corners are not in the same world");
            return;
        }

        if (isEOTW && !hasBypass) {
            promise.reject("You can not claim during End of the World");
            return;
        }

        final double cost = calculateCost();
        final Claim claim = new Claim(manager, faction.getUniqueId(), cornerA, cornerB, cost);

        if (faction instanceof PlayerFaction) {
            if (!cornerA.getBukkitBlock().getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                promise.reject("Player factions can only claim in the Overworld");
                return;
            }

            claim.getCornerA().setY(-64);
            claim.getCornerB().setY(320);
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            final List<Claim> existingClaims = manager.getClaimsByOwner(faction);
            boolean isTouching = existingClaims.isEmpty() || faction instanceof ServerFaction;
            final int[] lxw = claim.getSize();
            final int smallSide = Math.min(lxw[0], lxw[1]);
            final int largeSide = Math.max(lxw[0], lxw[1]);
            final int minClaimRatio = manager.getPlugin().getConfiguration().getClaimMinRatio();
            final int minClaimSize = manager.getPlugin().getConfiguration().getClaimMinSize();

            if (lxw[0] < minClaimSize || lxw[1] < minClaimSize && !hasBypass) {
                new Scheduler(manager.getPlugin()).sync(() -> promise.reject("Minimum claim size is " + minClaimSize + "x" + minClaimSize)).run();
                return;
            }

            if ((largeSide / smallSide) > minClaimRatio && !hasBypass) {
                new Scheduler(manager.getPlugin()).sync(() -> promise.reject("Minimum claim ratio is " + minClaimRatio + ":1")).run();
                return;
            }

            if (!hasBypass) {
                for (Claim existing : manager.getClaimRepository()) {
                    if (existing.isOverlapping(claim)) {
                        new Scheduler(manager.getPlugin()).sync(() -> promise.reject("Claim is overlapping an existing claim")).run();
                        return;
                    }
                }

                for (BLocatable perimeter : claim.getPerimeter(64)) {
                    for (Claim nearby : manager.getClaimsNearby(perimeter, false)) {
                        if (!nearby.getOwner().equals(faction.getUniqueId())) {
                            final IFaction nearbyFaction = manager.getPlugin().getFactionManager().getFactionById(nearby.getOwner());
                            final double buffer = (nearbyFaction instanceof ServerFaction) ? ((ServerFaction)nearbyFaction).getClaimBuffer() : manager.getPlugin().getConfiguration().getDefaultPlayerFactionClaimBuffer();
                            final BLocatable closestPerimeter = nearby.getClosestPerimeter(perimeter, buffer);
                            final double closestDist = (closestPerimeter != null) ? closestPerimeter.getDistance(perimeter) : 0.0;

                            new Scheduler(manager.getPlugin()).sync(() ->
                                    promise.reject("Claim can not be within " + buffer + " blocks of " + nearbyFaction.getName() + " (Currently " + closestDist + " blocks away)")).run();

                            return;
                        }
                    }

                    if (!isTouching) {
                        for (Claim existing : existingClaims) {
                            if (existing.isTouching(perimeter)) {
                                isTouching = true;
                                break;
                            }
                        }
                    }
                }

                if (!isTouching) {
                    new Scheduler(manager.getPlugin()).sync(() -> promise.reject("Claim is not touching existing claims")).run();
                    return;
                }

                if (faction instanceof final PlayerFaction playerFaction) {
                    if (playerFaction.getBalance() < cost) {
                        new Scheduler(manager.getPlugin()).sync(() -> promise.reject("Your faction can not afford this claim")).run();
                        return;
                    }

                    playerFaction.setBalance(playerFaction.getBalance() - cost);
                }
            }

            new Scheduler(manager.getPlugin()).sync(() -> promise.resolve(claim)).run();
        }).run();
    }
}

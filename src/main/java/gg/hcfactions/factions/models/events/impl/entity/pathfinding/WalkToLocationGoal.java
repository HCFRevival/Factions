package gg.hcfactions.factions.models.events.impl.entity.pathfinding;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;

import java.util.List;

public final class WalkToLocationGoal extends Goal {
    protected final Mob mob;
    private Path path;
    private double speedModifier;
    private double minRange;
    private List<Location> destinations;
    private int locationIndex;

    public WalkToLocationGoal(Mob mob, List<BLocatable> locations, double speedModifier, double minRange) {
        this.mob = mob;
        this.destinations = Lists.newArrayList();
        this.locationIndex = 0;
        this.speedModifier = speedModifier;
        this.minRange = minRange;
        this.path = null;

        locations.forEach(loc -> destinations.add(loc.getBukkitBlock().getLocation()));
    }

    public WalkToLocationGoal(Mob mob, List<BLocatable> locations, double speedModifier, double minRange, int startLocationIndex) {
        this(mob, locations, speedModifier, minRange);

        if (startLocationIndex > locations.size()) {
            throw new IllegalArgumentException("Start location index can not be greater than location array");
        }

        this.locationIndex = startLocationIndex;
    }

    private Location getDestination() {
        return destinations.get(locationIndex);
    }

    private void assignNewDestination() {
        locationIndex += 1;

        if (locationIndex >= destinations.size()) {
            locationIndex = 0;
        }
    }

    @Override
    public boolean canUse() {
        if (!mob.getBukkitEntity().getWorld().equals(getDestination().getWorld())) {
            return false;
        }

        if (mob.getBukkitEntity().getLocation().distance(getDestination()) <= 0.5) {
            return false;
        }

        final GroundPathNavigation nav = (GroundPathNavigation) mob.getNavigation();

        if (path == null || path.isDone()) {
            final Location dest = getDestination();
            path = nav.createPath(dest.getBlockX(), dest.getBlockY() + 1, dest.getBlockZ(), 0);
        }

        return (path != null && !path.isDone());
    }

    @Override
    public void tick() {
        if (mob.getBukkitEntity().getLocation().distance(getDestination()) <= minRange) {
            assignNewDestination();

            final GroundPathNavigation nav = (GroundPathNavigation) mob.getNavigation();
            final Location destination = getDestination();

            nav.stop();
            path = nav.createPath(destination.getBlockX(), destination.getBlockY(), destination.getBlockZ(), 0);
        }

        if (path != null) {
            mob.getNavigation().moveTo(this.path, this.speedModifier);

            if (mob.isInWater() || mob.isInLava()) {
                mob.getJumpControl().jump();
            }
        }
    }
}

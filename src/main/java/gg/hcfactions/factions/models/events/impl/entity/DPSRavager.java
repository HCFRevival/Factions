package gg.hcfactions.factions.models.events.impl.entity;

import gg.hcfactions.factions.models.events.EDPSEntityType;
import gg.hcfactions.factions.models.events.IDPSEntity;
import gg.hcfactions.factions.models.events.impl.entity.pathfinding.WalkToLocationGoal;
import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.util.Objects;

public final class DPSRavager extends Ravager implements IDPSEntity {
    @Getter public final DPSEvent event;
    @Getter public final Location origin;
    @Getter @Setter public Location suspendedLocation;
    @Getter @Setter public boolean recentlySuspended;
    @Getter public final EDPSEntityType entityType = EDPSEntityType.RAVAGER;

    public DPSRavager(DPSEvent event, Location origin) {
        super(EntityType.RAVAGER, ((CraftWorld) Objects.requireNonNull(origin.getWorld())).getHandle());
        this.event = event;
        this.origin = origin;
        this.suspendedLocation = null;
        this.recentlySuspended = false;

        this.goalSelector.addGoal(0, new WalkToLocationGoal(this, event.getSpawnpoints(), 1, 3.0));

        Objects.requireNonNull(this.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(100.0D);

        setup();
    }

    public DPSRavager(DPSRavager clone) {
        super(EntityType.RAVAGER, ((CraftWorld) Objects.requireNonNull(clone.getEntity().getWorld())).getHandle());

        final Location lastLocation = (isSuspended() ? getSuspendedLocation() : clone.getEntity().getLocation());

        this.event = clone.getEvent();
        this.origin = lastLocation;
        this.suspendedLocation = clone.getSuspendedLocation();
        this.recentlySuspended = clone.isRecentlySuspended();
        this.goalSelector.addGoal(0, new WalkToLocationGoal(this, event.getSpawnpoints(), 1, 3.0, getClosestNodeIndex(new BLocatable(lastLocation.getBlock()), event.getSpawnpoints())));

        Objects.requireNonNull(this.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(100.0D);

        setup();
        getEntity().teleport(lastLocation);
    }

    @Override
    public CraftEntity getEntity() {
        return getBukkitEntity();
    }

    @Override
    public void spawn() {
        level().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public void despawn() {
        remove(RemovalReason.DISCARDED);
    }

    @Override
    public boolean removeWhenFarAway(double d0) {
        return false;
    }

    @Override
    protected void pickUpItem(ItemEntity entityitem) {}

    @Override
    public void knockback(double d0, double d1, double d2) {
        super.knockback(0.0, 0.0, 0.0);
    }

    @Override
    public void move(MoverType enummovetype, Vec3 vec3d) {
        if (enummovetype.equals(MoverType.PLAYER)) {}

        super.move(enummovetype, vec3d);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isBaby() {
        return false;
    }


    @Override
    public boolean canHoldItem(ItemStack itemstack) {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        return false;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return null;
    }
}

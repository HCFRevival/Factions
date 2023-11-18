package gg.hcfactions.factions.models.events.impl.entity;

import gg.hcfactions.factions.models.events.EDPSEntityType;
import gg.hcfactions.factions.models.events.IDPSEntity;
import gg.hcfactions.factions.models.events.impl.entity.pathfinding.WalkToLocationGoal;
import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import lombok.Getter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Phantom;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Objects;

public final class DPSPhantom extends Phantom implements IDPSEntity {
    @Getter public final DPSEvent event;
    @Getter public final Location origin;
    @Getter public final EDPSEntityType entityType = EDPSEntityType.PHANTOM;

    public DPSPhantom(DPSEvent event, Location origin) {
        super(EntityType.PHANTOM, ((CraftWorld) Objects.requireNonNull(origin.getWorld())).getHandle());
        this.event = event;
        this.origin = origin;

        this.goalSelector.addGoal(0, new WalkToLocationGoal(this, event.getSpawnpoints(), 1.0));

        setup(getBukkitEntity());
        setPhantomSize(64);
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
    protected boolean isSunBurnTick() {
        return false;
    }
}

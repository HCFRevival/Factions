package gg.hcfactions.factions.models.boss.impl;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.boss.IBossEntity;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.utils.Worlds;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

public final class BossPhantom extends Phantom implements IBossEntity {
    private static final Random RANDOM = new Random();
    private static final double HEALTH = 300.0;
    private static final int FIRE_WEAPON_COOLDOWN = 3;
    private static final int FIRE_WEAPON_MAX_ROUNDS = 5;
    private static final float FIRE_WEAPON_VELOCITY = 1.0f;

    @Getter private final Factions plugin;
    @Getter private final EntityType bukkitType = EntityType.PHANTOM;
    @Getter private final Location originLocation;
    @Getter private long nextWindCharge;
    @Getter private Vec3 moveTargetPoint = Vec3.ZERO;
    @Getter private AttackPhase attackPhase = AttackPhase.CIRCLE;
    @Getter private BlockPos anchorPoint = BlockPos.ZERO;

    public BossPhantom(Factions plugin, Location origin) {
        super(net.minecraft.world.entity.EntityType.PHANTOM, ((CraftWorld) origin.getWorld()).getHandle());
        this.plugin = plugin;
        this.originLocation = origin;
        this.nextWindCharge = Time.now();

        this.moveControl = new PhantomMoveControl(this);
        this.lookControl = new PhantomLookControl(this);

        setAttributes();
        initializeBukkitEntity();
    }

    private void setAttributes() {
        Objects.requireNonNull(getAttribute(Attributes.SCALE)).setBaseValue(8.0);
        Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(10.0);
        Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(HEALTH);
        Objects.requireNonNull(getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(10.0);
    }

    private void initializeBukkitEntity() {
        CraftLivingEntity livingEntity = (CraftLivingEntity) getBukkitEntity();
        livingEntity.teleport(originLocation);
        livingEntity.setHealth(HEALTH);
        livingEntity.getPersistentDataContainer().set(plugin.getNamespacedKey(), PersistentDataType.STRING, "boss");
    }

    @Override
    public void registerGoals() {
        this.goalSelector.addGoal(1, new PhantomAttackStrategyGoal());
        this.goalSelector.addGoal(2, new PhantomSweepAttackGoal());
        this.goalSelector.addGoal(3, new PhantomCircleAroundAnchorGoal());
        this.targetSelector.addGoal(1, new PhantomAttackPlayerTargetGoal());
    }

    @Override
    public void spawn() {
        level().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        Worlds.playSound(originLocation, Sound.ENTITY_PHANTOM_FLAP);
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
    public void move(MoverType enummovetype, Vec3 vec3d) {
        super.move(enummovetype, vec3d);

        if (nextWindCharge <= Time.now() && getTarget() != null && getTarget().isAlive()) {
            fireWeapon(getTarget(), Math.abs(RANDOM.nextInt(FIRE_WEAPON_MAX_ROUNDS)));
            nextWindCharge = Time.now() + (FIRE_WEAPON_COOLDOWN * 1000L);
        }
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        return super.hurt(damagesource, f);
    }

    private Location predictTargetLocation(LivingEntity target, Location shooterLocation) {
        Location targetLocation = target.getBukkitEntity().getLocation();
        Vector targetVelocity = target.getBukkitEntity().getVelocity();
        double distance = shooterLocation.distance(targetLocation);
        double timeToHit = distance / FIRE_WEAPON_VELOCITY;

        Vector predictedPosition = targetLocation.toVector().add(targetVelocity.multiply(timeToHit));
        return new Location(targetLocation.getWorld(), predictedPosition.getX(), predictedPosition.getY(), predictedPosition.getZ());
    }

    private void fireWeapon(LivingEntity target, int count) {
        for (int i = 0; i < count; i++) {
            new Scheduler(plugin).sync(() -> {
                Location currentLocation = getBukkitEntity().getLocation();
                Location targetLocation = predictTargetLocation(target, currentLocation);
                Vector direction = targetLocation.toVector().subtract(currentLocation.toVector()).normalize();
                LargeFireball projectile = getBukkitEntity().getWorld().spawn(currentLocation, LargeFireball.class);

                projectile.setYield(4.0f);
                projectile.setIsIncendiary(true);
                projectile.setVelocity(direction.multiply(FIRE_WEAPON_VELOCITY));
                projectile.setShooter((ProjectileSource) getBukkitEntity());

                Worlds.playSound(currentLocation, Sound.ENTITY_GHAST_SHOOT);
            }).delay(i * 10L).run();
        }
    }

    private enum AttackPhase {
        CIRCLE,
        SWOOP
    }

    private class PhantomAttackPlayerTargetGoal extends Goal {
        private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0);
        private int nextScanTick = reducedTickDelay(20);

        @Override
        public boolean canUse() {
            if (nextScanTick > 0) {
                nextScanTick--;
                return false;
            }
            nextScanTick = reducedTickDelay(60);
            List<net.minecraft.world.entity.player.Player> players = BossPhantom.this.level().getNearbyPlayers(
                    attackTargeting, BossPhantom.this, BossPhantom.this.getBoundingBox().inflate(16.0, 64.0, 16.0));
            if (!players.isEmpty()) {
                players.sort(Comparator.comparingDouble((e) -> ((net.minecraft.world.entity.player.Player)e).getY()).reversed());
                for (net.minecraft.world.entity.player.Player player : players) {
                    if (BossPhantom.this.canAttack(player, TargetingConditions.DEFAULT)) {
                        BossPhantom.this.setTarget(player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = BossPhantom.this.getTarget();
            return target != null && BossPhantom.this.canAttack(target, TargetingConditions.DEFAULT);
        }
    }

    private class PhantomAttackStrategyGoal extends Goal {
        private int nextSweepTick;

        @Override
        public boolean canUse() {
            LivingEntity target = BossPhantom.this.getTarget();
            return target != null && BossPhantom.this.canAttack(target, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            nextSweepTick = adjustedTickDelay(10);
            BossPhantom.this.attackPhase = AttackPhase.CIRCLE;
            setAnchorAboveTarget();
        }

        @Override
        public void stop() {
            BossPhantom.this.anchorPoint = BossPhantom.this.level().getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING, BossPhantom.this.anchorPoint).above(10 + RANDOM.nextInt(20));
        }

        @Override
        public void tick() {
            if (BossPhantom.this.attackPhase == AttackPhase.CIRCLE) {
                nextSweepTick--;
                if (nextSweepTick <= 0) {
                    BossPhantom.this.attackPhase = AttackPhase.SWOOP;
                    setAnchorAboveTarget();
                    nextSweepTick = adjustedTickDelay((8 + RANDOM.nextInt(4)) * 20);
                    BossPhantom.this.playSound(SoundEvents.PHANTOM_SWOOP, 10.0F, 0.95F + RANDOM.nextFloat() * 0.1F);
                }
            }
        }

        private void setAnchorAboveTarget() {
            LivingEntity target = BossPhantom.this.getTarget();
            if (target != null) {
                BossPhantom.this.anchorPoint = target.blockPosition().above(20 + RANDOM.nextInt(20));
                if (BossPhantom.this.anchorPoint.getY() < BossPhantom.this.level().getSeaLevel()) {
                    BossPhantom.this.anchorPoint = new BlockPos(
                            BossPhantom.this.anchorPoint.getX(), BossPhantom.this.level().getSeaLevel() + 1,
                            BossPhantom.this.anchorPoint.getZ());
                }
            }
        }
    }

    private class PhantomBodyRotationControl extends BodyRotationControl {
        public PhantomBodyRotationControl(Mob mob) {
            super(mob);
        }

        @Override
        public void clientTick() {
            BossPhantom.this.yHeadRot = BossPhantom.this.yBodyRot;
            BossPhantom.this.yBodyRot = BossPhantom.this.getYRot();
        }
    }

    private class PhantomCircleAroundAnchorGoal extends PhantomMoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        @Override
        public boolean canUse() {
            return BossPhantom.this.getTarget() == null || BossPhantom.this.attackPhase == AttackPhase.CIRCLE;
        }

        @Override
        public void start() {
            distance = 5.0F + RANDOM.nextFloat() * 10.0F;
            height = -4.0F + RANDOM.nextFloat() * 9.0F;
            clockwise = RANDOM.nextBoolean() ? 1.0F : -1.0F;
            selectNext();
        }

        @Override
        public void tick() {
            if (RANDOM.nextInt(adjustedTickDelay(350)) == 0) {
                height = -4.0F + RANDOM.nextFloat() * 9.0F;
            }

            if (RANDOM.nextInt(adjustedTickDelay(250)) == 0) {
                distance++;
                if (distance > 15.0F) {
                    distance = 5.0F;
                    clockwise = -clockwise;
                }
            }

            if (RANDOM.nextInt(adjustedTickDelay(450)) == 0) {
                angle = RANDOM.nextFloat() * 2.0F * (float) Math.PI;
                selectNext();
            }

            if (touchingTarget()) {
                selectNext();
            }

            if (BossPhantom.this.moveTargetPoint.y < BossPhantom.this.getY() &&
                    !BossPhantom.this.level().isEmptyBlock(BossPhantom.this.blockPosition().below(1))) {
                height = Math.max(1.0F, height);
                selectNext();
            }

            if (BossPhantom.this.moveTargetPoint.y > BossPhantom.this.getY() &&
                    !BossPhantom.this.level().isEmptyBlock(BossPhantom.this.blockPosition().above(1))) {
                height = Math.min(-1.0F, height);
                selectNext();
            }
        }

        private void selectNext() {
            if (BlockPos.ZERO.equals(BossPhantom.this.anchorPoint)) {
                BossPhantom.this.anchorPoint = BossPhantom.this.blockPosition();
            }

            angle += clockwise * 15.0F * 0.017453292F;
            BossPhantom.this.moveTargetPoint = Vec3.atLowerCornerOf(BossPhantom.this.anchorPoint)
                    .add(distance * Mth.cos(angle), -4.0F + height, distance * Mth.sin(angle));
        }
    }

    private class PhantomLookControl extends LookControl {
        public PhantomLookControl(Phantom phantom) {
            super(phantom);
        }

        @Override
        public void tick() {
            // No specific implementation needed
        }
    }

    private class PhantomMoveControl extends MoveControl {
        private float speed = 0.1F;

        public PhantomMoveControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (BossPhantom.this.horizontalCollision) {
                BossPhantom.this.setYRot(BossPhantom.this.getYRot() + 180.0F);
                speed = 0.1F;
            }

            double dx = BossPhantom.this.moveTargetPoint.x - BossPhantom.this.getX();
            double dy = BossPhantom.this.moveTargetPoint.y - BossPhantom.this.getY();
            double dz = BossPhantom.this.moveTargetPoint.z - BossPhantom.this.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            if (horizontalDistance > 1e-6) {
                double scalingFactor = 1.0 - Math.abs(dy * 0.7) / horizontalDistance;
                dx *= scalingFactor;
                dz *= scalingFactor;
                horizontalDistance = Math.sqrt(dx * dx + dz * dz);
                double totalDistance = Math.sqrt(dx * dx + dz * dz + dy * dy);
                float currentYRot = BossPhantom.this.getYRot();
                float targetYRot = (float) Mth.atan2(dz, dx);
                float wrappedYRot = Mth.wrapDegrees(BossPhantom.this.getYRot() + 90.0F);
                float wrappedTargetYRot = Mth.wrapDegrees(targetYRot * 57.295776F);

                BossPhantom.this.setYRot(Mth.approachDegrees(wrappedYRot, wrappedTargetYRot, 4.0F) - 90.0F);
                BossPhantom.this.yBodyRot = BossPhantom.this.getYRot();

                if (Mth.degreesDifferenceAbs(currentYRot, BossPhantom.this.getYRot()) < 3.0F) {
                    speed = Mth.approach(speed, 1.8F, 0.005F * (1.8F / speed));
                } else {
                    speed = Mth.approach(speed, 0.2F, 0.025F);
                }

                float pitch = (float) -(Mth.atan2(-dy, horizontalDistance) * 57.2957763671875);
                BossPhantom.this.setXRot(pitch);
                float yaw = BossPhantom.this.getYRot() + 90.0F;
                double velocityX = speed * Mth.cos(yaw * 0.017453292F) * Math.abs(dx / totalDistance);
                double velocityZ = speed * Mth.sin(yaw * 0.017453292F) * Math.abs(dz / totalDistance);
                double velocityY = speed * Mth.sin(pitch * 0.017453292F) * Math.abs(dy / totalDistance);
                Vec3 currentVelocity = BossPhantom.this.getDeltaMovement();

                BossPhantom.this.setDeltaMovement(currentVelocity.add(new Vec3(velocityX, velocityY, velocityZ).subtract(currentVelocity).scale(0.2)));
            }
        }
    }

    private abstract class PhantomMoveTargetGoal extends Goal {
        public PhantomMoveTargetGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return BossPhantom.this.moveTargetPoint.distanceToSqr(BossPhantom.this.getX(), BossPhantom.this.getY(), BossPhantom.this.getZ()) < 4.0;
        }
    }

    private class PhantomSweepAttackGoal extends PhantomMoveTargetGoal {
        private static final int CAT_SEARCH_TICK_DELAY = 20;
        private boolean isScaredOfCat;
        private int catSearchTick;

        @Override
        public boolean canUse() {
            return BossPhantom.this.getTarget() != null && BossPhantom.this.attackPhase == AttackPhase.SWOOP;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = BossPhantom.this.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }

            if (target instanceof net.minecraft.world.entity.player.Player player && (player.isSpectator() || player.isCreative())) {
                return false;
            }

            if (!canUse()) {
                return false;
            }

            if (BossPhantom.this.tickCount > catSearchTick) {
                catSearchTick = BossPhantom.this.tickCount + CAT_SEARCH_TICK_DELAY;
                List<Cat> cats = BossPhantom.this.level().getEntitiesOfClass(Cat.class, BossPhantom.this.getBoundingBox().inflate(16.0), EntitySelector.ENTITY_STILL_ALIVE);
                for (Cat cat : cats) {
                    cat.hiss();
                }
                isScaredOfCat = !cats.isEmpty();
            }

            return !isScaredOfCat;
        }

        @Override
        public void stop() {
            BossPhantom.this.setTarget(null);
            BossPhantom.this.attackPhase = AttackPhase.CIRCLE;
        }

        @Override
        public void tick() {
            LivingEntity target = BossPhantom.this.getTarget();
            if (target != null) {
                BossPhantom.this.moveTargetPoint = new Vec3(target.getX(), target.getY(0.5), target.getZ());
                if (BossPhantom.this.getBoundingBox().inflate(0.2).intersects(target.getBoundingBox())) {
                    BossPhantom.this.doHurtTarget(target);
                    BossPhantom.this.attackPhase = AttackPhase.CIRCLE;
                    if (!BossPhantom.this.isSilent()) {
                        BossPhantom.this.level().levelEvent(1039, BossPhantom.this.blockPosition(), 0);
                    }
                } else if (BossPhantom.this.horizontalCollision || BossPhantom.this.hurtTime > 0) {
                    BossPhantom.this.attackPhase = AttackPhase.CIRCLE;
                }
            }
        }
    }
}

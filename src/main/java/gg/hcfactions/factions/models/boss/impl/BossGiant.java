package gg.hcfactions.factions.models.boss.impl;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.boss.IBossEntity;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.utils.Worlds;
import lombok.Getter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.player.Player;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.Random;

public final class BossGiant extends Giant implements IBossEntity {
    private static final Random RANDOM = new Random();
    private static final float KICK_CHANCE = 0.2F;
    private static final double KICK_FORCE_BASE = 5.5D;
    private static final double KICK_FORCE_VARIANCE = 2.5D;
    private static final float STOMP_CHANCE = 0.15F;
    private static final double STOMP_FORCE_BASE = 48.0D;
    private static final double STOMP_FORCE_VARIANCE = 16.0D;
    private static final int MINION_MIN_COUNT = 3;
    private static final int MINION_MAX_COUNT = 6;
    private static final int KICK_COOLDOWN = 8;
    private static final int STOMP_COOLDOWN = 15;
    private static final double HEALTH = 400.0;

    @Getter public final org.bukkit.entity.EntityType bukkitType = org.bukkit.entity.EntityType.GIANT;
    private final Factions plugin;
    private long nextKick = Time.now();
    private long nextStomp = Time.now();
    private BukkitTask hasLandedTask;
    private Location originLocation;

    public BossGiant(Factions plugin, Location origin) {
        super(EntityType.GIANT, ((CraftWorld) Objects.requireNonNull(origin.getWorld())).getHandle());
        this.plugin = plugin;
        this.hasLandedTask = null;
        this.originLocation = origin;

        // Set attrs
        Objects.requireNonNull(getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(8.0);
        Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(HEALTH);

        final CraftLivingEntity livingEntity = (CraftLivingEntity) getBukkitEntity();
        livingEntity.teleport(origin);
        livingEntity.setHealth(HEALTH);
        livingEntity.getPersistentDataContainer().set(plugin.getNamespacedKey(), PersistentDataType.STRING, "boss");

        registerGoals();
    }

    @Override
    public void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.25));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
    }

    @Override
    public void spawn() {
        level().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        Worlds.playSound(originLocation, Sound.ENTITY_WARDEN_AGITATED);
    }

    @Override
    public void despawn() {
        remove(RemovalReason.DISCARDED);
    }

    @Override
    public boolean removeWhenFarAway(double d0) {
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (nextKick <= Time.now()) {
            if (entity instanceof final LivingEntity livingEntity) {
                final float pull = Math.abs(RANDOM.nextFloat(1.0F));

                if (pull <= KICK_CHANCE) {
                    kickEntity(
                            livingEntity,
                            getBukkitEntity().getLocation(),
                            Math.abs(RANDOM.nextDouble(KICK_FORCE_VARIANCE)) + KICK_FORCE_BASE
                    );

                    nextKick = Time.now() + (KICK_COOLDOWN*1000L);
                }
            }
        }

        return super.doHurtTarget(entity);
    }

    @Override
    protected boolean damageEntity0(DamageSource damagesource, float f) {
        if (damagesource.isIndirect()) {
            return false;
        }

        final float pull = Math.abs(RANDOM.nextFloat(1.0F));

        if (nextStomp <= Time.now() && pull <= STOMP_CHANCE) {
            performStomp();
            nextStomp = Time.now() + (STOMP_COOLDOWN*1000L);
        }

        return super.damageEntity0(damagesource, f);
    }

    private void kickEntity(LivingEntity affectedEntity, Location origin, double force) {
        final Vector vec = origin.getDirection().multiply(force).setY(1.25);
        affectedEntity.getBukkitEntity().setVelocity(vec);

        applyShockwaveEffects(affectedEntity, (int)Math.round(force));
    }

    private void shockwaveEntity(LivingEntity affectedEntity, Location origin, double force) {
        final Location playerLocation = affectedEntity.getBukkitEntity().getLocation();
        final Vector direction = playerLocation.toVector().subtract(origin.toVector()).normalize();
        final double distance = origin.distance(playerLocation);
        final double power = Math.min(force / distance, force);
        final Vector currentVelocity = affectedEntity.getBukkitEntity().getVelocity();
        final Vector addedVelocity = direction.multiply(power);

        final EntityDamageEvent damageEvent = new EntityDamageEvent(affectedEntity.getBukkitEntity(), EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, Math.round(64 / distance));
        Bukkit.getPluginManager().callEvent(damageEvent);

        if (!damageEvent.isCancelled()) {
            final float newHealth = (float)Math.max(affectedEntity.getHealth() - damageEvent.getFinalDamage(), 0);

            if (!(affectedEntity instanceof final Player player) || player.getBukkitEntity().getGameMode().equals(GameMode.SURVIVAL)) {
                affectedEntity.setLastHurtByMob(this);

                if (newHealth <= 0) {
                    affectedEntity.kill();
                } else {
                    affectedEntity.setHealth(newHealth);
                }
            }
        }

        if (affectedEntity.isAlive()) {
            affectedEntity.getBukkitEntity().setVelocity(currentVelocity.add(new Vector(addedVelocity.getX(), 0.8, addedVelocity.getZ())));
            applyShockwaveEffects(affectedEntity, (int)Math.round(power));
        }
    }

    private void applyShockwaveEffects(LivingEntity affectedEntity, int duration) {
        if (affectedEntity instanceof final Player player) {
            player.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration*20, 1));
            player.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration*20, 0));
        }
    }

    private void performStompShockwave() {
        getBukkitEntity().getWorld().getNearbyEntities(getBukkitEntity().getLocation(), 16, 4, 16).forEach(entity -> {
            if (entity instanceof final CraftLivingEntity livingEntity && !entity.getUniqueId().equals(getUUID()) && !entity.getType().equals(org.bukkit.entity.EntityType.ZOMBIE)) {
                final double force = Math.abs(RANDOM.nextDouble(STOMP_FORCE_VARIANCE)) + STOMP_FORCE_BASE;
                shockwaveEntity(livingEntity.getHandle(), getBukkitEntity().getLocation(), force);
            }
        });
    }

    private void performStomp() {
        if (hasLandedTask != null) {
            hasLandedTask.cancel();
            hasLandedTask = null;
        }

        hasLandedTask = new Scheduler(plugin).sync(() -> {
            if (getBukkitEntity().isOnGround()) {
                performStompShockwave();
                spawnMinions();

                hasLandedTask.cancel();
                hasLandedTask = null;
            }
        }).repeat(5L, 1L).run();

        new Scheduler(plugin).sync(() -> {
            final Vector velocity = getBukkitEntity().getVelocity();
            getBukkitEntity().setVelocity(getBukkitEntity().getVelocity().add(new Vector(velocity.getX(), 1.5, velocity.getZ())));
            Worlds.playSound(getBukkitEntity().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP);
        }).delay(1L).run();
    }

    private void spawnMinions() {
        final int spawnCount = Math.max(Math.abs(RANDOM.nextInt(MINION_MAX_COUNT)), MINION_MIN_COUNT);

        for (int i = 0; i < spawnCount; i++) {
            final Zombie zombie = (Zombie) getBukkitEntity().getWorld().spawnEntity(getBukkitEntity().getLocation(), org.bukkit.entity.EntityType.ZOMBIE);
            zombie.setBaby();
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, PotionEffect.INFINITE_DURATION, 0));
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, PotionEffect.INFINITE_DURATION, 0));
            zombie.getPersistentDataContainer().set(plugin.getNamespacedKey(), PersistentDataType.STRING, "noMerge");
            zombie.setCanPickupItems(false);

            if (zombie.getEquipment() != null) {
                zombie.getEquipment().setItemInMainHand(new ItemBuilder().setMaterial(Material.GOLDEN_SWORD).setAmount(1).build());
            }
        }
    }
}

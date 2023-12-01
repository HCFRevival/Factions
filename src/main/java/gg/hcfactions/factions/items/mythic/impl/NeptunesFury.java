package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.EMythicAbilityType;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.items.mythic.MythicAbility;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.bukkit.utils.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class NeptunesFury implements IMythicItem {
    @AllArgsConstructor
    public static final class NeptunesFuryConfig {
        @Getter public final double explosionBase;
        @Getter public final double explosionVariance;
    }

    @Getter public final Factions plugin;
    @Getter public final List<MythicAbility> abilityInfo;
    private final Random random;
    private final NeptunesFuryConfig config;

    public NeptunesFury(Factions plugin, NeptunesFuryConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.random = new Random();
        this.abilityInfo = Lists.newArrayList();

        addAbilityInfo(
                Colors.DARK_AQUA.toBukkit() + "The Sea Calls",
                "Slaying an enemy while in riptide will channel a lightning strike with amplified explosive damage at the victims last location damaging and knocking back all nearby enemies.",
                EMythicAbilityType.ON_KILL
        );
    }

    @Override
    public Material getMaterial() {
        return Material.TRIDENT;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_AQUA + "Neptune's Fury";
    }

    @Override
    public List<String> getLore() {
        return getMythicLore();
    }

    @Override
    public Particle getAbilityParticle() {
        return Particle.BUBBLE_POP;
    }

    @Override
    public double getAbilityParticleSpeed() {
        return 0.01;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.put(Enchantment.DAMAGE_ALL, getMaxSharpness());
        enchantments.put(Enchantment.RIPTIDE, 4);
        enchantments.put(Enchantment.IMPALING, 3);
        return enchantments;
    }

    @Override
    public void onKill(Player player, LivingEntity slainEntity) {
        if (!player.isRiptiding()) {
            return;
        }

        if (!(slainEntity instanceof Player)) {
            return;
        }

        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);
        final List<LivingEntity> entities = slainEntity.getNearbyEntities(8, 4, 8).stream().filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity) e).toList();
        final double force = Math.abs(random.nextDouble(config.explosionVariance)) + config.explosionBase;

        spawnShockwaveParticle(slainEntity.getLocation());

        entities.forEach(livingEntity -> {
            // Shockwave players
            if (livingEntity instanceof final Player otherPlayer) {
                if (faction != null && !faction.isMember(otherPlayer)) {
                    shockwaveEntity(livingEntity, slainEntity.getLocation(), force);
                } else if (!otherPlayer.getUniqueId().equals(player.getUniqueId())) {
                    shockwaveEntity(livingEntity, slainEntity.getLocation(), force);
                }
            } else {
                shockwaveEntity(livingEntity, slainEntity.getLocation(), force);
            }
        });
    }

    private void spawnShockwaveParticle(Location location) {
        Objects.requireNonNull(location.getWorld()).spawnParticle(
                Particle.EXPLOSION_HUGE,
                location.getX(),
                location.getY() + 1.0,
                location.getZ(),
                3,
                0.4, 0.4, 0.4,
                0.01);
    }

    private void shockwaveEntity(LivingEntity affectedEntity, Location origin, double force) {
        final Location playerLocation = affectedEntity.getLocation();
        final Vector direction = playerLocation.toVector().subtract(origin.toVector()).normalize();
        final double distance = origin.distance(playerLocation);
        final double power = Math.min(force / distance, force);
        final Vector currentVelocity = affectedEntity.getVelocity();
        final Vector addedVelocity = direction.multiply(power);
        final EntityDamageEvent damageEvent = new EntityDamageEvent(affectedEntity, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, Math.round(64 / distance));

        Bukkit.getPluginManager().callEvent(damageEvent);
        if (damageEvent.isCancelled()) {
            return;
        }

        final float newHealth = (float)Math.max(affectedEntity.getHealth() - damageEvent.getFinalDamage(), 0);
        affectedEntity.setHealth(newHealth);
        affectedEntity.setLastDamageCause(damageEvent);

        final Vector vec = new Vector(addedVelocity.getX(), 0.2, addedVelocity.getZ());
        Bukkit.broadcastMessage(vec.toString());
        affectedEntity.setVelocity(currentVelocity.add(vec));
    }
}

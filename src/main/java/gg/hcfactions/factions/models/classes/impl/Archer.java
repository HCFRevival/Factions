package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Archer implements IClass {
    @Getter public ClassManager manager;
    @Getter public final String name = "Archer";
    @Getter public final String description = "Grants you bonus damage with a bow based on distance. Hit consecutive shots on the same player to deal bonus damage";
    @Getter public final int warmup;
    @Getter public final Material helmet = Material.LEATHER_HELMET;
    @Getter public final Material chestplate = Material.LEATHER_CHESTPLATE;
    @Getter public final Material leggings = Material.LEATHER_LEGGINGS;
    @Getter public final Material boots = Material.LEATHER_BOOTS;
    @Getter public final Material offhand = null;
    @Getter public Set<UUID> activePlayers;
    @Getter public Set<UUID> markedEntities;
    @Getter public Map<PotionEffectType, Integer> passiveEffects;
    @Getter public List<IConsumeable> consumables;
    @Getter public final Set<ArcherTag> archerTags;
    @Getter @Setter public double maxDealtDamage;
    @Getter @Setter public double consecutiveMultiplier;
    @Getter @Setter public double consecutiveBase;
    @Getter @Setter public double damagePerBlock;
    @Getter @Setter public int markDuration;
    @Getter @Setter public double markPercentage;

    public Archer(ClassManager manager) {
        this.manager = manager;
        this.warmup = 30;
        this.maxDealtDamage = 75.0;
        this.consecutiveBase = 5.0;
        this.consecutiveMultiplier = 1.25;
        this.damagePerBlock = 0.1;
        this.markDuration = 10;
        this.markPercentage = 0.15;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.archerTags = Sets.newConcurrentHashSet();
        this.markedEntities = Sets.newConcurrentHashSet();
    }

    public Archer(
            ClassManager manager,
            int warmup,
            double maxDealtDamage,
            double consecutiveBase,
            double consecutiveMultiplier,
            double damagePerBlock,
            int markDuration,
            double markPercentage
    ) {
        this.manager = manager;
        this.warmup = warmup;
        this.maxDealtDamage = maxDealtDamage;
        this.consecutiveBase = consecutiveBase;
        this.consecutiveMultiplier = consecutiveMultiplier;
        this.damagePerBlock = damagePerBlock;
        this.markDuration = markDuration;
        this.markPercentage = markPercentage;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.archerTags = Sets.newConcurrentHashSet();
        this.markedEntities = Sets.newConcurrentHashSet();
    }

    public int getHitCount(Player archer, LivingEntity victim) {
        return (int)archerTags.stream().filter(tag -> tag.getArcher().equals(archer.getUniqueId()) && tag.getVictim().equals(victim.getUniqueId())).count();
    }

    public void addHit(Player archer, LivingEntity victim, int expireSeconds) {
        final ArcherTag tag = new ArcherTag(archer.getUniqueId(), victim.getUniqueId());
        archerTags.removeIf(existing -> existing.getArcher().equals(archer.getUniqueId()) && !existing.getVictim().equals(victim.getUniqueId()));
        archerTags.add(tag);
        new Scheduler(getManager().getPlugin()).async(() -> archerTags.remove(tag)).delay(expireSeconds * 20L).run();
    }

    public void mark(LivingEntity victim) {
        if (markedEntities.contains(victim.getUniqueId())) {
            return;
        }

        final UUID uniqueId = victim.getUniqueId();

        markedEntities.add(uniqueId);
        new Scheduler(manager.getPlugin()).sync(() -> markedEntities.remove(uniqueId)).delay(markDuration*20L).run();
    }

    @AllArgsConstructor
    public final class ArcherTag {
        @Getter public UUID archer;
        @Getter public UUID victim;
    }
}

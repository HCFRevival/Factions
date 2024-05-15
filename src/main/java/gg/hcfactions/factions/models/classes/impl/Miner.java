package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Miner implements IClass {
    @Getter public ClassManager manager;
    @Getter public final String name = "Miner";
    @Getter public final String description = "Grants the ability to mine efficiently";
    @Getter public final int warmup;
    @Getter public final boolean emptyArmorEnforced = true;
    @Getter public final Material helmet = Material.IRON_HELMET;
    @Getter public final Material chestplate = Material.IRON_CHESTPLATE;
    @Getter public final Material leggings = Material.IRON_LEGGINGS;
    @Getter public final Material boots = Material.IRON_BOOTS;
    @Getter public final Material offhand = null;
    @Getter public Set<UUID> activePlayers;
    @Getter public Map<PotionEffectType, Integer> passiveEffects;
    @Getter public List<IConsumeable> consumables;

    public Miner(ClassManager manager) {
        this.manager = manager;
        this.warmup = 30;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
    }

    public Miner(ClassManager manager, int warmup) {
        this.manager = manager;
        this.warmup = warmup;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
    }
}

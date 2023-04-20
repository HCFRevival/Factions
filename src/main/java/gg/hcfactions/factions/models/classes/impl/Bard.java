package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Bard implements IClass {
    @Getter public ClassManager manager;
    @Getter public final String name = "Bard";
    @Getter public final String description = "Grant effects to nearby faction members and enemies";
    @Getter public final int warmup;
    @Getter public final Material helmet = Material.GOLDEN_HELMET;
    @Getter public final Material chestplate = Material.GOLDEN_CHESTPLATE;
    @Getter public final Material leggings = Material.GOLDEN_LEGGINGS;
    @Getter public final Material boots = Material.GOLDEN_BOOTS;
    @Getter public Set<UUID> activePlayers;
    @Getter public Map<PotionEffectType, Integer> passiveEffects;
    @Getter public List<IConsumeable> consumables;
    @Getter @Setter public double bardRange;

    public Bard(ClassManager manager) {
        this.manager = manager;
        this.warmup = 30;
        this.bardRange = 16.0;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
    }

    public Bard(ClassManager manager, int warmup, double bardRange) {
        this.manager = manager;
        this.warmup = warmup;
        this.bardRange = bardRange;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
    }
}

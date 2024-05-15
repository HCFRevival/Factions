package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Diver implements IClass {
    @Getter public ClassManager manager;
    @Getter public final String name = "Diver";
    @Getter public final String description = "Grants you the ability to use a Trident";
    @Getter public final int warmup;
    @Getter public final boolean emptyArmorEnforced = true;
    @Getter public final Material helmet = Material.TURTLE_HELMET;
    @Getter public final Material chestplate = Material.DIAMOND_CHESTPLATE;
    @Getter public final Material leggings = Material.DIAMOND_LEGGINGS;
    @Getter public final Material boots = Material.DIAMOND_BOOTS;
    @Getter public final Material offhand = null;
    @Getter public Set<UUID> activePlayers;
    @Getter public Map<PotionEffectType, Integer> passiveEffects;
    @Getter public Map<UUID, Long> seaCallCooldowns;
    @Getter public List<IConsumeable> consumables;
    @Getter @Setter public double damageMultiplier;
    @Getter @Setter public double minimumRange;
    @Getter public int seaCallCooldown;
    @Getter public int seaCallDuration;

    public Diver(ClassManager manager) {
        this.manager = manager;
        this.warmup = 30;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.seaCallCooldowns = Maps.newConcurrentMap();
        this.consumables = Lists.newArrayList();
        this.damageMultiplier = 3.0;
        this.minimumRange = 10.0;
        this.seaCallCooldown = 10800;
        this.seaCallDuration = 600;
    }

    public Diver(ClassManager manager, int warmup, double damageMultiplier, double minimumRange, int seaCallCooldown, int seaCallDuration) {
        this.manager = manager;
        this.warmup = warmup;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.seaCallCooldowns = Maps.newConcurrentMap();
        this.consumables = Lists.newArrayList();
        this.damageMultiplier = damageMultiplier;
        this.minimumRange = minimumRange;
        this.seaCallCooldown = seaCallCooldown;
        this.seaCallDuration = seaCallDuration;
    }

    public boolean hasSeaCallCooldown(Player player) {
        return seaCallCooldowns.containsKey(player.getUniqueId());
    }
}

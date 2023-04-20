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

public final class Rogue implements IClass {
    @Getter public ClassManager manager;
    @Getter public final String name = "Rogue";
    @Getter public final String description = "Grants you the ability to backstab players with Golden Swords";
    @Getter public final int warmup;
    @Getter public final Material helmet = Material.CHAINMAIL_HELMET;
    @Getter public final Material chestplate = Material.CHAINMAIL_CHESTPLATE;
    @Getter public final Material leggings = Material.CHAINMAIL_LEGGINGS;
    @Getter public final Material boots = Material.CHAINMAIL_BOOTS;
    @Getter public Set<UUID> activePlayers;
    @Getter public Map<PotionEffectType, Integer> passiveEffects;
    @Getter public List<IConsumeable> consumables;
    @Getter public final Map<UUID, Long> backstabCooldowns;
    @Getter @Setter public double backstabDamage;
    @Getter @Setter public int backstabTickrate;
    @Getter @Setter public int backstabCooldown;

    public Rogue(ClassManager manager) {
        this.manager = manager;
        this.warmup = 30;
        this.backstabDamage = 1.0;
        this.backstabTickrate = 10;
        this.backstabCooldown = 3;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.backstabCooldowns = Maps.newConcurrentMap();
    }

    public Rogue(ClassManager manager, int warmup, double backstabDamage, int backstabTickrate, int backstabCooldown) {
        this.manager = manager;
        this.warmup = warmup;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.backstabCooldowns = Maps.newConcurrentMap();
        this.backstabDamage = backstabDamage;
        this.backstabTickrate = backstabTickrate;
        this.backstabCooldown = backstabCooldown;
    }

    public boolean hasBackstabCooldown(Player player) {
        return backstabCooldowns.containsKey(player.getUniqueId());
    }
}

package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.classes.IClassHoldable;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;

public final class Holdable implements IClassHoldable {
    @Getter public final Factions plugin;
    @Getter public final Material material;
    @Getter public final PotionEffectType effectType;
    @Getter public final int amplifier;
    @Getter public final int duration;
    @Getter public final Map<UUID, Long> currentHolders;

    public Holdable(Factions plugin, Material material, PotionEffectType effectType, int amplifier, int duration) {
        this.plugin = plugin;
        this.material = material;
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.duration = duration;
        this.currentHolders = Maps.newConcurrentMap();
    }
}

package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.classes.EConsumableApplicationType;
import gg.hcfactions.factions.models.classes.IConsumeable;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;

public final class Consumable implements IConsumeable {
    @Getter public final Factions plugin;
    @Getter public final Material material;
    @Getter public final EConsumableApplicationType applicationType;
    @Getter public final PotionEffectType effectType;
    @Getter public final int duration;
    @Getter public final int cooldown;
    @Getter public final int amplifier;
    @Getter public final Map<UUID, Long> cooldowns;

    public Consumable(
            Factions plugin,
            Material material,
            EConsumableApplicationType applicationType,
            PotionEffectType effectType,
            int duration,
            int cooldown,
            int amplifier
    ) {
        this.plugin = plugin;
        this.material = material;
        this.applicationType = applicationType;
        this.effectType = effectType;
        this.duration = duration;
        this.cooldown = cooldown;
        this.amplifier = amplifier;
        this.cooldowns = Maps.newConcurrentMap();
    }
}

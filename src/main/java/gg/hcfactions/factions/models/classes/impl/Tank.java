package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.listeners.events.player.TankShieldReadyEvent;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Tank implements IClass {
    @Getter public ClassManager manager;
    @Getter public final String name = "Guardian";
    @Getter public final String description = "";
    @Getter public final int warmup;
    @Getter public final Material helmet = null;
    @Getter public final Material chestplate = Material.NETHERITE_CHESTPLATE;
    @Getter public final Material leggings = Material.NETHERITE_LEGGINGS;
    @Getter public final Material boots = Material.NETHERITE_BOOTS;
    @Getter public final Material offhand = Material.SHIELD;
    @Getter public final Map<PotionEffectType, Integer> passiveEffects;
    @Getter public final List<IConsumeable> consumables;
    @Getter public final Set<UUID> activePlayers;

    @Getter public final int shieldWarmup;
    @Getter public final Map<UUID, Double> stamina;
    @Getter public final Map<UUID, PLocatable> guardPoints;

    public Tank(ClassManager manager) {
        this.manager = manager;
        this.warmup = 30;
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.activePlayers = Sets.newConcurrentHashSet();
        this.shieldWarmup = 2;
        this.stamina = Maps.newConcurrentMap();
        this.guardPoints = Maps.newConcurrentMap();
    }

    public Tank(
            ClassManager manager,
            int warmup,
            int shieldWarmup,
            Map<PotionEffectType, Integer> passiveEffects,
            List<IConsumeable> consumables
    ) {
        this.manager = manager;
        this.warmup = warmup;
        this.passiveEffects = passiveEffects;
        this.consumables = consumables;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.shieldWarmup = shieldWarmup;
        this.stamina = Maps.newConcurrentMap();
        this.guardPoints = Maps.newConcurrentMap();
    }

    public void activateShield(Player player) {
        final TankShieldReadyEvent readyEvent = new TankShieldReadyEvent(player, this);
        Bukkit.getPluginManager().callEvent(readyEvent);

        if (readyEvent.isCancelled()) {
            return;
        }

        Bukkit.broadcastMessage("shield activated");
    }

    public double getStamina(UUID uniqueId) {
        return getStamina().getOrDefault(uniqueId, -1.0);
    }

    public double getStamina(Player player) {
        return getStamina(player.getUniqueId());
    }

    public PLocatable getGuardPoint(Player player) {
        return guardPoints.get(player.getUniqueId());
    }

    public boolean hasGuardPoint(Player player) {
        return guardPoints.containsKey(player.getUniqueId());
    }

    public void setGuardPoint(Player player) {
        guardPoints.put(player.getUniqueId(), new PLocatable(player));
    }
}

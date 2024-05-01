package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.EMythicAbilityType;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.items.mythic.MythicAbility;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Random;

public final class SerpentsImpaler implements IMythicItem {
    @AllArgsConstructor
    public static final class SerpentsImpalerConfig {
        @Getter public final float woundChance;
        @Getter public final int woundTicks;
        @Getter public double woundDamagePerTick;
    }

    @Getter public final Factions plugin;
    @Getter public final List<MythicAbility> abilityInfo;
    private final SerpentsImpalerConfig config;
    private final Random random;

    public SerpentsImpaler(Factions plugin, SerpentsImpalerConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.random = new Random();
        this.abilityInfo = Lists.newArrayList();

        addAbilityInfo(
                ChatColor.RED + "Impale",
                "Attacking an enemy while riptiding has a 25% chance to inflict wounds that will drain "
                        + config.getWoundDamagePerTick() + " ♥ over the span of " + config.woundTicks +  " seconds.",
                EMythicAbilityType.ON_HIT);
    }

    @Override
    public Material getMaterial() {
        return Material.TRIDENT;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_RED + "Serpent's Impaler";
    }

    @Override
    public List<String> getLore() {
        return getMythicLore();
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.put(Enchantment.SHARPNESS, getMaxSharpness());
        enchantments.put(Enchantment.IMPALING, 3);
        enchantments.put(Enchantment.CHANNELING, 1);
        return enchantments;
    }

    @Override
    public void onShoot(Player player, LivingEntity attackedEntity) {
        final float roll = random.nextFloat(100.0f);

        if (roll <= config.getWoundChance()) {
            spawnAbilityParticles(player);

            FMessage.printNeptunesFuryImpale(player, config.getWoundTicks());

            if (attackedEntity instanceof final Player attackedPlayer) {
                FMessage.printNeptunesFuryImpaleVictim(attackedPlayer, config.getWoundDamagePerTick(), config.getWoundTicks());
            }

            for (int i = 0; i < config.getWoundTicks(); i++) {
                new Scheduler(plugin).sync(() -> {
                    final double pre = attackedEntity.getHealth();
                    final double post = pre - config.getWoundDamagePerTick();

                    if (post <= 0.5) {
                        return;
                    }

                    attackedEntity.setHealth(post);
                    attackedEntity.damage(0.0, player);
                }).delay(i*10L).run();
            }
        }
    }
}

package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.cx.modules.player.combat.EnchantLimitModule;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.EMythicAbilityType;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.items.mythic.MythicAbility;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.utils.StringUtil;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.utils.Players;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public final class CrimsonFang implements IMythicItem {
    public record CrimsonFangConfig(@Getter double healPercent,
                                    @Getter int killRegenAmplifier,
                                    @Getter int killRegenDuration) {}

    public static final TextColor CRIMSON_FANG_COLOR = NamedTextColor.DARK_RED;

    @Getter public final Factions plugin;
    @Getter public final List<MythicAbility> abilityInfo;
    private final CrimsonFangConfig config;

    public CrimsonFang(Factions plugin, CrimsonFangConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.abilityInfo = Lists.newArrayList();

        final int percentDisplay = (int)Math.round(config.getHealPercent() * 100);

        addAbilityInfo(
                ChatColor.RED + "Bloodthirsty",
                "Heal " + percentDisplay + "% of the damage inflicted to monsters.",
                EMythicAbilityType.ON_HIT
        );

        addAbilityInfo(
                ChatColor.BLUE + "Immortality",
                "Gain Regeneration " + StringUtil.getRomanNumeral(config.killRegenAmplifier + 1)
                + " for " + config.killRegenDuration + " seconds upon slaying an enemy.",
                EMythicAbilityType.ON_KILL
        );
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_SWORD;
    }

    @Override
    public Map.Entry<NamespacedKey, String> getIdentifier() {
        return Map.entry(plugin.getNamespacedKey(), "CrimsonFang");
    }

    @Override
    public List<String> getLore() {
        return List.of();
    }

    @Override
    public List<Component> getLoreComponents() {
        return getMythicLore();
    }

    @Override
    public String getMythicName() {
        return "Crimson Fang";
    }

    @Override
    public TextColor getColor() {
        return CRIMSON_FANG_COLOR;
    }

    @Override
    public Particle getAbilityParticle() {
        return Particle.DAMAGE_INDICATOR;
    }

    @Override
    public double getAbilityParticleSpeed() {
        return 0.01;
    }

    @Override
    public int getAbilityParticleCount() {
        return 8;
    }

    @Override
    public double getAbilityParticleOffset() {
        return 0.4;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final CXService cxService = (CXService)plugin.getService(CXService.class);
        final EnchantLimitModule enchantLimitModule = cxService.getEnchantLimitModule();
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        int sharpnessLevel = 5;

        if (enchantLimitModule.getEnchantLimits().containsKey(Enchantment.SHARPNESS)) {
            sharpnessLevel = enchantLimitModule.getMaxEnchantmentLevel(Enchantment.SHARPNESS);
        }

        enchantments.put(Enchantment.SHARPNESS, sharpnessLevel);

        return enchantments;
    }

    @Override
    public void onAttack(Player player, LivingEntity attackedEntity) {
        if (attackedEntity instanceof Player) {
            return;
        }

        final double pre = attackedEntity.getHealth();

        new Scheduler(plugin).sync(() -> {
            final double post = attackedEntity.getHealth();
            final double diff = pre - post;
            final double healAmount = diff * config.getHealPercent();
            final double newHealth = Math.min(player.getHealth() + healAmount, 20.0);

            if (diff > 0.0) {
                player.setHealth(newHealth);
            }
        }).delay(1L).run();
    }

    @Override
    public void onKill(Player player, LivingEntity slainEntity) {
        if (!(slainEntity instanceof Player)) {
            return;
        }

        spawnAbilityParticles(player);
        FMessage.printCrimsonFangKill(player, config.getKillRegenDuration());
        Players.giveTemporaryEffect(plugin, player, new PotionEffect(PotionEffectType.REGENERATION, config.getKillRegenDuration()*20, config.getKillRegenAmplifier()));
    }
}

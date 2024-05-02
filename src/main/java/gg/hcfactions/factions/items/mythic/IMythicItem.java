package gg.hcfactions.factions.items.mythic;

import com.google.common.collect.Lists;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.cx.modules.player.combat.EnchantLimitModule;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.utils.StringUtil;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public interface IMythicItem extends ICustomItem {
    Factions getPlugin();
    List<MythicAbility> getAbilityInfo();
    String getMythicName();
    TextColor getColor();

    /**
     * @return Particle to be displayed
     */
    default Particle getAbilityParticle() {
        return Particle.HEART;
    }

    /**
     * @return Particle speed
     */
    default double getAbilityParticleSpeed() {
        return 1;
    }

    /**
     * @return Particle offset (more = wider range)
     */
    default double getAbilityParticleOffset() {
        return 0.5;
    }

    /**
     * @return Amount of particles to spawn per tick
     */
    default int getAbilityParticleCount() {
        return 16;
    }

    /**
     * @return Total frames to tick for (60 = 3 seconds)
     */
    default int getAbilityParticleFrames() {
        return 60;
    }

    /**
     * @return Rate (per frames) to spawn particles
     */
    default int getAbilityParticleRate() {
        return 5;
    }

    /**
     * @return If true onAttack and onKill will trigger on friendlies
     */
    default boolean isFriendlyFireEnabled() {
        return false;
    }

    /**
     * Queries mythic abilities by provided ability type
     * @param type Ability Type
     * @return List of MythicAbility
     */
    default List<MythicAbility> getAbilityInfoByType(EMythicAbilityType type) {
        return getAbilityInfo().stream().filter(ability -> ability.getAbilityType().equals(type)).collect(Collectors.toList());
    }

    /**
     * Add a new mythic ability info
     * @param name Name of the ability
     * @param description Description of the ability
     * @param type Type of the ability
     */
    default void addAbilityInfo(String name, String description, EMythicAbilityType type) {
        getAbilityInfo().add(new MythicAbility(name, description, type));
    }

    /**
     * @return Mythic Lore Components
     */
    default List<Component> getMythicLore() {
        List<Component> res = Lists.newArrayList();
        res.add(FMessage.getMythicIdentifier(this));

        for (EMythicAbilityType abilityType : EMythicAbilityType.values()) {
            final List<MythicAbility> abilities = getAbilityInfoByType(abilityType);

            if (abilities.isEmpty()) {
                continue;
            }

            res.add(Component.text(" "));
            res.add(LegacyComponentSerializer.legacySection().deserialize(abilityType.getDisplayName()));

            abilities.forEach(ability -> {
                res.add(LegacyComponentSerializer.legacySection().deserialize(ability.getName()));
                StringUtil.formatLoreComponents(res, ability.getDescription(), FMessage.TC_MYTHIC_ABILITY_DESC);
            });
        }

        return res;
    }

    /**
     * Spawn ability particles
     * @param player Player to spawn them at
     */
    default void spawnAbilityParticles(Player player) {
        for (int i = 0; i < getAbilityParticleFrames(); i++) {

            // every other tick
            if (i % getAbilityParticleRate() == 0) {
                new Scheduler(getPlugin()).sync(() -> {
                    player.getWorld().spawnParticle(
                            getAbilityParticle(),
                            player.getLocation().getX(),
                            player.getLocation().getY() + 1.5,
                            player.getLocation().getZ(),
                            getAbilityParticleCount(),
                            getAbilityParticleOffset(), getAbilityParticleOffset(), getAbilityParticleOffset(),
                            getAbilityParticleSpeed()
                    );
                }).delay(i).run();
            }
        }
    }

    /**
     * Utility command to quickly grab the highest sharpness level on the map
     * @return Max sharpness level that can be applied
     */
    default int getMaxSharpness() {
        final CXService cxService = (CXService)getPlugin().getService(CXService.class);
        final EnchantLimitModule enchantLimitModule = cxService.getEnchantLimitModule();
        final int sharpnessLimit = enchantLimitModule.getMaxEnchantmentLevel(Enchantment.SHARPNESS);

        if (sharpnessLimit <= -1) {
            return 5;
        }

        return sharpnessLimit;
    }

    @Override
    default Component getDisplayNameComponent() {
        return Component.text(getMythicName()).color(getColor());
    }

    @Override
    default boolean isSoulbound() {
        return false;
    }

    @Override
    default boolean isRepairable() {
        return false;
    }

    default void onKill(Player player, LivingEntity slainEntity) {}
    default void onAttack(Player player, LivingEntity attackedEntity) {}
    default void onShoot(Player player, LivingEntity attackedEntity) {}
}

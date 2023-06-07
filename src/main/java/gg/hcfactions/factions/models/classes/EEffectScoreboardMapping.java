package gg.hcfactions.factions.models.classes;

import gg.hcfactions.libs.bukkit.remap.ERemappedEffect;
import gg.hcfactions.libs.bukkit.utils.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public enum EEffectScoreboardMapping {
    SPEED(ERemappedEffect.SPEED, 30, Colors.LIGHT_BLUE.toBukkit()),
    SLOWNESS(ERemappedEffect.SLOWNESS, 31, Colors.LAVENDAR.toBukkit()),
    STRENGTH(ERemappedEffect.STRENGTH, 32, Colors.RED.toBukkit()),
    HASTE(ERemappedEffect.HASTE, 32, Colors.LIGHT_YELLOW.toBukkit()),
    MINING_FATIGUE(ERemappedEffect.MINING_FATIGUE, 33, Colors.LAVENDAR.toBukkit()),
    JUMP_BOOST(ERemappedEffect.JUMP_BOOST, 34, Colors.LIME_GREEN.toBukkit()),
    REGENERATION(ERemappedEffect.REGENERATION, 35, Colors.PINK.toBukkit()),
    RESISTANCE(ERemappedEffect.DAMAGE_RESISTANCE, 36, Colors.LAVENDAR.toBukkit()),
    FIRE_RESISTANCE(ERemappedEffect.FIRE_RESISTANCE, 37, Colors.GOLD.toBukkit()),
    WATER_BREATHING(ERemappedEffect.WATER_BREATHING, 38, Colors.DARK_BLUE.toBukkit()),
    INVISIBILITY(ERemappedEffect.INVISIBILITY, 39, Colors.LAVENDAR.toBukkit()),
    BLINDNESS(ERemappedEffect.BLINDNESS, 40, Colors.LAVENDAR.toBukkit()),
    HUNGER(ERemappedEffect.HUNGER, 41, Colors.DARK_GREEN.toBukkit()),
    WEAKNESS(ERemappedEffect.WEAKNESS, 42, Colors.LAVENDAR.toBukkit()),
    POISON(ERemappedEffect.POISON, 43, Colors.LIME_GREEN.toBukkit()),
    WITHER(ERemappedEffect.WITHER, 44, ChatColor.DARK_GRAY),
    SATURATION(ERemappedEffect.SATURATION, 45, Colors.LIGHT_RED.toBukkit()),
    LEVITATION(ERemappedEffect.LEVITATION, 46, Colors.LAVENDAR.toBukkit()),
    SLOW_FALLING(ERemappedEffect.SLOW_FALLING, 47, Colors.LAVENDAR.toBukkit()),
    CONDUIT(ERemappedEffect.CONDUIT, 48, Colors.LIGHT_BLUE.toBukkit()),
    DOLPHINS_GRACE(ERemappedEffect.DOLPHINS_GRACE, 49, Colors.LIGHT_BLUE.toBukkit()),
    DARKNESS(ERemappedEffect.DARKNESS, 50, ChatColor.DARK_GRAY);

    @Getter public final ERemappedEffect remappedEffect;
    @Getter public final int scoreboardPosition;
    @Getter public final ChatColor color;

    public static EEffectScoreboardMapping getByRemappedEffect(ERemappedEffect eff) {
        for (EEffectScoreboardMapping value : values()) {
            if (value.getRemappedEffect().equals(eff)) {
                return value;
            }
        }

        return null;
    }
}

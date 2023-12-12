package gg.hcfactions.factions.models.classes;

import gg.hcfactions.libs.bukkit.remap.ERemappedEffect;
import gg.hcfactions.libs.bukkit.utils.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public enum EEffectScoreboardMapping {
    SPEED(ERemappedEffect.SPEED, 33, Colors.LIGHT_BLUE.toBukkit()),
    SLOWNESS(ERemappedEffect.SLOWNESS, 34, Colors.LAVENDAR.toBukkit()),
    STRENGTH(ERemappedEffect.STRENGTH, 35, Colors.RED.toBukkit()),
    HASTE(ERemappedEffect.HASTE, 36, Colors.LIGHT_YELLOW.toBukkit()),
    MINING_FATIGUE(ERemappedEffect.MINING_FATIGUE, 37, Colors.LAVENDAR.toBukkit()),
    JUMP_BOOST(ERemappedEffect.JUMP_BOOST, 38, Colors.LIME_GREEN.toBukkit()),
    REGENERATION(ERemappedEffect.REGENERATION, 39, Colors.PINK.toBukkit()),
    RESISTANCE(ERemappedEffect.DAMAGE_RESISTANCE, 40, Colors.LAVENDAR.toBukkit()),
    FIRE_RESISTANCE(ERemappedEffect.FIRE_RESISTANCE, 41, Colors.GOLD.toBukkit()),
    WATER_BREATHING(ERemappedEffect.WATER_BREATHING, 42, Colors.DARK_BLUE.toBukkit()),
    INVISIBILITY(ERemappedEffect.INVISIBILITY, 43, Colors.LAVENDAR.toBukkit()),
    BLINDNESS(ERemappedEffect.BLINDNESS, 44, Colors.LAVENDAR.toBukkit()),
    HUNGER(ERemappedEffect.HUNGER, 45, Colors.DARK_GREEN.toBukkit()),
    WEAKNESS(ERemappedEffect.WEAKNESS, 46, Colors.LAVENDAR.toBukkit()),
    POISON(ERemappedEffect.POISON, 47, Colors.LIME_GREEN.toBukkit()),
    WITHER(ERemappedEffect.WITHER, 48, ChatColor.DARK_GRAY),
    SATURATION(ERemappedEffect.SATURATION, 49, Colors.LIGHT_RED.toBukkit()),
    LEVITATION(ERemappedEffect.LEVITATION, 50, Colors.LAVENDAR.toBukkit()),
    SLOW_FALLING(ERemappedEffect.SLOW_FALLING, 51, Colors.LAVENDAR.toBukkit()),
    CONDUIT(ERemappedEffect.CONDUIT, 52, Colors.LIGHT_BLUE.toBukkit()),
    DOLPHINS_GRACE(ERemappedEffect.DOLPHINS_GRACE, 53, Colors.LIGHT_BLUE.toBukkit()),
    DARKNESS(ERemappedEffect.DARKNESS, 54, ChatColor.DARK_GRAY);

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

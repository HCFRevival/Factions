package gg.hcfactions.factions.models.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.potion.PotionEffectType;

@Getter
@AllArgsConstructor
public enum EEffectScoreboardMapping {
    SPEED(PotionEffectType.SPEED, 33),
    SLOWNESS(PotionEffectType.SLOWNESS, 34),
    STRENGTH(PotionEffectType.STRENGTH, 35),
    HASTE(PotionEffectType.HASTE, 36),
    MINING_FATIGUE(PotionEffectType.MINING_FATIGUE, 37),
    JUMP_BOOST(PotionEffectType.JUMP_BOOST, 38),
    REGENERATION(PotionEffectType.REGENERATION, 39),
    RESISTANCE(PotionEffectType.RESISTANCE, 40),
    FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, 41),
    WATER_BREATHING(PotionEffectType.WATER_BREATHING, 42),
    INVISIBILITY(PotionEffectType.INVISIBILITY, 43),
    BLINDNESS(PotionEffectType.BLINDNESS, 44),
    HUNGER(PotionEffectType.HUNGER, 45),
    WEAKNESS(PotionEffectType.WEAKNESS, 46),
    POISON(PotionEffectType.POISON, 47),
    WITHER(PotionEffectType.WITHER, 48),
    SATURATION(PotionEffectType.SATURATION, 49),
    LEVITATION(PotionEffectType.LEVITATION, 50),
    SLOW_FALLING(PotionEffectType.SLOW_FALLING, 51),
    CONDUIT(PotionEffectType.CONDUIT_POWER, 52),
    DOLPHINS_GRACE(PotionEffectType.DOLPHINS_GRACE, 53),
    DARKNESS(PotionEffectType.DARKNESS, 54);

    public final PotionEffectType effectType;
    public final int scoreboardPosition;

    public static ChatColor getColor(EEffectScoreboardMapping mapping) {
        return ChatColor.of(String.format("#%02x%02x%02x",
                mapping.getEffectType().getColor().getRed(),
                mapping.getEffectType().getColor().getGreen(),
                mapping.getEffectType().getColor().getBlue())
        );
    }

    public static EEffectScoreboardMapping getByEffect(PotionEffectType eff) {
        for (EEffectScoreboardMapping value : values()) {
            if (value.getEffectType().equals(eff)) {
                return value;
            }
        }

        return null;
    }
}

package gg.hcfactions.factions.models.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffectType;

@Getter
@AllArgsConstructor
public enum EEffectScoreboardMapping {
    SPEED(PotionEffectType.SPEED, 33, ChatColor.AQUA),
    SLOWNESS(PotionEffectType.SLOWNESS, 34, ChatColor.DARK_GRAY),
    STRENGTH(PotionEffectType.STRENGTH, 35, ChatColor.RED),
    HASTE(PotionEffectType.HASTE, 36, ChatColor.YELLOW),
    MINING_FATIGUE(PotionEffectType.MINING_FATIGUE, 37, ChatColor.GRAY),
    JUMP_BOOST(PotionEffectType.JUMP_BOOST, 38, ChatColor.GREEN),
    REGENERATION(PotionEffectType.REGENERATION, 39, ChatColor.LIGHT_PURPLE),
    RESISTANCE(PotionEffectType.RESISTANCE, 40, ChatColor.YELLOW),
    FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, 41, ChatColor.GOLD),
    WATER_BREATHING(PotionEffectType.WATER_BREATHING, 42, ChatColor.BLUE),
    INVISIBILITY(PotionEffectType.INVISIBILITY, 43, ChatColor.GRAY),
    BLINDNESS(PotionEffectType.BLINDNESS, 44, ChatColor.DARK_GRAY),
    HUNGER(PotionEffectType.HUNGER, 45, ChatColor.RED),
    WEAKNESS(PotionEffectType.WEAKNESS, 46, ChatColor.GRAY),
    POISON(PotionEffectType.POISON, 47, ChatColor.GREEN),
    WITHER(PotionEffectType.WITHER, 48, ChatColor.DARK_GRAY),
    SATURATION(PotionEffectType.SATURATION, 49, ChatColor.RED),
    LEVITATION(PotionEffectType.LEVITATION, 50, ChatColor.DARK_PURPLE),
    SLOW_FALLING(PotionEffectType.SLOW_FALLING, 51, ChatColor.GRAY),
    CONDUIT(PotionEffectType.CONDUIT_POWER, 52, ChatColor.DARK_AQUA),
    DOLPHINS_GRACE(PotionEffectType.DOLPHINS_GRACE, 53, ChatColor.DARK_AQUA),
    DARKNESS(PotionEffectType.DARKNESS, 54, ChatColor.DARK_GRAY);

    public final PotionEffectType effectType;
    public final int scoreboardPosition;
    public final ChatColor color;

    public static EEffectScoreboardMapping getByEffect(PotionEffectType eff) {
        for (EEffectScoreboardMapping value : values()) {
            if (value.getEffectType().equals(eff)) {
                return value;
            }
        }

        return null;
    }
}

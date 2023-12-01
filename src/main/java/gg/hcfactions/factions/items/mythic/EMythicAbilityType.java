package gg.hcfactions.factions.items.mythic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum EMythicAbilityType {
    ON_KILL(ChatColor.RED + "On Kill"),
    ON_HIT(ChatColor.DARK_AQUA + "On Attack");

    @Getter public final String displayName;
}

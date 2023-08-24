package gg.hcfactions.factions.items.horn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum EBattleHornType {
    BERSERK(ChatColor.RED + "Berserk"),
    RETREAT(ChatColor.DARK_RED + "Retreat"),
    CHARGE(ChatColor.GREEN + "Charge"),
    FRENZY(ChatColor.GOLD + "Frenzy"),
    CLEANSE(ChatColor.AQUA + "Cleanse");

    @Getter public final String displayName;
}

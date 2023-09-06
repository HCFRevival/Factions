package gg.hcfactions.factions.models.battlepass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum EBPState {
    WEEKLY(ChatColor.RED + "" + ChatColor.BOLD + "** WEEKLY **"),
    DAILY(ChatColor.YELLOW + "" + ChatColor.BOLD + "** DAILY **"),
    INACTIVE(ChatColor.GRAY + "[Inactive]");

    @Getter public final String displayName;
}

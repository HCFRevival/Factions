package gg.hcfactions.factions.models.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@AllArgsConstructor
public enum EScoreboardEntryType {
    FRIENDLY("friendly", ChatColor.DARK_GREEN, true, true),
    FOCUS("focus", ChatColor.LIGHT_PURPLE, false, false);

    @Getter public final String scoreboardTeamName;
    @Getter public final ChatColor color;
    @Getter public final boolean canAlwaysSeeNametag;
    @Getter public final boolean canSeeInvisibles;
}

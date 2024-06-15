package gg.hcfactions.factions.models.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
@AllArgsConstructor
public enum EScoreboardEntryType {
    FRIENDLY("friendly", NamedTextColor.DARK_GREEN, true, true, false),
    ALLY("ally", NamedTextColor.BLUE, false, false, false),
    FOCUS("focus",  NamedTextColor.LIGHT_PURPLE, false, false, false),
    OBFUSCATED("obfuscated", NamedTextColor.WHITE, false, true, true);

    public final String scoreboardTeamName;
    public final NamedTextColor color;
    public final boolean canAlwaysSeeNametag;
    public final boolean canSeeInvisibles;
    public final boolean obfuscated;
}

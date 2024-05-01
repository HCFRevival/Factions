package gg.hcfactions.factions.models.scoreboard;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.player.EScoreboardEntryType;
import gg.hcfactions.libs.bukkit.scoreboard.AresScoreboard;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

@Getter
public final class FScoreboard extends AresScoreboard {
    public Factions plugin;

    public FScoreboard(Factions plugin, Player player, String title) {
        super(plugin, player, title);
        this.plugin = plugin;

        for (EScoreboardEntryType entryType : EScoreboardEntryType.values()) {
            final Team team = getInternal().registerNewTeam(entryType.getScoreboardTeamName());
            team.color(entryType.getColor());

            if (entryType.canAlwaysSeeNametag) {
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            }

            if (entryType.canSeeInvisibles) {
                team.setCanSeeFriendlyInvisibles(true);
            }
        }
    }
}

package gg.hcfactions.factions.models.scoreboard;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.player.EScoreboardEntryType;
import gg.hcfactions.libs.bukkit.scoreboard.AresScoreboard;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public final class FScoreboard extends AresScoreboard {
    @Getter public Factions plugin;

    public FScoreboard(Factions plugin, Player player, String title) {
        super(plugin, player, title);
        this.plugin = plugin;

        for (EScoreboardEntryType entryType : EScoreboardEntryType.values()) {
            final Team team = getInternal().registerNewTeam(entryType.getScoreboardTeamName());
            team.setColor(entryType.getColor());

            if (entryType.canAlwaysSeeNametag) {
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            }

            if (entryType.canSeeInvisibles) {
                team.setCanSeeFriendlyInvisibles(true);
            }
        }
    }

    /**
     * Add a player to the faction member entries
     * @param player Player
     */
    public void addFactionMember(Player player) {
        Objects.requireNonNull(getInternal().getTeam(EScoreboardEntryType.FRIENDLY.getScoreboardTeamName())).addEntry(player.getName());
    }

    /**
     * Remove a player from the faction member entries
     * @param player Player
     */
    public void removeFactionMember(Player player) {
        Objects.requireNonNull(getInternal().getTeam(EScoreboardEntryType.FRIENDLY.getScoreboardTeamName())).removeEntry(player.getName());
    }

    /**
     * Add a player to the scoreboard
     * @param player Player
     * @param entryType Scoreboard Entry Type
     */
    public void addPlayer(Player player, EScoreboardEntryType entryType) {
        Objects.requireNonNull(getInternal().getTeam(entryType.getScoreboardTeamName())).addEntry(player.getName());
    }

    /**
     * Remvoe a player from the scoreboard
     * @param player Player
     * @param entryType Scoreboard Entry Type
     */
    public void removePlayer(Player player, EScoreboardEntryType entryType) {
        Objects.requireNonNull(getInternal().getTeam(entryType.getScoreboardTeamName())).removeEntry(player.getName());
    }

    /**
     * Clear all entries in the faction member team
     */
    public void removeAllFactionMembers() {
        final Team friendly = getInternal().getTeam(EScoreboardEntryType.FRIENDLY.getScoreboardTeamName());

        if (friendly == null) {
            return;
        }

        friendly.getEntries().forEach(friendly::removeEntry);
    }

    /**
     * Clear all entries for the provided entry type
     * @param type Scoreboard Entry Type
     */
    public void removeAll(EScoreboardEntryType type) {
        final Team team = getInternal().getTeam(type.getScoreboardTeamName());

        if (team == null) {
            return;
        }

        team.getEntries().forEach(team::removeEntry);
    }
}

package gg.hcfactions.factions.models.scoreboard;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.bukkit.scoreboard.AresScoreboard;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

public final class FScoreboard extends AresScoreboard {
    @Getter public Factions plugin;

    public FScoreboard(Factions plugin, Player player, String title) {
        super(plugin, player, title);
        this.plugin = plugin;

        final Team friendly = getInternal().registerNewTeam("friendly");
        friendly.setColor(ChatColor.DARK_GREEN);
        friendly.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        friendly.setCanSeeFriendlyInvisibles(true);
    }

    /**
     * Add a player to the faction member entries
     * @param player Player
     */
    public void addFactionMember(Player player) {
        Objects.requireNonNull(getInternal().getTeam("friendly")).addEntry(player.getName());
    }

    /**
     * Remove a player from the faction member entries
     * @param player Player
     */
    public void removeFactionMember(Player player) {
        Objects.requireNonNull(getInternal().getTeam("friendly")).removeEntry(player.getName());
    }

    /**
     * Clear all entries in the faction member team
     */
    public void removeAllFactionMembers() {
        final Team friendly = getInternal().getTeam("friendly");

        if (friendly == null) {
            return;
        }

        friendly.getEntries().forEach(friendly::removeEntry);
    }
}

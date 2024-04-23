package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.boss.impl.BossGiant;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.CommandPermission;
import gg.hcfactions.libs.acf.annotation.Subcommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("debug")
public final class DebugCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("tracker")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onDebug(Player player) {
        plugin.getEventManager().getActiveEvents().forEach(event -> {
            if (event instanceof final KOTHEvent kothEvent) {
                player.sendMessage(ChatColor.BLUE + "Event Tracker Dump Output for " + kothEvent.getDisplayName());
                player.sendMessage("Participants: " + kothEvent.getSession().getTracker().getParticipants().size());
                player.sendMessage("Global Entries: " + kothEvent.getSession().getTracker().getEntries().size());
            }
        });
    }
}

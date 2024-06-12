package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.boss.impl.BossGiant;
import gg.hcfactions.factions.models.boss.impl.BossPhantom;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.CommandPermission;
import gg.hcfactions.libs.acf.annotation.Subcommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    @Subcommand("boss")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onDebugBossMob(Player player, String bossName) {
        if (bossName.equalsIgnoreCase("giant")) {
            final BossGiant giant = new BossGiant(plugin, player.getLocation());
            giant.spawn();
        } else if (bossName.equalsIgnoreCase("phantom")) {
            final BossPhantom phantom = new BossPhantom(plugin, player.getLocation());
            phantom.spawn();
        }

        player.sendMessage(Component.text("Boss has been spawned", NamedTextColor.GREEN));
    }

    @Subcommand("subclaim")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onDebugSubclaims(Player player) {
        if (plugin.getSubclaimManager().getDebuggingPlayers().remove(player.getUniqueId())) {
            player.sendMessage(Component.text("You are no longer debugging subclaims", NamedTextColor.AQUA));
            return;
        }

        plugin.getSubclaimManager().getDebuggingPlayers().add(player.getUniqueId());
        player.sendMessage(Component.text("You are now debugging subclaims", NamedTextColor.AQUA));
    }
}

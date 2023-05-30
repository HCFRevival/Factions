package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.displays.IDisplayable;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Locale;

@AllArgsConstructor
@CommandAlias("display")
public final class DisplayCommand extends BaseCommand {
    @Getter public Factions plugin;

    @Subcommand("create lb")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<type>")
    @Description("Create a new leaderboard display at your current position")
    @CommandCompletion("@stattypes")
    public void onCreateLeaderboard(Player player, String statTypeName) {
        EStatisticType type;
        try {
            type = EStatisticType.valueOf(statTypeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid statistic type");
            return;
        }

        plugin.getDisplayManager().getExecutor().createLeaderboardDisplay(player, type);
    }

    @Subcommand("forcecheck lb")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Description("Force an update on all leaderboard displays")
    public void onForceCheckLeaderboards(Player player) {
        plugin.getDisplayManager().getDisplayRepository().forEach(IDisplayable::update);
        player.sendMessage(ChatColor.GREEN + "Force-check complete");
    }

    @Subcommand("delete")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<radius>")
    @Description("Delete all displays within the provided radius of your position")
    public void onDeleteDisplay(Player player, String radiusName) {
        double d;
        try {
            d = Double.parseDouble(radiusName);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid radius");
            return;
        }

        plugin.getDisplayManager().getExecutor().deleteDisplay(player, d, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Displays have been deleted");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to delete displays: " + s);
            }
        });
    }
}

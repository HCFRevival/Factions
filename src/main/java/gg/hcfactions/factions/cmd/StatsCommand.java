package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("stats|stat")
public final class StatsCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @CommandAlias("stats|stat")
    @Description("View your stats")
    public void onViewOwnStats(Player player) {
        plugin.getStatsManager().getExecutor().openPlayerStats(player, player.getName(), new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to open stats: " + s);
            }
        });
    }

    @Subcommand("player|p")
    @Description("View player stats")
    @Syntax("[player]")
    public void onViewPlayerStats(Player player, @Optional String name) {
        plugin.getStatsManager().getExecutor().openPlayerStats(player, (name == null ? player.getName() : name), new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to open stats: " + s);
            }
        });
    }

    @Subcommand("faction|f")
    @Description("View faction stats")
    @Syntax("[faction]")
    public void onViewFactionStats(Player player, @Optional String name) {
        final PlayerFaction ownFaction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (name == null) {
            if (ownFaction == null) {
                player.sendMessage(ChatColor.RED + FError.P_NOT_IN_FAC.getErrorDescription());
                return;
            }

            plugin.getStatsManager().getExecutor().openFactionStats(player, ownFaction.getName(), new Promise() {
                @Override
                public void resolve() {}

                @Override
                public void reject(String s) {
                    player.sendMessage(ChatColor.RED + "Failed to open stats: " + s);
                }
            });

            return;
        }

        plugin.getStatsManager().getExecutor().openFactionStats(player, name, new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to open stats: " + s);
            }
        });
    }
}

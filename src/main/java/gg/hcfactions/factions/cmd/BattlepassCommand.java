package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.menus.BattlepassMenu;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("battlepass|bp|pass")
public final class BattlepassCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Default
    @Description("View active objectives in the Battlepass")
    public void onDefaultCommand(Player player) {
        if (!plugin.getBattlepassManager().isEnabled()) {
            player.sendMessage(ChatColor.RED + "Battlepass is not active");
            return;
        }

        new BattlepassMenu(plugin, player).open();
    }

    @Subcommand("reset")
    @Description("Reset all current Battlepass Objectives and acquire new ones")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("[daily|weekly|all]")
    public void onReset(CommandSender sender, @Values("daily|weekly|all") String valueName) {
        if (valueName.equalsIgnoreCase("daily") || valueName.equalsIgnoreCase("all")) {
            plugin.getBattlepassManager().getNewObjectives(false);
            plugin.getBattlepassManager().resetTrackers(false);
            sender.sendMessage(ChatColor.GREEN + "Daily challenges have been reset");
            return;
        }

        if (valueName.equalsIgnoreCase("weekly") || valueName.equalsIgnoreCase("all")) {
            plugin.getBattlepassManager().getNewObjectives(true);
            plugin.getBattlepassManager().resetTrackers(true);
            sender.sendMessage(ChatColor.GREEN + "Weekly challenges have been reset");
        }
    }

    @Subcommand("enable")
    @Description("Enable the Battlepass")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onEnable(CommandSender sender) {
        if (plugin.getBattlepassManager().isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Battlepass is already active");
            return;
        }

        plugin.getBattlepassManager().setEnabled(true);
    }

    @Subcommand("disable")
    @Description("Disable the Battlepass")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onDisable(CommandSender sender) {
        if (!plugin.getBattlepassManager().isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Battlepass is already inactive");
            return;
        }

        plugin.getBattlepassManager().setEnabled(false);
    }

    @Subcommand("reload")
    @Description("Reload all Battlepass Objectives from file")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onReload(CommandSender sender) {
        plugin.getBattlepassManager().getDailyObjectiveRepository().clear();
        plugin.getBattlepassManager().getWeeklyObjectiveRepository().clear();

        plugin.getBattlepassManager().loadConfiguration();
        plugin.getBattlepassManager().loadObjectives(true);
        plugin.getBattlepassManager().loadObjectives(false);

        sender.sendMessage(ChatColor.GREEN + "Battlepass Reloaded");
    }
}

package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.menus.BattlepassMenu;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("battlepass|bp|pass")
public final class BattlepassCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Default
    @Description("View active objectives in the Battlepass")
    public void onDefaultCommand(Player player) {
        new BattlepassMenu(plugin, player).open();
    }

    @Subcommand("reset")
    @Description("Reset all current Battlepass Objectives and acquire new ones")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onReset(CommandSender sender) {

    }
}

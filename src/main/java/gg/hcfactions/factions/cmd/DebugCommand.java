package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.models.logger.impl.CombatLogger;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.CommandPermission;
import gg.hcfactions.libs.acf.annotation.Subcommand;
import org.bukkit.entity.Player;

@CommandAlias("debug")
public final class DebugCommand extends BaseCommand {
    @Subcommand("cl")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onCombatLoggerSpawn(Player player) {
        final CombatLogger logger = new CombatLogger(player.getLocation(), player, 30);
        logger.spawn();
    }
}

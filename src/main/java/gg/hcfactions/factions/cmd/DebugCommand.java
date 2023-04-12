package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.models.logger.impl.CombatLogger;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.Subcommand;
import org.bukkit.entity.Player;

@CommandAlias("debug")
public class DebugCommand extends BaseCommand {
    @Subcommand("cl")
    public void onCombatLoggerSpawn(Player player) {
        final CombatLogger logger = new CombatLogger(player.getLocation(), player, 30);
        logger.spawn();
    }
}

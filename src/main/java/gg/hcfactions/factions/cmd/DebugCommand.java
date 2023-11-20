package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.boss.impl.BossGiant;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.CommandPermission;
import gg.hcfactions.libs.acf.annotation.Subcommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("debug")
public final class DebugCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("entity")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onDebug(Player player) {
        final BossGiant giant = new BossGiant(plugin, player.getLocation());
        giant.spawn();
    }
}

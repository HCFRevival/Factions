package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.Description;
import gg.hcfactions.libs.acf.annotation.Subcommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("pvp")
public class PvPCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("enable")
    @Description("Enable PvP")
    public void onRemoveProt(Player player) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            player.sendMessage(FMessage.ERROR + FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (!factionPlayer.hasTimer(ETimerType.PROTECTION)) {
            player.sendMessage(FMessage.ERROR + "PvP is already enabled");
            return;
        }

        factionPlayer.finishTimer(ETimerType.PROTECTION);
        player.sendMessage(FMessage.SUCCESS + "PvP has been enabled");
    }
}

package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.Default;
import gg.hcfactions.libs.acf.annotation.Description;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("logout")
@AllArgsConstructor
public final class LogoutCommand extends BaseCommand {
    @Getter public Factions plugin;

    @Default
    @Description("Start a logout timer to be safely disconnected from the server")
    public void onLogout(Player player) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            player.sendMessage(FMessage.ERROR + FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        factionPlayer.addTimer(new FTimer(ETimerType.LOGOUT, plugin.getConfiguration().getLogoutDuration()));
        player.sendMessage(ChatColor.YELLOW + "You will be safely disconnected from the server in " + ChatColor.BLUE + plugin.getConfiguration().getLogoutDuration() + " seconds");
        player.sendMessage(ChatColor.RED + "Warning! Moving or taking damage will cancel this timer");
    }
}

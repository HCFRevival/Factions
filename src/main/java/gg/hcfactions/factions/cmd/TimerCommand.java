package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.base.util.Time;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("timer")
public class TimerCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("give")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Description("Assign a timer to a player")
    @Syntax("<player> <timer> <duration>")
    @CommandCompletion("@players")
    public void onGive(Player player, String playerName, String timerName, String durationName) {
        final Player assignedTo = Bukkit.getPlayer(playerName);

        if (assignedTo == null) {
            player.sendMessage(FMessage.ERROR + FError.P_NOT_FOUND.getErrorDescription());
            return;
        }

        final ETimerType timerType = ETimerType.fromString(timerName);

        if (timerType == null) {
            player.sendMessage(FMessage.ERROR + "Timer not found");
            return;
        }

        final long duration = Time.parseTime(durationName);

        if (duration <= 0) {
            player.sendMessage(FMessage.ERROR + "Invalid duration amount");
            return;
        }

        final FactionPlayer fp = (FactionPlayer) plugin.getPlayerManager().getPlayer(assignedTo);

        if (fp == null) {
            player.sendMessage(FMessage.ERROR + FError.P_NOT_FOUND.getErrorDescription());
            return;
        }

        if (fp.hasTimer(timerType)) {
            if (fp.isPreferScoreboardDisplay()) {
                fp.getScoreboard().removeLine(timerType.getScoreboardPosition());
            }

            fp.removeTimer(timerType);
        }

        final FTimer timer = new FTimer(timerType, duration);
        fp.addTimer(timer);

        player.sendMessage(FMessage.SUCCESS + assignedTo.getName()
                + " has been given a " + timerType.getDisplayName()
                + FMessage.SUCCESS + " timer for "
                + ChatColor.RESET + Time.convertToRemaining(duration));

        assignedTo.sendMessage(FMessage.P_NAME + player.getName()
                + FMessage.LAYER_1 + " assigned a " + timerType.getDisplayName()
                + FMessage.LAYER_1 + " timer for " + ChatColor.RESET
                + Time.convertToRemaining(duration));
    }

    @Subcommand("remove|rem")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Description("Remove a timer from a player")
    @Syntax("<player> <timer>")
    @CommandCompletion("@players")
    public void onRemove(Player player, String playerName, String timerName) {
        final Player assignedTo = Bukkit.getPlayer(playerName);

        if (assignedTo == null) {
            player.sendMessage(FMessage.ERROR + FError.P_NOT_FOUND.getErrorDescription());
            return;
        }

        final ETimerType timerType = ETimerType.fromString(timerName);

        if (timerType == null) {
            player.sendMessage(FMessage.ERROR + "Timer not found");
            return;
        }

        final FactionPlayer fp = (FactionPlayer) plugin.getPlayerManager().getPlayer(assignedTo);

        if (fp == null) {
            player.sendMessage(FMessage.ERROR + FError.P_NOT_FOUND.getErrorDescription());
            return;
        }

        if (!fp.hasTimer(timerType)) {
            player.sendMessage(FMessage.ERROR + "Player does not have this timer");
            return;
        }

        fp.finishTimer(timerType);
    }

    @CommandAlias("togglescoreboard")
    @Description("Toggle UI mode for rendering Timers")
    public void onToggleScoreboard(Player player) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
        if (factionPlayer == null) {
            player.sendMessage(FMessage.ERROR + FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        factionPlayer.setPreferScoreboardDisplay(!factionPlayer.isPreferScoreboardDisplay());

        if (!factionPlayer.isPreferScoreboardDisplay() && !factionPlayer.getScoreboard().isHidden()) {
            factionPlayer.getScoreboard().hide();
        }

        player.sendMessage(FMessage.LAYER_1 + "User interface mode has been set to "
                + ((factionPlayer.isPreferScoreboardDisplay()) ? ChatColor.RED + "Scoreboard" : ChatColor.AQUA + "Hotbar"));
    }
}

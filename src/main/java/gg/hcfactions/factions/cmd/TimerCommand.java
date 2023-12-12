package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.menus.PlayerTimerMenu;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.IFactionPlayer;
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

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@CommandAlias("timer")
public final class TimerCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("give")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Description("Assign a timer to a player")
    @Syntax("<player> <timer> <duration>")
    @CommandCompletion("@players @timers")
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
    @CommandCompletion("@players @timers")
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

    @Subcommand("list")
    @Description("View a list of all players who have a provided timer type")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@timers")
    public void onTimerList(Player player, String timerName) {
        final ETimerType type = ETimerType.fromString(timerName);

        if (type == null) {
            player.sendMessage(ChatColor.RED + "Invalid timer type");
            return;
        }

        final List<IFactionPlayer> initialEntries = plugin.getPlayerManager().getPlayerRepository().stream().filter(fp -> fp.hasTimer(type)).collect(Collectors.toList());

        if (initialEntries.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No entries found");
            return;
        }

        final PlayerTimerMenu menu = new PlayerTimerMenu(plugin, player, type, initialEntries);

        menu.open();
    }

    @CommandAlias("clearcd")
    @Description("Clear all class cooldowns")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onClearCooldowns(Player player, @Optional String username) {
        if (username != null) {
            final Player otherPlayer = Bukkit.getPlayer(username);

            if (otherPlayer == null) {
                player.sendMessage(ChatColor.RED + "Player not found");
                return;
            }

            final IClass playerClass = plugin.getClassManager().getCurrentClass(otherPlayer);

            if (playerClass == null) {
                player.sendMessage(ChatColor.RED + "Player does not have an active class");
                return;
            }

            playerClass.getConsumables().forEach(consumeable -> consumeable.getCooldowns().remove(otherPlayer.getUniqueId()));
            player.sendMessage(ChatColor.GREEN + "Cleared class cooldowns for " + otherPlayer.getName());
            otherPlayer.sendMessage(ChatColor.YELLOW + "Your class cooldowns have been cleared by " + ChatColor.BLUE + player.getName());
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (playerClass == null) {
            player.sendMessage(ChatColor.RED + "You do not have an active class");
            return;
        }

        playerClass.getConsumables().forEach(consumable -> consumable.getCooldowns().remove(player.getUniqueId()));
        player.sendMessage(ChatColor.YELLOW + "Your class cooldowns have been cleared");
    }
}

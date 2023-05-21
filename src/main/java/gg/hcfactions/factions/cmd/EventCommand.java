package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.EPalaceLootTier;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.CommandHelp;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

@CommandAlias("event")
@AllArgsConstructor
public final class EventCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("create")
    @Description("Create an event")
    @Syntax("<name> <koth|palace>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onCreate(Player player, String eventName, @Values("koth|palace") String eventType) {
        plugin.getEventManager().getBuilderManager().getExecutor().buildCaptureEvent(player, eventName, (eventType.equalsIgnoreCase("palace")), new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    /*
    /event start courtyard 20 60
     */
    @Subcommand("start koth")
    @Description("Start a King of the Hill Event")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<name> <tickets> <timer> <tokens>")
    public void onStart(Player player, String eventName, String ticketsToWinNamed, String timerIntervalNamed, String tokenRewardName) {
        int ticketsToWin;
        int timerInterval;
        int tokenReward;

        try {
            ticketsToWin = Integer.parseInt(ticketsToWinNamed);
            timerInterval = Integer.parseInt(timerIntervalNamed);
            tokenReward = Integer.parseInt(tokenRewardName);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid Capture Event Config");
            return;
        }

        plugin.getEventManager().getExecutor().startCaptureEvent(player, eventName, ticketsToWin, timerInterval, tokenReward, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Event started");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to start event: " + s);
            }
        });
    }

    @Subcommand("stop")
    @Description("Stop an event")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<name>")
    public void onStop(Player player, String eventName) {
        plugin.getEventManager().getExecutor().stopEvent(player, eventName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Event stopped");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to stop event: " + s);
            }
        });
    }

    @Subcommand("koth config")
    @Description("Update capture event config for KOTH events")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<event> <tickets> <timer> <tokens>")
    public void onUpdateCaptureConfig(Player player, String eventName, String ticketName, String timerName, String tokenName) {
        int tickets = 0;
        int timerDuration = 0;
        int tokens = 0;

        try {
            tickets = Integer.parseInt(ticketName);
            timerDuration = Integer.parseInt(timerName);
            tokens = Integer.parseInt(tokenName);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid integer amounts");
            return;
        }

        plugin.getEventManager().getExecutor().setCaptureEventConfig(player, eventName, tickets, timerDuration, tokens, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Event configuration updated");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to update event config: " + s);
            }
        });
    }

    @Subcommand("list")
    @Description("View all events and their information")
    public void onList(Player player) {
        plugin.getEventManager().getExecutor().openEventsMenu(player, new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to open menu: " + s);
            }
        });
    }

    @Subcommand("loot palace view")
    @Description("Inspect Palace Loot Table")
    @Syntax("<t1|t2|t3>")
    public void onViewPalaceLoot(Player player, @Values("t1|t2|t3") String tierName) {
        EPalaceLootTier tier;

        try {
            tier = EPalaceLootTier.valueOf(tierName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid loot tier");
            return;
        }

        plugin.getEventManager().getExecutor().openPalaceLootMenu(player, tier, new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to open menu: " + s);
            }
        });
    }

    @Subcommand("loot palace chest add")
    @Description("Add a Palace Chest to a Palace Event")
    @Syntax("<event> <t1|t2|t3>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onAddPalaceChest(Player player, String eventName, @Values("t1|t2|t3") String tierName) {
        EPalaceLootTier tier;

        try {
            tier = EPalaceLootTier.valueOf(tierName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid loot tier");
            return;
        }

        plugin.getEventManager().getExecutor().addPalaceLootChest(player, eventName, tier, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Event chest added");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to add chest: " + s);
            }
        });
    }

    @Subcommand("loot palace add")
    @Description("Add a new item to the Palace Loot Table")
    @Syntax("<t1|t2|t3> <min> <max> <probability>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onAddPalaceLoot(Player player, @Values("t1|t2|t3") String tierName, String minName, String maxName, String probName) {
        EPalaceLootTier tier;
        int minAmount;
        int maxAmount;
        int probability;

        try {
            tier = EPalaceLootTier.valueOf(tierName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid loot tier");
            return;
        }

        try {
            minAmount = Integer.parseInt(minName);
            maxAmount = Integer.parseInt(maxName);
            probability = Integer.parseInt(probName);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid min/max/probability");
            return;
        }

        plugin.getEventManager().getExecutor().addPalaceLoot(player, tier, minAmount, maxAmount, probability, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Item added to Loot Table");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to add item: " + s);
            }
        });
    }

    @Subcommand("restock")
    @Description("Restock the loot chests within an event")
    @Syntax("<event> [-b]")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@events")
    public void onRestock(Player player, String eventName, @Optional String broadcast) {
        final boolean flag = (broadcast != null && broadcast.equalsIgnoreCase("-b"));
        plugin.getEventManager().getExecutor().restockPalaceEvent(player, eventName, flag, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Event has been restocked");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to restock event: " + s);
            }
        });
    }

    @HelpCommand
    public void onCommandHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}

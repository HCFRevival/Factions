package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.EPalaceLootTier;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.CommandHelp;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Locale;

@CommandAlias("event")
@AllArgsConstructor
public final class EventCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("create koth")
    @Description("Create a King of the Hill event")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<name> [palace]")
    public void onCreateKOTH(Player player, String eventName, @Optional String palaceFlag) {
        plugin.getEventManager().getBuilderManager().getExecutor().buildCaptureEvent(
                player,
                eventName,
                (palaceFlag != null && palaceFlag.equalsIgnoreCase("palace")
            ), new Promise() {

            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @Subcommand("create conquest")
    @Description("Create a new Conquest event")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<name> <display name> <server faction>")
    public void onCreateConquest(Player player, String eventName, String displayName, String serverFactionName) {
        plugin.getEventManager().getBuilderManager().getExecutor().buildConquestEvent(
                player,
                eventName,
                displayName,
                serverFactionName,
                new Promise() {
                    @Override
                    public void resolve() {
                        player.sendMessage(ChatColor.GREEN + "Conquest event has been created");
                    }

                    @Override
                    public void reject(String s) {
                        player.sendMessage(ChatColor.RED + "Failed to create Conquest event: " + s);
                    }
                }
        );
    }

    @Subcommand("create zone")
    @Description("Create a new Conquest Zone")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<event> <zone name>")
    public void onCreateConquestZone(Player player, String eventName, String zoneName) {
        plugin.getEventManager().getBuilderManager().getExecutor().buildConquestZone(
                player,
                eventName,
                zoneName,
                new Promise() {
                    @Override
                    public void resolve() {}

                    @Override
                    public void reject(String s) {
                        player.sendMessage(ChatColor.RED + "Failed to create Conquest Zone: " + s);
                    }
                }
        );
    }

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

    @Subcommand("start conquest")
    @Description("Start a Conquest Event")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<name> <tickets> <timer> <tokens> <pertick>")
    public void onStartConquest(Player player, String eventName, int ticketsToWin, int timerDuration, int tokenReward, int ticketsPerTick) {
        plugin.getEventManager().getExecutor().startConquestEvent(player, eventName, ticketsToWin, timerDuration, tokenReward, ticketsPerTick, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Event started");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to start an event: " + s);
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

    @Subcommand("delete event")
    @Description("Delete an event")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<name>")
    @CommandCompletion("@events")
    public void onDelete(Player player, String eventName) {
        plugin.getEventManager().getExecutor().deleteEvent(player, eventName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Event deleted");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to delete event: " + s);
            }
        });
    }

    @Subcommand("delete zone")
    @Description("Delete a Conquest Zone")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<event> <zone>")
    @CommandCompletion("@events")
    public void onDeleteZone(Player player, String eventName, String zoneName) {
        plugin.getEventManager().getExecutor().deleteZone(player, eventName, zoneName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Zone deleted");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to delete zone: " + s);
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

    @Subcommand("loot palace reload")
    @Description("Reload Palace Loot Table")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onPalaceLootReload(CommandSender sender) {
        plugin.getEventManager().getPalaceLootManager().loadTiers();
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
    public void onAddPalaceLoot(Player player, @Values("t1|t2|t3") String tierName, int minAmount, int maxAmount, int probability) {
        EPalaceLootTier tier;

        try {
            tier = EPalaceLootTier.valueOf(tierName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid loot tier");
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

    @Subcommand("schedule")
    @Description("Modify event schedules")
    @Syntax("<event> <add|rem> <day> <hr:min> [temp]")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@events")
    public void onSchedule(
            Player player,
            String eventName,
            @Values("add|rem") String modifier,
            @Values("sunday|monday|tuesday|wednesday|thursday|friday|saturday") String dayOfWeekName,
            String timeName,
            @Optional String temp
    ) {
        final boolean isTemp = (temp != null && temp.equalsIgnoreCase("temp"));
        int dayOfWeek = -1;

        switch (dayOfWeekName.toLowerCase()) {
            case "sunday" -> dayOfWeek = 7;
            case "monday" -> dayOfWeek = 1;
            case "tuesday" -> dayOfWeek = 2;
            case "wednesday" -> dayOfWeek = 3;
            case "thursday" -> dayOfWeek = 4;
            case "friday" -> dayOfWeek = 5;
            case "saturday" -> dayOfWeek = 6;
        }

        if (dayOfWeek == -1) {
            player.sendMessage(FMessage.ERROR + "Invalid day of week");
            return;
        }

        final String[] timeSplit = timeName.split(":");
        int hourOfDay;
        int minuteOfHour;

        if (timeSplit.length != 2) {
            player.sendMessage(ChatColor.RED + "Invalid time format");
            return;
        }

        try {
            hourOfDay = Integer.parseInt(timeSplit[0]);
            minuteOfHour = Integer.parseInt(timeSplit[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid time format");
            return;
        }

        if (hourOfDay < 0 || hourOfDay > 23) {
            player.sendMessage(ChatColor.RED + "Hours must be 0-23");
            return;
        }

        if (minuteOfHour < 0 || minuteOfHour > 59) {
            player.sendMessage(ChatColor.RED + "Minutes must be 0-60");
            return;
        }

        if (modifier.equalsIgnoreCase("add")) {
            plugin.getEventManager().getExecutor().addEventSchedule(player, eventName, dayOfWeek, hourOfDay, minuteOfHour, isTemp, new Promise() {
                @Override
                public void resolve() {
                    player.sendMessage(FMessage.SUCCESS + "Event schedule updated");
                }

                @Override
                public void reject(String s) {
                    player.sendMessage(FMessage.ERROR + "Failed to update event schedule: " + s);
                }
            });

            return;
        }

        plugin.getEventManager().getExecutor().removeEventSchedule(player, eventName, dayOfWeek, hourOfDay, minuteOfHour, isTemp, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(FMessage.SUCCESS + "Event schedule updated");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to update event schedule: " + s);
            }
        });
    }

    @Subcommand("reload")
    @Description("Reload all event data")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onReload(Player player) {
        if (!plugin.getEventManager().getActiveEvents().isEmpty()) {
            player.sendMessage(ChatColor.RED + "Please stop all active events before reloading");
            return;
        }

        plugin.getEventManager().getEventRepository().clear();
        plugin.getEventManager().loadEvents();
    }

    @HelpCommand
    public void onCommandHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}

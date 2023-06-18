package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.claim.EClaimBufferType;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.CommandHelp;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.base.util.Time;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("faction|f|team|t")
public final class FactionCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("create")
    @Description("Create a faction")
    @Syntax("<name>")
    public void onCreatePlayerFaction(Player player, String factionName) {
        plugin.getFactionManager().getExecutor().createPlayerFaction(player, factionName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Your faction has been created");
                FMessage.broadcastFactionCreated(factionName, player.getName());
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to create faction: " + s);
            }
        });
    }

    @Subcommand("cs|createserver")
    @Description("Create a server faction")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<name>")
    public void onCreateServerFaction(Player player, String factionName) {
        plugin.getFactionManager().getExecutor().createServerFaction(player, factionName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Server faction has been created");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to create faction: " + s);
            }
        });
    }

    @Subcommand("invite|inv")
    @Description("Invite a player to your faction")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onInvite(Player player, String username) {
        plugin.getFactionManager().getExecutor().createInvite(player, username, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Invitation sent");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to send invitation: " + s);
            }
        });
    }

    @Subcommand("uninvite|uninv")
    @Description("Revoke an invitation to your faction")
    @Syntax("<name>")
    public void onUninvite(Player player, String username) {
        plugin.getFactionManager().getExecutor().revokeInvite(player, username, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Invitation has been revoked");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to revoke invitation: " + s);
            }
        });
    }

    @Subcommand("join|accept")
    @Description("Join a faction you have been invited to")
    @Syntax("<name>")
    public void onFactionJoin(Player player, String factionName) {
        plugin.getFactionManager().getExecutor().joinFaction(player, factionName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "You have joined the faction");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to join faction: " + s);
            }
        });
    }

    @Subcommand("leave")
    @Description("Leave your faction")
    public void onFactionLeave(Player player) {
        plugin.getFactionManager().getExecutor().leaveFaction(player, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "You have left the faction");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to leave faction: " + s);
            }
        });
    }

    @Subcommand("disband")
    @Description("Disband your faction")
    public void onDisbandFaction(Player player, @Optional String factionName) {
        final Promise promise = new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction has been disbanded");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to disband faction: " + s);
            }
        };

        if (factionName == null) {
            plugin.getFactionManager().getExecutor().disbandFaction(player, promise);
            return;
        }

        plugin.getFactionManager().getExecutor().disbandFaction(player, factionName, promise);
    }

    @Subcommand("rename|setname")
    @Description("Rename your faction")
    @Syntax("<name>")
    public void onRenameFaction(Player player, String newFactionName) {
        plugin.getFactionManager().getExecutor().renameFaction(player, newFactionName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction has been renamed to " + newFactionName);
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to rename faction: " + s);
            }
        });
    }

    @Subcommand("rename|setname")
    @Description("Rename a faction")
    @Syntax("<current> <name>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onRenameFaction(Player player, String currentFactionName, String newFactionName) {
        plugin.getFactionManager().getExecutor().renameFaction(player, currentFactionName, newFactionName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction has been renamed to " + newFactionName);
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to rename faction: " + s);
            }
        });
    }

    @Subcommand("sethome|sethq")
    @Description("Update your faction home to your current location")
    public void onFactionHomeSet(Player player, @Optional String factionName) {
        final Promise promise = new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction home has been updated to your location");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to update faction home: " + s);
            }
        };

        if (factionName != null) {
            plugin.getFactionManager().getExecutor().setFactionHome(player, factionName, promise);
            return;
        }

        plugin.getFactionManager().getExecutor().setFactionHome(player, promise);
    }

    @Subcommand("unsethome")
    @Description("Unset your faction home")
    public void onFactionHomeUnset(Player player, @Optional String factionName) {
        final Promise promise = new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction home has been unset");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to unset faction home: " + s);
            }
        };

        if (factionName != null) {
            plugin.getFactionManager().getExecutor().unsetFactionHome(player, factionName, promise);
            return;
        }

        plugin.getFactionManager().getExecutor().unsetFactionHome(player, promise);
    }

    @Subcommand("show|who")
    @Description("Fetch details about a faction")
    @Syntax("[name]")
    @CommandCompletion("@pfactionsmixed")
    public void onFactionShow(Player player, @Optional String name) {
        if (name != null) {
            plugin.getFactionManager().getExecutor().showFactionInfo(player, name);
            return;
        }

        plugin.getFactionManager().getExecutor().showFactionInfo(player);
    }

    @Subcommand("list")
    @Description("Fetch a list of factions")
    @Syntax("[page]")
    public void onFactionList(Player player, @Optional String page) {
        if (page != null) {
            int pageNumber;

            try {
                pageNumber = Integer.parseInt(page);
            } catch (NumberFormatException e) {
                player.sendMessage(FMessage.ERROR + "Failed to query faction list: invalid page number");
                return;
            }

            plugin.getFactionManager().getExecutor().showFactionList(player, pageNumber);
            return;
        }

        plugin.getFactionManager().getExecutor().showFactionList(player, 1);
    }

    @Subcommand("chat|channel")
    @Description("Update your faction chat channel")
    @Syntax("[public|faction]")
    public void onFactionChat(Player player, @Optional @Values("public|faction") String channelName) {
        PlayerFaction.ChatChannel channel = null;

        if (channelName != null) {
            if (channelName.equalsIgnoreCase("public") || channelName.equalsIgnoreCase("p") || channelName.equalsIgnoreCase("pub")) {
                channel = PlayerFaction.ChatChannel.PUBLIC;
            } else if (channelName.equalsIgnoreCase("faction") || channelName.equalsIgnoreCase("f") || channelName.equalsIgnoreCase("fac")) {
                channel = PlayerFaction.ChatChannel.FACTION;
            } else {
                player.sendMessage(FMessage.ERROR + "Failed to change chat channel: invalid channel name (valid channels: faction, public)");
                return;
            }
        }

        plugin.getFactionManager().getExecutor().setFactionChatChannel(player, channel, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Chat channel has been updated");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to change chat channel: " + s);
            }
        });
    }

    @Subcommand("freeze")
    @Description("Freeze a faction's power regeneration")
    @Syntax("<name> <duration>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@pfactions")
    public void onFactionPowerFreeze(Player player, String factionName, String freezeTime) {
        long parsed;
        try {
            parsed = Time.parseTime(freezeTime);
        } catch (NumberFormatException e) {
            player.sendMessage(FMessage.ERROR + "Failed to freeze faction power: invalid time format");
            return;
        }

        plugin.getFactionManager().getExecutor().freezeFactionPower(player, factionName, parsed, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction power has been frozen");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to freeze faction power: " + s);
            }
        });
    }

    @Subcommand("thaw|unfreeze")
    @Description("Thaw a faction's power regeneration")
    @Syntax("<name>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@pfactions")
    public void onFactionPowerThaw(Player player, String factionName) {
        plugin.getFactionManager().getExecutor().thawFactionPower(player, factionName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction power has been thawed");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to thaw faction power: " + s);
            }
        });
    }

    @Subcommand("setdtr")
    @Description("Update a faction's DTR")
    @Syntax("<name> <dtr>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@pfactions")
    public void onFactionSetDTR(Player player, String factionName, String dtr) {
        double v;
        try {
            v = Double.parseDouble(dtr);
        } catch (NumberFormatException ex) {
            player.sendMessage(FMessage.ERROR + "Failed to set faction DTR: invalid DTR format");
            return;
        }

        plugin.getFactionManager().getExecutor().setFactionDTR(player, factionName, v, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction DTR has been set to " + v);
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to set faction DTR: " + s);
            }
        });
    }

    @Subcommand("setflag|flag")
    @Description("Update a server faction's flag value")
    @Syntax("<name> <flag>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@sfactions")
    public void onFactionFlagSet(Player player, String factionName, String flagName) {
        ServerFaction.Flag flag = ServerFaction.Flag.getFlagByName(flagName);

        if (flag == null) {
            player.sendMessage(FMessage.ERROR + "Failed to set faction flag: invalid flag name");
            return;
        }

        plugin.getFactionManager().getExecutor().setFactionFlag(player, factionName, flag, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction flag has been updated");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to set faction flag: " + s);
            }
        });
    }

    @Subcommand("setdisplay|sd|displayname")
    @Description("Update a server faction's display name")
    @Syntax("<name> <displayname>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@sfactions")
    public void onFactionDisplayNameUpdate(Player player, String factionName, String displayName) {
        plugin.getFactionManager().getExecutor().setFactionDisplayName(player, factionName, displayName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction display name has been updated to: " +
                        ChatColor.translateAlternateColorCodes('&', displayName));
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to set faction display name: " + s);
            }
        });
    }

    @Subcommand("setbuffer|sb")
    @Description("Update a server faction's buffer value")
    @Syntax("<name> <claim|build> <size>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@sfactions")
    public void onFactionBufferUpdate(
            Player player,
            String factionName,
            @Values("claim|build")String bufferTypeName,
            String size
    ) {
        EClaimBufferType bufferType = EClaimBufferType.getBufferTypeByName(bufferTypeName);
        if (bufferType == null) {
            player.sendMessage(FMessage.ERROR + "Failed to set faction buffer value: invalid buffer type (claim/build)");
            return;
        }

        int v;
        try {
            v = Integer.parseInt(size);
        } catch (NumberFormatException e) {
            player.sendMessage(FMessage.ERROR + "Failed to set faction buffer value: invalid buffer size");
            return;
        }

        plugin.getFactionManager().getExecutor().setFactionBuffer(player, factionName, bufferType, v, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction buffer has been updated");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to set faction buffer value: " + s);
            }
        });
    }

    @Subcommand("setinv|setreinv")
    @Description("Update a faction's reinvites")
    @Syntax("<name> <amount>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@pfactions")
    public void onFactionReinviteUpdate(Player player, String factionName, String reinviteAmount) {
        int v;
        try {
            v = Integer.parseInt(reinviteAmount);
        } catch (NumberFormatException e) {
            player.sendMessage(FMessage.ERROR + "Failed to set faction reinvites: invalid number");
            return;
        }

        plugin.getFactionManager().getExecutor().setFactionReinvites(player, factionName, v, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction reinvites has been updated to: " + v);
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to set faction reinvites: " + s);
            }
        });
    }

    @Subcommand("map")
    @Description("View nearby faction claims")
    public void onFactionMap(Player player) {
        plugin.getFactionManager().getExecutor().showFactionMap(player, new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to display map: " + s);
            }
        });
    }

    @Subcommand("claim")
    @Description("Claim land for your faction")
    public void onFactionClaim(Player player, @Optional String factionName) {
        final Promise promise = new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "You have been given a faction claiming stick");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to start claiming: " + s);
            }
        };

        if (factionName == null) {
            plugin.getFactionManager().getExecutor().startClaiming(player, promise);
            return;
        }

        plugin.getFactionManager().getExecutor().startClaiming(player, factionName, promise);
    }

    @Subcommand("unclaim")
    @Description("Unclaim land for your faction")
    public void onFactionUnclaim(Player player, @Optional String factionName) {
        final Promise promise = new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(FMessage.SUCCESS + "Land has been unclaimed");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to unclaim land: " + s);
            }
        };

        if (factionName == null) {
            plugin.getFactionManager().getExecutor().unclaim(player, promise);
            return;
        }

        plugin.getFactionManager().getExecutor().unclaim(player, factionName, promise);
    }

    @Subcommand("subclaim")
    @Description("Subclaim land for your faction")
    @Syntax("<name>")
    public void onFactionSubclaim(Player player, String subclaimName) {
        plugin.getFactionManager().getExecutor().startSubclaiming(player, subclaimName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "You have been given a faction subclaiming axe");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to start subclaiming: " + s);
            }
        });
    }

    @Subcommand("subclaim")
    @Description("Modify access to a subclaim within your faction")
    @Syntax("<add|remove> <subclaim> <player>")
    public void onFactionSubclaimAccess(
            Player player,
            @Values("add|remove|rem") String modifier,
            String subclaimName,
            String username
    ) {
        plugin.getFactionManager().getExecutor().modifySubclaim(player, subclaimName, modifier, username, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Subclaim has been updated");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to modify subclaim: " + s);
            }
        });
    }

    @Subcommand("subclaims|sublist")
    @Description("View a list of your faction's subclaims")
    public void onSubclaimList(Player player, @Optional String factionName) {
        if (factionName == null) {
            plugin.getFactionManager().getExecutor().showSubclaimList(player);
            return;
        }

        plugin.getFactionManager().getExecutor().showSubclaimList(player, factionName);
    }

    @Subcommand("unsubclaim")
    @Description("Unclaim a subclaim you are standing in")
    public void onUnsubclaim(Player player, @Optional String subclaimName) {
        final Promise promise = new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Subclaim has been removed");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to unsubclaim: " + s);
            }
        };

        if (subclaimName == null) {
            plugin.getFactionManager().getExecutor().unsubclaim(player, promise);
            return;
        }

        plugin.getFactionManager().getExecutor().unsubclaim(player, subclaimName, promise);
    }

    @Subcommand("stuck")
    @Description("Start a timer that will safely teleport you outside of a claim")
    public void onStuck(Player player) {
        plugin.getFactionManager().getExecutor().startStuckTimer(player, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Stuck timer started. Moving or taking damage will cancel this request.");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to start stuck timer: " + s);
            }
        });
    }

    @Subcommand("home|hq")
    @Description("Start a timer that will safely teleport you to your faction home")
    public void onHomeWarp(Player player) {
        plugin.getFactionManager().getExecutor().startHomeTimer(player, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Home timer started. Moving or taking damage will cancel this request.");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to start home timer: " + s);
            }
        });
    }

    @Subcommand("kick")
    @Description("Kick a player from your faction")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onKickFromFaction(Player player, String username) {
        plugin.getFactionManager().getExecutor().kickFromFaction(player, username, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Player has been kicked from the faction");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to kick player from faction: " + s);
            }
        });
    }

    @Subcommand("kick")
    @Description("Kick a player from a faction")
    @Syntax("<player> <faction>")
    @CommandCompletion("@players")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onKickFromFaction(Player player, String username, String factionName) {
        plugin.getFactionManager().getExecutor().kickFromFaction(player, factionName, username, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Player has been kicked from the faction");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to kick player from faction: " + s);
            }
        });
    }

    @Subcommand("announce|announcement")
    @Description("Set your faction announcement")
    @Syntax("<announcement>")
    public void onFactionAnnouncement(Player player, String announcement) {
        plugin.getFactionManager().getExecutor().setFactionAnnouncement(player, announcement, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Faction announcement has been updated");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to update faction announcement: " + s);
            }
        });
    }

    @Subcommand("promote")
    @Description("Promote a player to the next highest rank in your faction")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onFactionPromotePlayer(Player player, String promotedUsername) {
        plugin.getFactionManager().getExecutor().promotePlayer(player, promotedUsername, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Player has been promoted");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to promote player: " + s);
            }
        });
    }

    @Subcommand("demote")
    @Description("Demote a player to the next lowest rank in your faction")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onFactionDemotePlayer(Player player, String demotedUsername) {
        plugin.getFactionManager().getExecutor().demotePlayer(player, demotedUsername, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Player has been demoted");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to demote player: " + s);
            }
        });
    }

    @Subcommand("deposit|d")
    @Description("Deposit money from your personal balance to your faction balance")
    @Syntax("<amount/all>")
    public void onDepositBalance(Player player, String amount) {
        double v;

        if (amount.equalsIgnoreCase("all")) {
            final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

            if (factionPlayer == null) {
                player.sendMessage(ChatColor.RED + "Failed to deposit in to faction balance: Failed to obtain profile");
                return;
            }

            v = factionPlayer.getBalance();
        } else {
            try {
                v = Double.parseDouble(amount);
            } catch (NumberFormatException e) {
                player.sendMessage(FMessage.ERROR + "Failed to deposit in to faction balance: invalid amount");
                return;
            }
        }

        plugin.getFactionManager().getExecutor().depositMoney(player, v, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Deposit complete");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to deposit in to faction balance: " + s);
            }
        });
    }

    @Subcommand("withdraw|w")
    @Description("Withdraw money from your faction balance to your personal balance")
    @Syntax("<amount>")
    public void onWithdrawBalance(Player player, String amount) {
        double v;
        try {
            v = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            player.sendMessage(FMessage.ERROR + "Failed to withdraw from faction balance: invalid amount");
            return;
        }

        plugin.getFactionManager().getExecutor().withdrawMoney(player, v, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Withdraw complete");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to withdraw from faction balance: " + s);
            }
        });
    }

    @Subcommand("setbalance|setbal")
    @Description("Set a factions balance")
    @Syntax("<faction> <amount>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@pfactions")
    public void onSetBalance(Player player, String factionName, double amount) {
        plugin.getFactionManager().getExecutor().setBalance(player, factionName, amount, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Balance update complete");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to update balance: " + s);
            }
        });
    }

    @Subcommand("token")
    @Description("Update a factions token balance")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Syntax("<add|remove> <faction> <amount>")
    @CommandCompletion("@pfactions")
    public void onUpdateTokens(Player player, @Values("add|remove")String modifier, String factionName, String amountName) {
        int amount;

        try {
            amount = Integer.parseInt(amountName);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid token amount");
            return;
        }

        if (modifier.equalsIgnoreCase("add")) {
            plugin.getFactionManager().getExecutor().addTokens(player, factionName, amount, new Promise() {
                @Override
                public void resolve() {
                    player.sendMessage(FMessage.SUCCESS + "Tokens added successfully");
                }

                @Override
                public void reject(String s) {
                    player.sendMessage(FMessage.ERROR + "Failed to add tokens: " + s);
                }
            });

            return;
        }

        plugin.getFactionManager().getExecutor().subtractTokens(player, factionName, amount, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(FMessage.SUCCESS + "Tokens subtracted successfully");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to remove tokens: " + s);
            }
        });
    }

    @Subcommand("rally|r")
    @Description("Set the rally point for your faction")
    public void onRallyUpdate(Player player) {
        plugin.getFactionManager().getExecutor().setFactionRally(player, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Rallypoint updated to your current location");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to update rallypoint: " + s);
            }
        });
    }

    @CommandAlias("tl|fl")
    @Description("Print your current coordinates in Faction Chat")
    public void onTeamLocate(Player player) {
        plugin.getFactionManager().getExecutor().printTeamLocate(player, new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to print location: " + s);
            }
        });
    }

    @Subcommand("reload")
    @Description("Reload factions config")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onReload(Player player) {
        plugin.getConfiguration().loadConfig();
        player.sendMessage(ChatColor.GREEN + "Factions configuration has been reloaded");
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}

package gg.hcfactions.factions.models.message;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import gg.hcfactions.libs.bukkit.utils.Colors;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public final class FMessage {
    public static final ChatColor LAYER_1 = ChatColor.YELLOW;
    public static final ChatColor LAYER_2 = ChatColor.GOLD;
    public static final ChatColor SUCCESS = ChatColor.GREEN;
    public static final ChatColor ERROR = ChatColor.RED;
    public static final ChatColor INFO = ChatColor.BLUE;
    public static final ChatColor P_NAME = ChatColor.RESET;

    public static final String T_EPEARL_UNLOCKED = SUCCESS + "Your enderpearls have been unlocked";
    public static final String T_CTAG_EXPIRE = SUCCESS + "Your combat-tag has expired";
    public static final String T_CRAPPLE_UNLOCKED = SUCCESS + "Your crapples have been unlocked";
    public static final String T_GAPPLE_UNLOCKED = SUCCESS + "Your gapples have been unlocked";
    public static final String T_HOME_EXPIRE = SUCCESS + "You have been returned to your faction home";
    public static final String T_STUCK_EXPIRE = SUCCESS + "You have been teleported to safety";
    public static final String T_LOGOUT_EXPIRE = SUCCESS + "You have been disconnected safely";
    public static final String T_PROTECTION_EXPIRE = SUCCESS + "Your combat protection has expired";
    public static final String T_FREEZE_EXPIRE = SUCCESS + "Your faction will now begin regenerating power";
    public static final String T_HOME_COMPLETE = SUCCESS + "You have been returned to your faction home";
    public static final String F_KICKED_FROM_FAC = ERROR + "You have been kicked from the faction";

    public static void broadcastFactionCreated(String factionName, String playerName) {
        Bukkit.broadcastMessage(LAYER_1 + "Faction " + INFO + factionName + LAYER_1 + " has been " + SUCCESS + "created" + LAYER_1 + " by " + P_NAME + playerName);
    }

    public static void broadcastFactionDisbanded(String factionName, String playerName) {
        Bukkit.broadcastMessage(LAYER_1 + "Faction " + INFO + factionName + LAYER_1 + " has been " + ERROR + "disbanded" + LAYER_1 + " by " + P_NAME + playerName);
    }

    public static void broadcastCombatLogger(Player player) {
        Bukkit.broadcastMessage(ERROR + "Combat-Logger" + ChatColor.RESET + ": " + player.getName());
    }

    public static void printPlayerJoinedFaction(PlayerFaction faction, Player player) {
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + SUCCESS + "joined" + LAYER_1 + " the faction");
    }

    public static void printPlayerLeftFaction(PlayerFaction faction, Player player) {
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + ERROR + "left" + LAYER_1 + " the faction");
    }

    public static void printDepositReceived(Player player, double amount) {
        player.sendMessage(ChatColor.DARK_GREEN + "$" + String.format("%.2f", amount) + LAYER_1 + " has been " + SUCCESS + "deposited" + LAYER_1 + " to your personal balance");
    }

    public static void printWithdrawReceived(Player player, double amount) {
        player.sendMessage(ChatColor.DARK_GREEN + "$" + String.format("%.2f", amount) + LAYER_1 + " has been " + ERROR + "withdrawn" + LAYER_1 + " from your personal balance");
    }

    public static void printFactionMemberOnline(PlayerFaction faction, String username) {
        faction.sendMessage(ChatColor.YELLOW + "Member " + SUCCESS + "Online" + LAYER_1 + ": " + P_NAME + username);
    }

    public static void printFactionMemberOffline(PlayerFaction faction, String username) {
        faction.sendMessage(ChatColor.YELLOW + "Member " + ERROR + "Offline" + LAYER_1 + ": " + P_NAME + username);
    }

    public static void printCombatTag(Player player, long duration) {
        player.sendMessage(ERROR + "Combat Tag: " + INFO + Time.convertToHHMMSS(duration));
    }

    public static void printTimerCancelled(Player player, String timerName, String reason) {
        player.sendMessage(ERROR + "Your " + timerName + " timer was cancelled because you " + reason);
    }

    public static void printLockedTimer(Player player, String itemName, long duration) {
        final String asDecimal = Time.convertToDecimal(duration);
        player.sendMessage(ERROR + "Your " + itemName + " are locked for " + ERROR + "" + ChatColor.BOLD + asDecimal + ERROR + "s");
    }

    public static void printRallyUpdate(Player player, PlayerFaction playerFaction) {
        final BLocatable location = new BLocatable(player.getLocation().getBlock());
        playerFaction.sendMessage(P_NAME + player.getName() + LAYER_2 + " updated your faction rally to " + INFO + location);
    }

    public static void printPlayerInvite(Player player, PlayerFaction playerFaction, String username) {
        playerFaction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + SUCCESS + "invited " + P_NAME + username + LAYER_1 + " to the faction");
    }

    public static void printNowAtMaxDTR(PlayerFaction playerFaction) {
        playerFaction.sendMessage(SUCCESS + "Your faction is now at max DTR");
    }

    public static void printCanNotJoinWhileRaidable(PlayerFaction playerFaction, String username) {
        playerFaction.sendMessage(P_NAME + username + ERROR + " will not be able to join until your faction is unraidable");
    }

    public static void printCanNotJoinWhileFrozen(PlayerFaction playerFaction, String username) {
        playerFaction.sendMessage(P_NAME + username + ERROR + " will not be able to join until your faction power is thawed");
    }

    public static void printCanNotJoinFulLFaction(PlayerFaction playerFaction, String username) {
        playerFaction.sendMessage(P_NAME + username + ERROR + " will not be able to join unless a member leaves or is kicked");
    }

    public static void printReinviteWillBeConsumed(PlayerFaction playerFaction, String username) {
        if (playerFaction.getReinvites() > 0) {
            playerFaction.sendMessage(P_NAME + username + ERROR + " has left the faction recently and will consume a re-invite upon joining again");
            return;
        }

        playerFaction.sendMessage(P_NAME + username + ERROR + " will not be able to join the faction until you obtain more re-invites");
    }

    public static void printPlayerUninvite(Player player, PlayerFaction playerFaction, String username) {
        playerFaction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + ERROR + "uninvited" + P_NAME + username + LAYER_1 + " from the faction");
    }

    public static void printChatChannelChange(Player player, PlayerFaction.ChatChannel channel) {
        player.sendMessage(LAYER_1 + "You are now speaking in " + channel.getDisplayName());
    }

    public static void printCanNotFightInClaim(Player player, String claimName) {
        player.sendMessage(ERROR + "You can not attack players in " + claimName + ERROR + "'s claims");
    }

    public static void printCanNotAttackFactionMembers(Player player) {
        player.sendMessage(ERROR + "PvP is disabled between " + P_NAME + "Faction Members");
    }

    public static void printPlayerKickedFromFaction(PlayerFaction faction, Player player, String username) {
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + ERROR + "kicked " + P_NAME + username + LAYER_1 + " from the faction");
    }

    public static void printMemberDeath(PlayerFaction faction, String memberName, double deducted) {
        faction.sendMessage(ChatColor.DARK_RED + "Member Death" + P_NAME + ": " + memberName);
        faction.sendMessage(ChatColor.DARK_RED + "DTR Loss" + P_NAME + ": -" + Math.round(deducted));
    }

    public static void printFrozenPower(PlayerFaction faction, long duration) {
        final String remaining = Time.convertToRemaining(duration);
        faction.sendMessage(ERROR + "Your faction power has been frozen for " + INFO + remaining);
    }

    public static void printDTRUpdate(PlayerFaction faction, double newDtr) {
        faction.sendMessage(LAYER_1 + "Your faction DTR has been " + LAYER_2 + "updated" + LAYER_1 + " to " + INFO + String.format("%.2f", newDtr));
    }

    public static void printReinviteUpdate(PlayerFaction faction, int amount) {
        faction.sendMessage(LAYER_1 + "Your faction reinvites has been " + LAYER_2 + "updated" + LAYER_1 + " to " + INFO + amount);
    }

    public static void printAnnouncement(PlayerFaction faction, String announcement) {
        faction.sendMessage(LAYER_2 + "Faction Announcement" + P_NAME + ": " + announcement);
    }

    public static void printPromotion(Player initiater, String updatedName, PlayerFaction faction, PlayerFaction.Rank rank) {
        faction.sendMessage(P_NAME + initiater.getName() + LAYER_1 + " has " + SUCCESS + "promoted " + P_NAME + updatedName + LAYER_1 + " to " + INFO + rank.getDisplayName());
    }

    public static void printDemotion(Player initiater, String updatedName, PlayerFaction faction, PlayerFaction.Rank rank) {
        faction.sendMessage(P_NAME + initiater.getName() + LAYER_1 + " has " + ERROR + "demoted " + P_NAME + updatedName + LAYER_1 + " to " + INFO + rank.getDisplayName());
    }

    public static void printHomeUpdate(PlayerFaction faction, Player player, PLocatable location) {
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + SUCCESS + "updated" + LAYER_1 + " your faction home to " + INFO + Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ()));
    }

    public static void printReinviteConsumed(PlayerFaction faction, int newReinvites) {
        faction.sendMessage(LAYER_1 + "Remaining Faction Re-invites: " + (newReinvites <= 2 ? ERROR : INFO) + newReinvites);
    }

    public static void printBalance(Player player, double amount) {
        final String formatted = String.format("%.2f", amount);
        player.sendMessage(LAYER_2 + "Your balance" + LAYER_1 + ": " + INFO + "$" + formatted);
    }

    public static void printFactionWithdrawn(PlayerFaction faction, Player player, double amount) {
        final String formatted = String.format("%.2f", amount);
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + ERROR + "withdrawn" + INFO + "$" + formatted + LAYER_1 + " from the faction balance");
    }

    public static void printFactionDeposit(PlayerFaction faction, Player player, double amount) {
        final String formatted = String.format("%.2f", amount);
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + SUCCESS + "deposited" + INFO + "$" + formatted + LAYER_1 + " in to the faction balance");
    }

    public static void printEotwMessage(String message) {
        Bukkit.broadcastMessage(LAYER_2 + "[" + LAYER_1 + "EOTW" + LAYER_2 + "] " + Colors.DARK_AQUA.toBukkit() + message);
    }

    public static void printClassActivated(Player player, IClass playerClass) {
        player.sendMessage(LAYER_2 + "Class Activated" + LAYER_1 + ": " + INFO + playerClass.getName());
        player.sendMessage(LAYER_1 + playerClass.getDescription());
    }

    public static void printClassDeactivated(Player player, IClass playerClass) {
        player.sendMessage(LAYER_2 + "Class Deactivated" + LAYER_1 + ": " + INFO + playerClass.getName());
    }

    public static String getPublicFormat(PlayerFaction faction, String displayName, String message, Player receiver) {
        if (faction == null) {
            return displayName + ChatColor.RESET + ": " + message;
        }

        if (faction.getMember(receiver.getUniqueId()) != null) {
            return ChatColor.DARK_GREEN + "[" + faction.getName() + "]" + ChatColor.RESET + " " + displayName + ChatColor.RESET + ": " + message;
        }

        return ChatColor.GOLD + "[" + ChatColor.YELLOW + faction.getName() + ChatColor.GOLD + "]" + ChatColor.RESET + " " + displayName + ChatColor.RESET + ": " + message;
    }

    public static String getFactionFormat(String displayName, String message) {
        return ChatColor.DARK_GREEN + "(" + ChatColor.GOLD + "FC" + ChatColor.DARK_GREEN + ") " + ChatColor.RESET + displayName + ChatColor.DARK_GREEN + ": " + message;
    }

    public static void printFactionInfo(Factions plugin, Player player, IFaction faction) {
        // ◼⚠⬛⬆⬇▴▶▾
        // ⬆⬇➡
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);
        final String spacer = ChatColor.RESET + " " + LAYER_1 + " - " + ChatColor.RESET;
        final boolean access = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (faction instanceof ServerFaction) {
            final ServerFaction serverFaction = (ServerFaction)faction;

            player.sendMessage(LAYER_1 + "------------" + LAYER_2 + "[ " + INFO + serverFaction.getDisplayName() + LAYER_2 + " ]" + LAYER_1 + "------------");
            player.sendMessage(spacer + ChatColor.DARK_PURPLE + StringUtils.capitalize(serverFaction.getFlag().name().toLowerCase().replace("_", " ")));

            if (serverFaction.getHomeLocation() != null) {
                player.sendMessage(spacer + LAYER_2 + "Located At" + LAYER_1 + ": " +
                        INFO + (int)(Math.round(serverFaction.getHomeLocation().getX())) + LAYER_1 + ", " +
                        INFO + (int)(Math.round(serverFaction.getHomeLocation().getY())) + LAYER_1 + ", " +
                        INFO + (int)(Math.round(serverFaction.getHomeLocation().getZ())) + LAYER_1 + ", " +
                        INFO + StringUtils.capitalize(Objects.requireNonNull(serverFaction
                        .getHomeLocation()
                        .getBukkitLocation()
                        .getWorld()).getEnvironment().name().toLowerCase().replace("_", " ")));
            }

            player.sendMessage(LAYER_1 + "--------------------------------");

            return;
        }

        final PlayerFaction playerFaction = (PlayerFaction)faction;
        final String unformattedDTR = String.format("%.2f", playerFaction.getDtr());
        String DTR;

        if (playerFaction.getDtr() >= playerFaction.getMaxDtr()) {
            DTR = ChatColor.GREEN + unformattedDTR + " ➡";
        } else if (!playerFaction.isRaidable() && playerFaction.getDtr() < playerFaction.getMaxDtr() && !playerFaction.isFrozen()) {
            DTR = ChatColor.YELLOW + unformattedDTR + " ⬆";
        } else if (playerFaction.isRaidable() && playerFaction.isFrozen()) {
            DTR = ChatColor.RED + unformattedDTR + " ⚠";
        } else if (playerFaction.isRaidable() && !playerFaction.isFrozen()) {
            DTR = ChatColor.RED + unformattedDTR + " ⬆";
        } else if (playerFaction.isFrozen()) {
            DTR = ChatColor.YELLOW + unformattedDTR + " ➡";
        } else {
            DTR = ChatColor.YELLOW + unformattedDTR;
        }

        player.sendMessage(LAYER_1 + "--------------------" + LAYER_2 + "[ " + INFO + faction.getName() + LAYER_2 + " ]" + LAYER_1 + "--------------------");

        if (playerFaction.getAnnouncement() != null && (playerFaction.isMember(player.getUniqueId()) || access)) {
            player.sendMessage(spacer + LAYER_2 + "Announcement" + LAYER_1 + ": " + INFO + playerFaction.getAnnouncement());
        }

        if (playerFaction.getRallyLocation() != null && (playerFaction.isMember(player.getUniqueId()) || access)) {
            player.sendMessage(spacer + LAYER_2 + "Rally" + LAYER_1 + ": " +
                    INFO + (int)(Math.round(playerFaction.getRallyLocation().getX())) + LAYER_1 + ", " +
                    INFO + (int)(Math.round(playerFaction.getRallyLocation().getY())) + LAYER_1 + ", " +
                    INFO + (int)(Math.round(playerFaction.getRallyLocation().getZ())) + LAYER_1 + ", " +
                    INFO + StringUtils.capitalize(Objects.requireNonNull(playerFaction
                    .getRallyLocation()
                    .getBukkitLocation()
                    .getWorld()).getEnvironment().name().toLowerCase().replace("_", " ")));
        }

        if (playerFaction.getHomeLocation() != null) {
            player.sendMessage(spacer + LAYER_2 + "Home" + LAYER_1 + ": " +
                    INFO + (int)(Math.round(playerFaction.getHomeLocation().getX())) + LAYER_1 + ", " +
                    INFO + (int)(Math.round(playerFaction.getHomeLocation().getY())) + LAYER_1 + ", " +
                    INFO + (int)(Math.round(playerFaction.getHomeLocation().getZ())));
        }

        player.sendMessage(spacer + LAYER_2 + "Balance" + LAYER_1 + ": " + INFO + "$" + String.format("%.2f", playerFaction.getBalance()));
        player.sendMessage(spacer + LAYER_2 + "Deaths Until Raid-able" + LAYER_1 + ": " + DTR);

        if (playerFaction.isFrozen()) {
            final FTimer timer = playerFaction.getTimer(ETimerType.FREEZE);
            player.sendMessage(spacer + LAYER_2 + "Frozen" + LAYER_1 + ": " + INFO + Time.convertToRemaining(timer.getRemaining()));
        }

        player.sendMessage(spacer + LAYER_2 + "Re-invites" + LAYER_1 + ": " + INFO + playerFaction.getReinvites());
        player.sendMessage(spacer + LAYER_2 + "Online" + LAYER_1 + ": " + INFO + playerFaction.getOnlineMembers().size() + LAYER_1 + " / " + INFO + playerFaction.getMembers().size());

        new Scheduler(plugin).async(() -> {
            final Map<PlayerFaction.Rank, List<String>> namesByRank = Maps.newHashMap();

            for (PlayerFaction.Rank rank : PlayerFaction.Rank.values()) {
                final List<String> usernames = Lists.newArrayList();

                for (PlayerFaction.Member member : playerFaction.getMembersByRank(rank)) {
                    final AresAccount account = acs.getAccount(member.getUniqueId());

                    if (account != null) {
                        usernames.add(account.getUsername());
                    }
                }

                namesByRank.put(rank, usernames);
            }

            new Scheduler(plugin).sync(() -> {
                final Map<PlayerFaction.Rank, List<String>> formattedNames = Maps.newHashMap();

                for (PlayerFaction.Rank rank : namesByRank.keySet()) {
                    final List<String> names = namesByRank.get(rank);
                    final List<String> formatted = Lists.newArrayList();

                    names.sort(Comparator.comparing(name -> Bukkit.getPlayer(name) != null));

                    for (String name : names) {
                        if (Bukkit.getPlayer(name) != null) {
                            formatted.add(ChatColor.GREEN + name);
                        } else {
                            formatted.add(ChatColor.GRAY + name);
                        }
                    }

                    Collections.reverse(formatted);
                    formattedNames.put(rank, formatted);
                }

                final List<String> leaders = formattedNames.get(PlayerFaction.Rank.LEADER);
                final List<String> officers = formattedNames.get(PlayerFaction.Rank.OFFICER);
                final List<String> members = formattedNames.get(PlayerFaction.Rank.MEMBER);

                if (!leaders.isEmpty()) {
                    player.sendMessage(spacer + LAYER_2 + "Leader" + LAYER_1 + ": " + Joiner.on(LAYER_1 + ", ").join(leaders));
                }

                if (!officers.isEmpty()) {
                    player.sendMessage(spacer + LAYER_2 + "Officers" + LAYER_1 + ": " + Joiner.on(LAYER_1 + ", ").join(officers));
                }

                if (!members.isEmpty()) {
                    player.sendMessage(spacer + LAYER_2 + "Members" + LAYER_1 + ": " + Joiner.on(LAYER_1 + ", ").join(members));
                }

                player.sendMessage(LAYER_1 + "------------------------------------------------");
            }).run();
        }).run();
    }
}

package gg.hcfactions.factions.models.message;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.client.model.Filters;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
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

    public static void broadcastFactionCreated(String factionName, String playerName) {
        Bukkit.broadcastMessage(LAYER_1 + "Faction " + INFO + factionName + LAYER_1 + " has been " + SUCCESS + "created" + LAYER_1 + " by " + P_NAME + playerName);
    }

    public static void broadcastFactionDisbanded(String factionName, String playerName) {
        Bukkit.broadcastMessage(LAYER_1 + "Faction " + INFO + factionName + LAYER_1 + " has been " + ERROR + "disbanded" + LAYER_1 + " by " + P_NAME + playerName);
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

    public static void printFactionInfo(Factions plugin, Player player, IFaction faction) {
        // ◼⚠⬛⬆⬇▴▶▾
        // ⬆⬇➡
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);
        final String spacer = ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.RESET;
        final boolean access = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (faction instanceof ServerFaction) {
            final ServerFaction serverFaction = (ServerFaction)faction;

            player.sendMessage(ChatColor.YELLOW + "------------" + ChatColor.GOLD + "[ " + ChatColor.WHITE + serverFaction.getDisplayName() + ChatColor.GOLD + " ]" + ChatColor.YELLOW + "------------");
            player.sendMessage(spacer + ChatColor.DARK_PURPLE + StringUtils.capitalize(serverFaction.getFlag().name().toLowerCase().replace("_", " ")));

            if (serverFaction.getHomeLocation() != null) {
                player.sendMessage(spacer + ChatColor.GOLD + "Located At" + ChatColor.YELLOW + ": " +
                        ChatColor.BLUE + (int)(Math.round(serverFaction.getHomeLocation().getX())) + ChatColor.YELLOW + ", " +
                        ChatColor.BLUE + (int)(Math.round(serverFaction.getHomeLocation().getY())) + ChatColor.YELLOW + ", " +
                        ChatColor.BLUE + (int)(Math.round(serverFaction.getHomeLocation().getZ())) + ChatColor.YELLOW + ", " +
                        ChatColor.BLUE + StringUtils.capitalize(Objects.requireNonNull(serverFaction
                        .getHomeLocation()
                        .getBukkitLocation()
                        .getWorld()).getEnvironment().name().toLowerCase().replace("_", " ")));
            }

            player.sendMessage(ChatColor.YELLOW + "--------------------------------");

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

        player.sendMessage(ChatColor.YELLOW + "--------------------" + ChatColor.GOLD + "[ " + ChatColor.WHITE + faction.getName() + ChatColor.GOLD + " ]" + ChatColor.YELLOW + "--------------------");

        if (playerFaction.getAnnouncement() != null && (playerFaction.isMember(player.getUniqueId()) || access)) {
            player.sendMessage(spacer + ChatColor.GOLD + "Announcement" + ChatColor.YELLOW + ": " + ChatColor.LIGHT_PURPLE + playerFaction.getAnnouncement());
        }

        if (playerFaction.getRallyLocation() != null && (playerFaction.isMember(player.getUniqueId()) || access)) {
            player.sendMessage(spacer + ChatColor.GOLD + "Rally" + ChatColor.YELLOW + ": " +
                    ChatColor.BLUE + (int)(Math.round(playerFaction.getRallyLocation().getX())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(playerFaction.getRallyLocation().getY())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(playerFaction.getRallyLocation().getZ())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + StringUtils.capitalize(Objects.requireNonNull(playerFaction
                    .getRallyLocation()
                    .getBukkitLocation()
                    .getWorld()).getEnvironment().name().toLowerCase().replace("_", " ")));
        }

        if (playerFaction.getHomeLocation() != null) {
            player.sendMessage(spacer + ChatColor.GOLD + "Home" + ChatColor.YELLOW + ": " +
                    ChatColor.BLUE + (int)(Math.round(playerFaction.getHomeLocation().getX())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(playerFaction.getHomeLocation().getY())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(playerFaction.getHomeLocation().getZ())));
        }

        player.sendMessage(spacer + ChatColor.GOLD + "Balance" + ChatColor.YELLOW + ": " + ChatColor.BLUE + "$" + String.format("%.2f", playerFaction.getBalance()));
        player.sendMessage(spacer + ChatColor.GOLD + "Deaths Until Raid-able" + ChatColor.YELLOW + ": " + DTR);

        if (playerFaction.isFrozen()) {
            final FTimer timer = playerFaction.getTimer(ETimerType.FREEZE);
            player.sendMessage(spacer + ChatColor.GOLD + "Frozen" + ChatColor.YELLOW + ": " + ChatColor.BLUE + Time.convertToRemaining(timer.getRemaining()));
        }

        player.sendMessage(spacer + ChatColor.GOLD + "Re-invites" + ChatColor.YELLOW + ": " + ChatColor.BLUE + playerFaction.getReinvites());
        player.sendMessage(spacer + ChatColor.GOLD + "Online" + ChatColor.YELLOW + ": " + ChatColor.BLUE + playerFaction.getOnlineMembers().size() + ChatColor.YELLOW + " / " + ChatColor.BLUE + playerFaction.getMembers().size());

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
                    player.sendMessage(spacer + ChatColor.GOLD + "Leader" + ChatColor.YELLOW + ": " + Joiner.on(ChatColor.YELLOW + ", ").join(leaders));
                }

                if (!officers.isEmpty()) {
                    player.sendMessage(spacer + ChatColor.GOLD + "Officers" + ChatColor.YELLOW + ": " + Joiner.on(ChatColor.YELLOW + ", ").join(officers));
                }

                if (!members.isEmpty()) {
                    player.sendMessage(spacer + ChatColor.GOLD + "Members" + ChatColor.YELLOW + ": " + Joiner.on(ChatColor.YELLOW + ", ").join(members));
                }

                player.sendMessage(ChatColor.YELLOW + "------------------------------------------------");
            }).run();
        }).run();
    }
}

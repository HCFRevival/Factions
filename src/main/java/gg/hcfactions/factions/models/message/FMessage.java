package gg.hcfactions.factions.models.message;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.subclaim.Subclaim;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanService;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.impl.Deathban;
import gg.hcfactions.libs.bukkit.utils.Colors;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

public final class FMessage {
    public static final ChatColor LAYER_1 = ChatColor.YELLOW;
    public static final ChatColor LAYER_2 = ChatColor.GOLD;
    public static final ChatColor SUCCESS = ChatColor.GREEN;
    public static final ChatColor ERROR = ChatColor.RED;
    public static final ChatColor INFO = ChatColor.BLUE;
    public static final ChatColor P_NAME = ChatColor.RESET;

    public static final String KOTH_PREFIX = LAYER_2 + "[" + LAYER_1 + "KOTH" + LAYER_2 + "] " + LAYER_1;
    public static final String PALACE_PREFIX = LAYER_2 + "[" + LAYER_1 + "Palace" + LAYER_2 + "] " + LAYER_1;
    public static final String PVE_PREFIX = LAYER_2 + "[" + ChatColor.DARK_RED + "PvE" + LAYER_2 + "] " + LAYER_1;
    public static final String CONQ_PREFIX = LAYER_2 + "[" + Colors.RED.toBukkit() + "Conquest" + LAYER_2 + "] " + LAYER_1;
    public static final String OUTPOST_PREFIX = LAYER_2 + "[" + Colors.GOLD.toBukkit() + "Outpost" + LAYER_2 + "] " + LAYER_1;
    public static final String EOTW_PREFIX = LAYER_2 + "[" + LAYER_1 + "EOTW" + LAYER_2 + "] " + LAYER_1;

    public static final String T_EPEARL_UNLOCKED = SUCCESS + "Your enderpearls have been unlocked";
    public static final String T_CTAG_EXPIRE = SUCCESS + "Your combat-tag has expired";
    public static final String T_CRAPPLE_UNLOCKED = SUCCESS + "Your crapples have been unlocked";
    public static final String T_GAPPLE_UNLOCKED = SUCCESS + "Your gapples have been unlocked";
    public static final String T_TRIDENT_UNLOCKED = SUCCESS + "Your riptide has been unlocked";
    public static final String T_HOME_EXPIRE = SUCCESS + "You have been returned to your faction home";
    public static final String T_STUCK_EXPIRE = SUCCESS + "You have been teleported to safety";
    public static final String T_LOGOUT_EXPIRE = SUCCESS + "You have been disconnected safely";
    public static final String T_PROTECTION_EXPIRE = SUCCESS + "Your combat protection has expired";
    public static final String T_FREEZE_EXPIRE = SUCCESS + "Your faction will now begin regenerating power";
    public static final String T_HOME_COMPLETE = SUCCESS + "You have been returned to your faction home";
    public static final String T_ARCHER_MARK_COMPLETE = SUCCESS + "You are no longer marked";

    public static final String F_KICKED_FROM_FAC = ERROR + "You have been kicked from the faction";
    public static final String F_MAP_PILLARS_HIDDEN = LAYER_1 + "Map pillars have been hidden";

    public static void broadcastFactionCreated(String factionName, String playerName) {
        Bukkit.broadcastMessage(LAYER_1 + "Faction " + INFO + factionName + LAYER_1 + " has been " + SUCCESS + "created" + LAYER_1 + " by " + P_NAME + playerName);
    }

    public static void broadcastFactionDisbanded(String factionName, String playerName) {
        Bukkit.broadcastMessage(LAYER_1 + "Faction " + INFO + factionName + LAYER_1 + " has been " + ERROR + "disbanded" + LAYER_1 + " by " + P_NAME + playerName);
    }

    public static void broadcastCombatLogger(Player player) {
        Bukkit.broadcastMessage(ERROR + "Combat-Logger" + ChatColor.RESET + ": " + player.getName());
    }

    public static void broadcastCaptureEventMessage(String message) {
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(KOTH_PREFIX + message);
        Bukkit.broadcastMessage(" ");
    }

    public static void broadcastConquestMessage(String message) {
        Bukkit.broadcastMessage(" ");
        Bukkit.broadcastMessage(CONQ_PREFIX + message);
        Bukkit.broadcastMessage(" ");
    }

    public static void broadcastFactionRaidable(PlayerFaction faction) {
        Bukkit.broadcastMessage(ChatColor.RESET + faction.getName() + ChatColor.RED + " is now " + ChatColor.DARK_RED + "RAID-ABLE");
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
        final int x = player.getLocation().getBlockX();
        final int y = player.getLocation().getBlockY();
        final int z = player.getLocation().getBlockZ();
        final String env = StringUtils.capitalize(player.getWorld().getEnvironment().name().toLowerCase(Locale.ROOT).replaceAll("_", " "));

        playerFaction.sendMessage(P_NAME + player.getName() + LAYER_2 + " updated your faction rally to " + INFO + x + " " + y + " " + z + " " + env);
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
        playerFaction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + ERROR + "uninvited " + P_NAME + username + LAYER_1 + " from the faction");
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
        faction.sendMessage(ChatColor.DARK_RED + "DTR Loss" + P_NAME + ": -" + deducted);
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

    public static void printHomeUnset(PlayerFaction faction, Player player) {
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + ERROR + "unset" + LAYER_1 + " your faction home");
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
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + ERROR + "withdrawn" + INFO + " $" + formatted + LAYER_1 + " from the faction balance");
    }

    public static void printFactionDeposit(PlayerFaction faction, Player player, double amount) {
        final String formatted = String.format("%.2f", amount);
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + SUCCESS + "deposited" + INFO + " $" + formatted + LAYER_1 + " in to the faction balance");
    }

    public static void printFactionBalanceSet(PlayerFaction faction, Player player, double amount) {
        final String formatted = String.format("%.2f", amount);
        faction.sendMessage(P_NAME + player.getName() + LAYER_1 + " has " + LAYER_2 + "updated" + LAYER_1 + " your faction balance to " + INFO + " $" + formatted);
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

    public static void printSubclaimAdded(Subclaim subclaim, String addingUsername, String addedUsername) {
        subclaim.sendMessage(P_NAME + addingUsername + LAYER_1 + " has " + SUCCESS + "added " + P_NAME + addedUsername + LAYER_1 + " to " + INFO + subclaim.getName());
    }

    public static void printSubclaimRemoved(Subclaim subclaim, String removingUsername, String removedUsername) {
        subclaim.sendMessage(P_NAME + removingUsername + LAYER_1 + " has " + ERROR + "removed " + P_NAME + removedUsername + LAYER_1 + " from " + INFO + subclaim.getName());
    }

    public static void printSubclaimCreated(PlayerFaction faction, String creatorUsername, String subclaimName) {
        faction.sendMessage(P_NAME + creatorUsername + LAYER_1 + " has " + SUCCESS + "created" + LAYER_1 + " a new subclaim: " + INFO + subclaimName);
    }

    public static void printSubclaimDeleted(Subclaim subclaim, String deletingUsername) {
        subclaim.sendMessage(P_NAME + deletingUsername + LAYER_1 + " has " + ERROR + "deleted " + INFO + subclaim.getName());
    }

    public static String getPublicFormat(PlayerFaction faction, String displayName, long kills, String message, Player receiver) {
        if (faction == null) {
            return displayName + ChatColor.RESET + ": " + message;
        }

        if (faction.getMember(receiver.getUniqueId()) != null) {
            return ChatColor.DARK_GREEN + "[" + faction.getName() + "]" + ChatColor.BLUE + "[" + kills + "]" + ChatColor.RESET + displayName + ChatColor.RESET + ": " + message;
        }

        return ChatColor.RED + "[" + faction.getName() + "]" + ChatColor.BLUE + "[" + kills + "]" + ChatColor.RESET + displayName + ChatColor.RESET + ": " + message;
    }

    public static String getFactionFormat(String displayName, String message) {
        return ChatColor.DARK_GREEN + "(" + ChatColor.GOLD + "FC" + ChatColor.DARK_GREEN + ") " + ChatColor.RESET + displayName + ChatColor.DARK_GREEN + ": " + message;
    }

    public static void printFocusedByFaction(PlayerFaction faction, Player player) {
        player.sendMessage(LAYER_1 + "You are being " + ERROR + "focused" + LAYER_1 + " by " + INFO + faction.getName());
    }

    public static void printFocusing(PlayerFaction faction, Player initiated, Player player) {
        faction.sendMessage(P_NAME + initiated.getName() + LAYER_1 + " wants to focus " + ChatColor.LIGHT_PURPLE + player.getName());
    }

    public static void printNoLongerFocused(PlayerFaction faction, Player player) {
        player.sendMessage(LAYER_1 + "You are no longer being " + ERROR + "focused" + LAYER_1 + " by " + INFO + faction.getName());
    }

    public static void printStaffDeathMessage(Player viewer, String username, Location location) {
        viewer.spigot().sendMessage(
                new ComponentBuilder("[Teleport to " + username + "'s Death Location]")
                        .color(net.md_5.bungee.api.ChatColor.GRAY)
                        .italic(true)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + location.getBlockX() + " " + location.getY() + " " + location.getZ() + " " + Objects.requireNonNull(location.getWorld()).getName()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to teleport").create()))
                        .create());
    }

    public static void printStaffCombatLogger(Player viewer, String username, Location location) {
        viewer.spigot().sendMessage(
                new ComponentBuilder("[Teleport to " + username + "'s Combat Logger]")
                        .color(net.md_5.bungee.api.ChatColor.GRAY)
                        .italic(true)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + location.getBlockX() + " " + location.getY() + " " + location.getZ() + " " + Objects.requireNonNull(location.getWorld()).getName()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to teleport").create()))
                        .create()
        );
    }

    public static List<String> getEnemyNametag(String username, String factionName) {
        final List<String> res = Lists.newArrayList();

        if (factionName != null) {
            res.add(ChatColor.GRAY + "[" + ChatColor.RED + factionName + ChatColor.GRAY + "]");
        }

        res.add(ChatColor.RED + username);
        return res;
    }

    public static List<String> getFriendlyNametag(String username, @Nonnull String factionName) {
        final List<String> res = Lists.newArrayList();
        res.add(ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + factionName + ChatColor.GRAY + "]");
        res.add(ChatColor.DARK_GREEN + username);
        return res;
    }

    public static List<String> getFocusedNametag(String username, String factionName) {
        final List<String> res = Lists.newArrayList();

        if (factionName != null) {
            res.add(ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + factionName + ChatColor.GRAY + "]");
        }

        res.add(ChatColor.LIGHT_PURPLE + username);
        return res;
    }

    public static void printFactionInfo(Factions plugin, Player player, IFaction faction) {
        // ◼⚠⬛⬆⬇▴▶▾
        // ⬆⬇➡
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);
        final DeathbanService dbs = (DeathbanService) plugin.getService(DeathbanService.class);
        final boolean access = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);
        final List<String> message = Lists.newArrayList();

        if (faction instanceof final ServerFaction serverFaction) {
            message.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + Strings.repeat("-", 10));
            message.add(serverFaction.getDisplayName() + ChatColor.GRAY + " [" + serverFaction.getFlag().getDisplayName() + ChatColor.GRAY + "]");

            if (serverFaction.getHomeLocation() != null) {
                message.add(ChatColor.YELLOW + "Located At: " + ChatColor.BLUE +
                        (int)(Math.round(serverFaction.getHomeLocation().getX())) + LAYER_1 + ", " +
                        (int)(Math.round(serverFaction.getHomeLocation().getY())) + LAYER_1 + ", " +
                        (int)(Math.round(serverFaction.getHomeLocation().getZ())) + LAYER_1 + ", " +
                        gg.hcfactions.libs.base.util.Strings.capitalize(Objects.requireNonNull(serverFaction
                        .getHomeLocation()
                        .getBukkitLocation()
                        .getWorld()).getEnvironment().name().toLowerCase().replace("_", " ")));
            }

            message.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + Strings.repeat("-", 10));
            message.forEach(player::sendMessage);
            return;
        }

        final PlayerFaction playerFaction = (PlayerFaction)faction;
        final String unformattedDTR = String.format("%.2f", playerFaction.getDtr()) + "/" + String.format("%.2f", playerFaction.getMaxDtr());
        String homeLocation = "Unset";
        String DTR;

        if (playerFaction.getHomeLocation() != null) {
            final int x = (int)Math.round(playerFaction.getHomeLocation().getX());
            final int y = (int)Math.round(playerFaction.getHomeLocation().getY());
            final int z = (int)Math.round(playerFaction.getHomeLocation().getZ());
            homeLocation = x + ", " + y + ", " + z;
        }

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

        message.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + Strings.repeat("-", 10));
        message.add(ChatColor.BLUE + faction.getName() + " "
                + ChatColor.GRAY + "[" + ChatColor.GREEN + playerFaction.getOnlineMembers().size() + ChatColor.GRAY + "/" + playerFaction.getMembers().size() + "]"
                + ChatColor.YELLOW + " | Home: " + ChatColor.RESET + homeLocation);

        if (playerFaction.getAnnouncement() != null && (playerFaction.isMember(player.getUniqueId()) || access)) {
            message.add(ChatColor.YELLOW + "Announcement: " + ChatColor.BLUE + playerFaction.getAnnouncement());
        }

        if (playerFaction.getRallyLocation() != null && (playerFaction.isMember(player.getUniqueId()) || access)) {
            message.add(ChatColor.YELLOW + "Rally: " + ChatColor.BLUE +
                    (int)(Math.round(playerFaction.getRallyLocation().getX())) + ", " +
                    (int)(Math.round(playerFaction.getRallyLocation().getY())) + ", " +
                    (int)(Math.round(playerFaction.getRallyLocation().getZ())) + ", " +
                    gg.hcfactions.libs.base.util.Strings.capitalize(Objects.requireNonNull(playerFaction
                    .getRallyLocation()
                    .getBukkitLocation()
                    .getWorld()).getEnvironment().name().toLowerCase().replace("_", " ")));
        }

        message.add(ChatColor.YELLOW + "Balance: " + ChatColor.BLUE + "$" + String.format("%.2f", playerFaction.getBalance()));
        message.add(ChatColor.YELLOW + "Tokens: " + ChatColor.BLUE + playerFaction.getTokens());
        message.add(ChatColor.YELLOW + "Deaths until Raid-able: " + DTR);

        if (playerFaction.isFrozen()) {
            final FTimer timer = playerFaction.getTimer(ETimerType.FREEZE);
            message.add(ChatColor.YELLOW + "Frozen: " + ChatColor.BLUE + Time.convertToRemaining(timer.getRemaining()));
        }

        message.add(ChatColor.YELLOW + "Re-invites: " + ChatColor.BLUE + playerFaction.getReinvites());

        new Scheduler(plugin).async(() -> {
            final Map<PlayerFaction.Rank, List<String>> namesByRank = Maps.newHashMap();
            final List<String> deathbannedUsernames = Lists.newArrayList();

            for (PlayerFaction.Rank rank : PlayerFaction.Rank.values()) {
                final List<String> usernames = Lists.newArrayList();

                for (PlayerFaction.Member member : playerFaction.getMembersByRank(rank)) {
                    final AresAccount account = acs.getAccount(member.getUniqueId());
                    final Deathban deathban = dbs.getDeathban(member.getUniqueId());

                    if (account != null) {
                        usernames.add(account.getUsername());

                        if (deathban != null && !deathban.isExpired()) {
                            deathbannedUsernames.add(account.getUsername());
                        }
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
                        if (Bukkit.getPlayerExact(name) != null) {
                            formatted.add(ChatColor.GREEN + name);
                        } else if (deathbannedUsernames.contains(name)) {
                            formatted.add(ChatColor.RED + name);
                        } else {
                            formatted.add(ChatColor.GRAY + name);
                        }
                    }

                    Collections.reverse(formatted);
                    formattedNames.put(rank, formatted);
                }

                if (formattedNames.containsKey(PlayerFaction.Rank.LEADER)) {
                    message.add(ChatColor.YELLOW + (formattedNames.get(PlayerFaction.Rank.LEADER).size() > 1 ? "Leaders: " : "Leader: ")
                            + Joiner.on(ChatColor.YELLOW + ", ").join(formattedNames.get(PlayerFaction.Rank.LEADER)));
                }

                if (formattedNames.containsKey(PlayerFaction.Rank.OFFICER) && formattedNames.get(PlayerFaction.Rank.OFFICER).size() > 0) {
                    message.add(ChatColor.YELLOW + (formattedNames.get(PlayerFaction.Rank.LEADER).size() > 1 ? "Officers: " : "Officer: ")
                            + Joiner.on(ChatColor.YELLOW + ", ").join(formattedNames.get(PlayerFaction.Rank.OFFICER)));
                }

                if (formattedNames.containsKey(PlayerFaction.Rank.MEMBER) && formattedNames.get(PlayerFaction.Rank.MEMBER).size() > 0) {
                    message.add(ChatColor.YELLOW + (formattedNames.get(PlayerFaction.Rank.MEMBER).size() > 1 ? "Members: " : "Member: ")
                            + Joiner.on(ChatColor.YELLOW + ", ").join(formattedNames.get(PlayerFaction.Rank.MEMBER)));
                }

                message.add(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + Strings.repeat("-", 10));
                message.forEach(player::sendMessage);
            }).run();
        }).run();
    }

    /**
     * Handles printing the message showing a list of subclaims
     * @param player Player
     * @param subclaims Subclaims to display
     */
    public static void listSubclaims(Player player, Collection<Subclaim> subclaims) {
        player.sendMessage(LAYER_2 + "" + ChatColor.BOLD + "Subclaim List" + LAYER_2 + " (" + LAYER_1 + subclaims.size() + " found" + LAYER_2 + ")");

        int pos = 1;

        for (Subclaim subclaim : subclaims) {
            player.spigot().sendMessage(
                    new ComponentBuilder(" ").color(net.md_5.bungee.api.ChatColor.RESET)
                            .append(" ").color(net.md_5.bungee.api.ChatColor.RESET)
                            .append(pos + ". ").color(net.md_5.bungee.api.ChatColor.GOLD)
                            .append(subclaim.getName())
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("There is " + subclaim.getMembers().size() + " members in this subclaim").color(net.md_5.bungee.api.ChatColor.YELLOW).create()))
                            .append(" [").color(net.md_5.bungee.api.ChatColor.GRAY)
                            .append(subclaim.getCorner(1).toString()).color(net.md_5.bungee.api.ChatColor.GOLD)
                            .append("]").color(net.md_5.bungee.api.ChatColor.GRAY).create());

            pos += 1;
        }
    }
}

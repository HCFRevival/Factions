package gg.hcfactions.factions.models.message;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.items.mythic.impl.*;
import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.subclaim.Subclaim;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.factions.utils.StringUtil;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanService;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.impl.Deathban;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public final class FMessage {
    @Deprecated public static final ChatColor LAYER_1 = ChatColor.YELLOW;
    @Deprecated public static final ChatColor LAYER_2 = ChatColor.GOLD;
    @Deprecated public static final ChatColor SUCCESS = ChatColor.GREEN;
    @Deprecated public static final ChatColor ERROR = ChatColor.RED;
    @Deprecated public static final ChatColor INFO = ChatColor.BLUE;
    @Deprecated public static final ChatColor P_NAME = ChatColor.RESET;

    @Deprecated public static final String DPS_PREFIX = LAYER_2 + "[" + ChatColor.RED + "DPS" + LAYER_2 + "] " + LAYER_1;
    @Deprecated public static final String KOTH_PREFIX = LAYER_2 + "[" + LAYER_1 + "KOTH" + LAYER_2 + "] " + LAYER_1;
    @Deprecated public static final String PALACE_PREFIX = LAYER_2 + "[" + LAYER_1 + "Palace" + LAYER_2 + "] " + LAYER_1;
    @Deprecated public static final String PVE_PREFIX = LAYER_2 + "[" + ChatColor.DARK_RED + "PvE" + LAYER_2 + "] " + LAYER_1;
    @Deprecated public static final String CONQ_PREFIX = LAYER_2 + "[" + ChatColor.RED + "Conquest" + LAYER_2 + "] " + LAYER_1;
    @Deprecated public static final String OUTPOST_PREFIX = LAYER_2 + "[" + ChatColor.GOLD + "Outpost" + LAYER_2 + "] " + LAYER_1;
    @Deprecated public static final String EOTW_PREFIX = LAYER_2 + "[" + LAYER_1 + "EOTW" + LAYER_2 + "] " + LAYER_1;

    public static final TextColor TC_LAYER1 = TextColor.color(0xffdf61);
    public static final TextColor TC_LAYER2 = TextColor.color(0xffb700);
    public static final TextColor TC_SUCCESS = TextColor.color(0x55ff55);
    public static final TextColor TC_ERROR = TextColor.color(0xff5555);
    public static final TextColor TC_INFO = TextColor.color(0x00a2ff);
    public static final TextColor TC_NAME = TextColor.color(0xffffff);
    public static final TextColor TC_DANGER = TextColor.color(0xaa0000);
    public static final TextColor TC_MYTHIC_ABILITY_DESC = TextColor.color(0xd9d9d9);

    public static final Component DPS_PRE = Component.text("[").color(TC_LAYER2)
            .append(Component.text("DPS").color(TC_LAYER1)
            .append(Component.text("]").color(TC_LAYER2).appendSpace()));

    public static final Component KOTH_PRE = Component.text("[").color(TC_LAYER2)
            .append(Component.text("KOTH").color(TC_LAYER1)
                    .append(Component.text("]").color(TC_LAYER2).appendSpace()));

    public static final Component PALACE_PRE = Component.text("[").color(TC_LAYER2)
            .append(Component.text("Palace").color(TC_LAYER1)
                    .append(Component.text("]").color(TC_LAYER2).appendSpace()));

    public static final Component CONQ_PRE = Component.text("[").color(TC_LAYER2)
            .append(Component.text("Conquest").color(TC_LAYER1)
                    .append(Component.text("]").color(TC_LAYER2).appendSpace()));

    public static final Component OUTPOST_PRE = Component.text("[").color(TC_LAYER2)
            .append(Component.text("Outpost").color(TC_LAYER1)
                    .append(Component.text("]").color(TC_LAYER2).appendSpace()));

    public static final Component EOTW_PRE = Component.text("[").color(TC_LAYER2)
            .append(Component.text("EOTW").color(TC_LAYER1)
                    .append(Component.text("]").color(TC_LAYER2).appendSpace()));

    public static final String T_EPEARL_UNLOCKED = SUCCESS + "Your enderpearls have been unlocked";
    public static final String T_WINDCHARGE_UNLOCKED = SUCCESS + "Your wind charges have been unlocked";
    public static final String T_CTAG_EXPIRE = SUCCESS + "Your combat-tag has expired";
    public static final String T_CRAPPLE_UNLOCKED = SUCCESS + "Your crapples have been unlocked";
    public static final String T_GAPPLE_UNLOCKED = SUCCESS + "Your gapples have been unlocked";
    public static final String T_CHORUS_UNLOCKED = SUCCESS + "Your chorus fruit have been unlocked";
    public static final String T_TRIDENT_UNLOCKED = SUCCESS + "Your riptide has been unlocked";
    public static final String T_GRAPPLE_UNLOCKED = SUCCESS + "Your grapple has been unlocked";
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
        Component content = Component.text("Faction").color(TC_LAYER1)
                        .appendSpace().append(Component.text(factionName).color(TC_INFO))
                        .appendSpace().append(Component.text("has been").color(TC_LAYER1))
                        .appendSpace().append(Component.text("created").color(TC_SUCCESS))
                        .appendSpace().append(Component.text("by").color(TC_LAYER1))
                        .appendSpace().append(Component.text(playerName).color(TC_NAME));

        Bukkit.broadcast(content);
    }

    public static void broadcastFactionDisbanded(String factionName, String playerName) {
        Component content = Component.text("Faction").color(TC_LAYER1)
                .appendSpace().append(Component.text(factionName).color(TC_INFO))
                .appendSpace().append(Component.text("has been").color(TC_LAYER1))
                .appendSpace().append(Component.text("disbanded").color(TC_ERROR))
                .appendSpace().append(Component.text("by").color(TC_LAYER1))
                .appendSpace().append(Component.text(playerName).color(TC_NAME));

        Bukkit.broadcast(content);
    }

    public static void broadcastCombatLogger(Player player) {
        Component content = Component.text("Combat Logger").color(TC_ERROR)
                        .append(Component.text(":").color(TC_NAME)
                        .appendSpace().append(Component.text(player.getName())));

        Bukkit.broadcast(content);
    }

    public static void broadcastEventTrackerPublish(String url) {
        Component content = Component.text("Event Statistics").color(TC_LAYER1)
                .append(Component.text(":").color(TC_LAYER2)
                .appendSpace().append(Component.text("[Click Here]").clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl(url))).color(TC_SUCCESS));

        Bukkit.broadcast(content);
    }

    public static void broadcastDpsEventMessage(String message) {
        Bukkit.broadcast(DPS_PRE.append(Component.text(message).color(TC_LAYER1)));
    }

    public static void broadcastCaptureEventMessage(String message) {
        Bukkit.broadcast(KOTH_PRE.append(Component.text(message).color(TC_LAYER1)));
    }

    public static void broadcastConquestMessage(String message) {
        Bukkit.broadcast(CONQ_PRE.append(Component.text(message).color(TC_LAYER1)));
    }

    public static void printEotwMessage(String message) {
        Bukkit.broadcast(EOTW_PRE.append(Component.text(message).color(TC_LAYER1)));
    }

    public static void broadcastFactionRaidable(PlayerFaction faction) {
        Component component = Component.text(faction.getName()).color(TC_NAME)
                .appendSpace().append(Component.text("is now").color(TC_ERROR))
                .appendSpace().append(Component.text("RAID-ABLE").color(TC_DANGER).decorate(TextDecoration.BOLD));

        Bukkit.broadcast(component);
    }

    public static void printPlayerJoinedFaction(PlayerFaction faction, Player player) {
        Component component = Component.text(player.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("has").color(TC_LAYER1))
                        .appendSpace().append(Component.text("joined").color(TC_SUCCESS)
                        .appendSpace().append(Component.text("the faction").color(TC_LAYER1)));

        faction.sendMessage(component);
    }

    public static void printPlayerLeftFaction(PlayerFaction faction, Player player) {
        Component component = Component.text(player.getName()).color(TC_NAME)
                .appendSpace().append(Component.text("has").color(TC_LAYER1))
                .appendSpace().append(Component.text("left").color(TC_ERROR)
                        .appendSpace().append(Component.text("the faction").color(TC_LAYER1)));

        faction.sendMessage(component);
    }

    public static void printPlayerKickedFromFaction(PlayerFaction faction, Player player, String username) {
        Component component = Component.text(player.getName()).color(TC_NAME)
                .appendSpace().append(Component.text("has").color(TC_LAYER1))
                .appendSpace().append(Component.text("kicked").color(TC_ERROR)
                .appendSpace().append(Component.text(username).color(TC_LAYER1))
                .appendSpace().append(Component.text("from the faction").color(TC_LAYER1)));

        faction.sendMessage(component);
    }

    public static void printDepositReceived(Player player, double amount) {
        Component component = Component.text("$" + String.format("%.2f", amount)).color(NamedTextColor.DARK_GREEN)
                        .appendSpace().append(Component.text("has been").color(TC_LAYER1))
                        .appendSpace().append(Component.text("deposited").color(TC_SUCCESS))
                        .appendSpace().append(Component.text("to your personal balance").color(TC_LAYER1));

        player.sendMessage(component);
    }

    public static void printWithdrawReceived(Player player, double amount) {
        Component component = Component.text("$" + String.format("%.2f", amount)).color(NamedTextColor.DARK_GREEN)
                .appendSpace().append(Component.text("has been").color(TC_LAYER1))
                .appendSpace().append(Component.text("withdrawn").color(TC_ERROR))
                .appendSpace().append(Component.text("from your personal balance").color(TC_LAYER1));

        player.sendMessage(component);
    }

    public static void printFactionMemberOnline(PlayerFaction faction, String username) {
        Component component = Component.text("Member").color(TC_LAYER1)
                        .appendSpace().append(Component.text("Online").color(TC_SUCCESS))
                        .append(Component.text(":").color(TC_LAYER1))
                        .appendSpace().append(Component.text(username).color(TC_NAME));

        faction.sendMessage(component);
    }

    public static void printFactionMemberOffline(PlayerFaction faction, String username) {
        Component component = Component.text("Member").color(TC_LAYER1)
                .appendSpace().append(Component.text("Offline").color(TC_ERROR))
                .append(Component.text(":").color(TC_LAYER1))
                .appendSpace().append(Component.text(username).color(TC_NAME));

        faction.sendMessage(component);
    }

    public static void printCombatTag(Player player, long duration) {
        String convertedTime = Time.convertToHHMMSS(duration);
        Component component = Component.text("Combat Tag").color(TC_ERROR)
                        .append(Component.text(":").color(NamedTextColor.WHITE))
                        .appendSpace().append(Component.text(convertedTime).color(TC_INFO));

        player.sendMessage(component);
    }

    public static void printTimerCancelled(Player player, String timerName, String reason) {
        Component component = Component.text("Your").color(TC_ERROR)
                        .appendSpace().append(Component.text(timerName).color(TC_ERROR))
                        .appendSpace().append(Component.text("timer was cancelled because you").color(TC_ERROR))
                        .appendSpace().append(Component.text(reason).color(TC_ERROR));

        player.sendMessage(component);
    }

    public static void printLockedTimer(Player player, String itemName, long duration) {
        String asDecimal = Time.convertToDecimal(duration);
        Component component = Component.text("Your").color(TC_ERROR)
                        .appendSpace().append(Component.text(itemName)).color(TC_ERROR)
                        .appendSpace().append(Component.text("are locked for").color(TC_ERROR))
                        .appendSpace().append(Component.text(asDecimal).color(TC_ERROR).decorate(TextDecoration.BOLD))
                        .append(Component.text("s").decoration(TextDecoration.BOLD, TextDecoration.State.FALSE));

        player.sendMessage(component);
    }

    public static void printRallyUpdate(Player player, PlayerFaction playerFaction) {
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        String env = (player.getLocation().getWorld().getEnvironment().equals(World.Environment.NORMAL)
                ? "Overworld"
                : StringUtils.capitalize(player.getWorld().getEnvironment().name().toLowerCase(Locale.ROOT).replaceAll("_", " ")));

        String position = x + " " + y + " " + z + " " + env;

        Component component = Component.text(player.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("updated your faction rally to").color(TC_LAYER1))
                        .appendSpace().append(Component.text(position).color(TC_INFO));

        playerFaction.sendMessage(component);
    }

    public static void printPlayerInvite(Player player, PlayerFaction playerFaction, String username) {
        Component component = Component.text(player.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("has").color(TC_LAYER1))
                        .appendSpace().append(Component.text("invited").color(TC_SUCCESS))
                        .appendSpace().append(Component.text(username).color(TC_NAME))
                        .appendSpace().append(Component.text("to the faction").color(TC_LAYER1));

        playerFaction.sendMessage(component);
    }

    public static void printAllyRequest(Player player, PlayerFaction faction, PlayerFaction otherFaction) {
        Component senderComponent = Component.text(player.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("has invited").color(TC_LAYER1))
                        .appendSpace().append(Component.text(otherFaction.getName()).color(TC_INFO))
                        .appendSpace().append(Component.text("to form an alliance").color(TC_LAYER1));

        Component receiverComponent = Component.text(faction.getName()).color(TC_INFO)
                        .appendSpace().append(Component.text("has invited your faction to form an alliance").color(TC_LAYER1))
                        .appendSpace().append(Component.text("[Click to Accept]").color(TC_SUCCESS).clickEvent(ClickEvent.runCommand("/f ally accept " + faction.getName())));

        faction.sendMessage(senderComponent);
        otherFaction.sendMessage(receiverComponent);
    }

    public static void printAllianceFormed(PlayerFaction faction, PlayerFaction otherFaction) {
        Component senderComponent = Component.text("You are now allied to").color(TC_LAYER1)
                .appendSpace().append(Component.text(otherFaction.getName()).color(TC_INFO));

        Component receiverComponent = Component.text("You are now allied to").color(TC_LAYER1)
                .appendSpace().append(Component.text(faction.getName()).color(TC_INFO));

        faction.sendMessage(senderComponent);
        otherFaction.sendMessage(receiverComponent);
    }

    public static void printAllianceBroken(PlayerFaction faction, PlayerFaction otherFaction) {
        Component senderComponent = Component.text("You are no longer allied to").color(TC_LAYER1)
                .appendSpace().append(Component.text(otherFaction.getName()).color(TC_INFO));

        Component receiverComponent = Component.text("You are no longer allied to").color(TC_LAYER1)
                .appendSpace().append(Component.text(faction.getName()).color(TC_INFO));

        faction.sendMessage(senderComponent);
        otherFaction.sendMessage(receiverComponent);
    }

    public static void printNowAtMaxDTR(PlayerFaction playerFaction) {
        playerFaction.sendMessage(Component.text("Your faction is now at max DTR").color(TC_SUCCESS));
    }

    public static void printCanNotJoinWhileRaidable(PlayerFaction playerFaction, String username) {
        Component component = Component.text(username).color(TC_NAME)
                        .appendSpace().append(Component.text("will not be able to join until your faction is un-raidable").color(TC_ERROR));

        playerFaction.sendMessage(component);
    }

    public static void printCanNotJoinWhileFrozen(PlayerFaction playerFaction, String username) {
        Component component = Component.text(username).color(TC_NAME)
                .appendSpace().append(Component.text("will not be able to join until your faction power is thawed").color(TC_ERROR));

        playerFaction.sendMessage(component);
    }

    public static void printCanNotJoinFulLFaction(PlayerFaction playerFaction, String username) {
        Component component = Component.text(username).color(TC_NAME)
                .appendSpace().append(Component.text("will not be able to join until your faction unless a member leaves or is kicked").color(TC_ERROR));

        playerFaction.sendMessage(component);
    }

    public static void printReinviteWillBeConsumed(PlayerFaction playerFaction, String username) {
        if (playerFaction.getReinvites() > 0) {
            Component component = Component.text(username).color(TC_NAME)
                            .appendSpace().append(Component.text("has left the faction recently and will consume a re-invite upon joining again").color(TC_ERROR));

            playerFaction.sendMessage(component);
            return;
        }

        Component component = Component.text(username).color(TC_NAME)
                        .appendSpace().append(Component.text("will not be able to join the faction until you obtain more re-invites").color(TC_ERROR));

        playerFaction.sendMessage(component);
    }

    public static void printPlayerUninvite(Player player, PlayerFaction playerFaction, String username) {
        Component component = Component.text(player.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("has").color(TC_LAYER1))
                        .appendSpace().append(Component.text("uninvited").color(TC_ERROR))
                        .appendSpace().append(Component.text(username).color(TC_NAME))
                        .appendSpace().append(Component.text("from the faction").color(TC_LAYER1));

        playerFaction.sendMessage(component);
    }

    public static void printChatChannelChange(Player player, PlayerFaction.ChatChannel channel) {
        Component component = Component.text("You are now speaking in").color(TC_LAYER1)
                        .appendSpace().append(channel.getDisplayName());

        player.sendMessage(component);
    }

    public static void printCanNotFightInClaim(Player player, String claimName) {
        Component component = Component.text("You can not attack players inside").color(TC_LAYER1)
                        .appendSpace().append(Component.text(claimName).color(TC_ERROR))
                        .append(Component.text("'s claims").color(TC_LAYER1));

        player.sendMessage(component);
    }

    public static void printCanNotAttackFactionMembers(Player player) {
        Component component = Component.text("You can not attack").color(TC_ERROR)
                        .appendSpace().append(Component.text("Faction Members").color(NamedTextColor.DARK_GREEN));

        player.sendMessage(component);
    }

    public static void printAttackingAllyMember(Player player) {
        Component component = Component.text("You are attacking an").color(TC_ERROR)
                .appendSpace().append(Component.text("ally").color(NamedTextColor.BLUE));

        player.sendMessage(component);
    }

    public static void printMemberDeath(PlayerFaction faction, String memberName, double deducted) {
        Component component = Component.text("Member Death").color(TC_DANGER)
                        .append(Component.text(":").color(TC_NAME))
                        .appendSpace().append(Component.text(memberName).color(TC_NAME))
                        .appendNewline().append(Component.text("DTR Loss"))
                        .append(Component.text(":").color(TC_NAME))
                        .appendSpace().append(Component.text("-" + deducted).color(TC_NAME));

        faction.sendMessage(component);
    }

    public static void printFrozenPower(PlayerFaction faction, long duration) {
        String remaining = Time.convertToRemaining(duration);
        Component component = Component.text("Your faction power has been frozen for").color(TC_ERROR)
                        .appendSpace().append(Component.text(remaining).color(TC_INFO));

        faction.sendMessage(component);
    }

    public static void printDTRUpdate(PlayerFaction faction, double newDtr) {
        Component component = Component.text("Your faction DTR has been").color(TC_LAYER1)
                        .appendSpace().append(Component.text("updated").color(TC_LAYER2))
                        .appendSpace().append(Component.text("to").color(TC_LAYER1))
                        .appendSpace().append(Component.text(String.format("%.2f", newDtr)).color(TC_INFO));

        faction.sendMessage(component);
    }

    public static void printReinviteUpdate(PlayerFaction faction, int amount) {
        Component component = Component.text("Your faction re-invites has been").color(TC_LAYER1)
                .appendSpace().append(Component.text("updated").color(TC_LAYER2))
                .appendSpace().append(Component.text("to").color(TC_LAYER1))
                .appendSpace().append(Component.text(amount).color(TC_INFO));

        faction.sendMessage(component);
    }

    public static void printAnnouncement(PlayerFaction faction, String announcement) {
        Component component = Component.text("Faction Announcement").color(TC_LAYER2)
                        .append(Component.text(":").color(TC_LAYER1))
                        .appendSpace().append(Component.text(announcement).color(TC_INFO));

        faction.sendMessage(component);
    }

    public static void printPromotion(Player initiater, String updatedName, PlayerFaction faction, PlayerFaction.Rank rank) {
        Component component = Component.text(initiater.getName()).color(TC_LAYER1)
                        .appendSpace().append(Component.text("has").color(TC_LAYER1))
                        .appendSpace().append(Component.text("promoted").color(TC_SUCCESS))
                        .appendSpace().append(Component.text(updatedName).color(TC_NAME))
                        .appendSpace().append(Component.text("to").color(TC_LAYER1))
                        .appendSpace().append(rank.getDisplayName().color(TC_INFO));

        faction.sendMessage(component);
    }

    public static void printDemotion(Player initiator, String updatedName, PlayerFaction faction, PlayerFaction.Rank rank) {
        Component component = Component.text(initiator.getName()).color(TC_LAYER1)
                .appendSpace().append(Component.text("has").color(TC_LAYER1))
                .appendSpace().append(Component.text("demoted").color(TC_ERROR))
                .appendSpace().append(Component.text(updatedName).color(TC_NAME))
                .appendSpace().append(Component.text("to").color(TC_LAYER1))
                .appendSpace().append(rank.getDisplayName().color(TC_INFO));

        faction.sendMessage(component);
    }

    public static void printHomeUpdate(PlayerFaction faction, Player player, PLocatable location) {
        String position = Math.round(location.getX()) + ", " + Math.round(location.getY()) + ", " + Math.round(location.getZ());
        Component component = Component.text(player.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("has updated your faction home to").color(TC_LAYER1))
                        .appendSpace().append(Component.text(position).color(TC_INFO));

        faction.sendMessage(component);
    }

    public static void printHomeUnset(PlayerFaction faction, Player player) {
        Component component = Component.text(player.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("has unset your faction home").color(TC_LAYER1));

        faction.sendMessage(component);
    }

    public static void printReinviteConsumed(PlayerFaction faction, int newReinvites) {
        TextColor valueColor = (newReinvites <= 2 ? TC_ERROR : TC_INFO);
        Component component = Component.text("Remaining Faction Re-Invites:").color(TC_LAYER1)
                        .appendSpace().append(Component.text(newReinvites).color(valueColor));

        faction.sendMessage(component);
    }

    public static void printBalance(Player player, double amount) {
        String formatted = "$" + String.format("%.2f", amount);
        Component component = Component.text("Your balance").color(TC_LAYER2)
                        .append(Component.text(":").color(TC_LAYER1))
                                .appendSpace().append(Component.text(formatted).color(NamedTextColor.DARK_GREEN));

        player.sendMessage(component);
    }

    public static void printFactionWithdrawn(PlayerFaction faction, Player player, double amount) {
        String formatted = "$" + String.format("%.2f", amount);
        Component component = Component.text(player.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("has").color(TC_LAYER1))
                        .appendSpace().append(Component.text("withdrawn").color(TC_ERROR))
                        .appendSpace().append(Component.text(formatted).color(NamedTextColor.DARK_GREEN))
                        .appendSpace().append(Component.text("from the faction balance").color(TC_LAYER1));

        faction.sendMessage(component);
    }

    public static void printFactionDeposit(PlayerFaction faction, Player player, double amount) {
        String formatted = "$" + String.format("%.2f", amount);
        Component component = Component.text(player.getName()).color(TC_NAME)
                .appendSpace().append(Component.text("has").color(TC_LAYER1))
                .appendSpace().append(Component.text("deposited").color(TC_SUCCESS))
                .appendSpace().append(Component.text(formatted).color(NamedTextColor.DARK_GREEN))
                .appendSpace().append(Component.text("to the faction balance").color(TC_LAYER1));

        faction.sendMessage(component);
    }

    public static void printFactionBalanceSet(PlayerFaction faction, Player player, double amount) {
        String formatted = "$" + String.format("%.2f", amount);
        Component component = Component.text(player.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("has updated your faction balance to").color(TC_LAYER1))
                        .appendSpace().append(Component.text(formatted).color(NamedTextColor.DARK_GREEN));

        faction.sendMessage(component);
    }

    public static void printClassCancelled(Player player, IClass playerClass) {
        Component component = Component.text("Class Cancelled").color(TC_LAYER2)
                .append(Component.text(":").color(TC_LAYER1))
                .appendSpace().append(Component.text(playerClass.getName()).color(TC_NAME))
                .appendNewline().append(Component.text(playerClass.getDescription()).color(TC_LAYER1));

        player.sendMessage(component);
    }

    public static void printClassPreparing(Player player, IClass playerClass) {
        Component component = Component.text("Class Preparing").color(TC_LAYER2)
                .append(Component.text(":").color(TC_LAYER1))
                .appendSpace().append(Component.text(playerClass.getName()).color(TC_NAME))
                .appendNewline().append(Component.text(playerClass.getDescription()).color(TC_LAYER1));

        player.sendMessage(component);
    }

    public static void printClassActivated(Player player, IClass playerClass) {
        Component component = Component.text("Class Activated").color(TC_LAYER2)
                        .append(Component.text(":").color(TC_LAYER1))
                                .appendSpace().append(Component.text(playerClass.getName()).color(TC_NAME))
                        .appendNewline().append(Component.text(playerClass.getDescription()).color(TC_LAYER1));

        player.sendMessage(component);
    }

    public static void printClassDeactivated(Player player, IClass playerClass) {
        Component component = Component.text("Class Deactivated").color(TC_LAYER2)
                        .append(Component.text(":").color(TC_LAYER1))
                                .appendSpace().append(Component.text(playerClass.getName()).color(TC_NAME));

        player.sendMessage(component);
    }

    public static void printRogueUncloak(Player player, String reason) {
        Component component = Component.text("Uncloaked!").color(TC_DANGER).decorate(TextDecoration.BOLD)
                        .appendSpace().append(Component.text("You are now visible because you").color(TC_LAYER1).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                        .appendSpace().append(Component.text(reason).color(TC_LAYER1).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE));

        player.sendMessage(component);
    }

    public static void printSubclaimAdded(Subclaim subclaim, String addingUsername, String addedUsername) {
        Component component = Component.text(addingUsername).color(TC_NAME)
                        .appendSpace().append(Component.text("has added").color(TC_LAYER1))
                        .appendSpace().append(Component.text(addedUsername).color(TC_NAME))
                        .appendSpace().append(Component.text("to").color(TC_LAYER1))
                        .appendSpace().append(Component.text(subclaim.getName()).color(TC_LAYER2));

        subclaim.sendMessage(component);
    }

    public static void printSubclaimRemoved(Subclaim subclaim, String removingUsername, String removedUsername) {
        Component component = Component.text(removingUsername).color(TC_NAME)
                .appendSpace().append(Component.text("has removed").color(TC_LAYER1))
                .appendSpace().append(Component.text(removedUsername).color(TC_NAME))
                .appendSpace().append(Component.text("from").color(TC_LAYER1))
                .appendSpace().append(Component.text(subclaim.getName()).color(TC_LAYER2));

        subclaim.sendMessage(component);
    }

    public static void printSubclaimCreated(PlayerFaction faction, String creatorUsername, String subclaimName) {
        Component component = Component.text(creatorUsername).color(TC_NAME)
                        .appendSpace().append(Component.text("has created a new subclaim:").color(TC_LAYER1))
                        .appendSpace().append(Component.text(subclaimName).color(TC_NAME));

        faction.sendMessage(component);
    }

    public static void printSubclaimDeleted(Subclaim subclaim, String deletingUsername) {
        Component component = Component.text(deletingUsername).color(TC_NAME)
                .appendSpace().append(Component.text("has deleted a subclaim:").color(TC_LAYER1))
                .appendSpace().append(Component.text(subclaim.getName()).color(TC_NAME));

        subclaim.sendMessage(component);
    }

    public static Component getPublicFormat(PlayerFaction faction, Component displayName, long kills, String message, Player receiver) {
        if (faction == null) {
            return Component.text("[" + kills + "]", TC_INFO)
                    .append(displayName)
                    .append(Component.text(":", NamedTextColor.WHITE))
                    .appendSpace().append(Component.text(message, NamedTextColor.WHITE));
        }

        NamedTextColor textColor;
        if (faction.isMember(receiver.getUniqueId())) {
            textColor = NamedTextColor.DARK_GREEN;
        } else if (faction.isAlly(receiver)) {
            textColor = NamedTextColor.BLUE;
        } else {
            textColor = NamedTextColor.RED;
        }

        return Component.text("[" + faction.getName() + "]", textColor)
                .append(Component.text("[" + kills + "]", TC_INFO))
                .append(displayName).colorIfAbsent(NamedTextColor.WHITE)
                .append(Component.text(":", NamedTextColor.WHITE))
                .appendSpace().append(Component.text(message, NamedTextColor.WHITE));
    }

    public static Component getFactionFormat(Component displayName, String message) {
        return Component.text("[FC]").color(NamedTextColor.DARK_GREEN)
                .appendSpace().append(displayName).colorIfAbsent(NamedTextColor.WHITE)
                .append(Component.text(":").color(NamedTextColor.DARK_GREEN))
                .appendSpace().append(Component.text(message).color(NamedTextColor.DARK_GREEN));
    }

    public static Component getAllyFormat(Component displayName, String message) {
        return Component.text("[A]").color(NamedTextColor.BLUE)
                .appendSpace().append(displayName).colorIfAbsent(NamedTextColor.WHITE)
                .append(Component.text(":").color(NamedTextColor.BLUE))
                .appendSpace().append(Component.text(message).color(NamedTextColor.BLUE));
    }

    public static void printFocusedByFaction(PlayerFaction faction, Player player) {
        Component component = Component.text("You are being").color(TC_LAYER1)
                        .appendSpace().append(Component.text("focused").color(TC_ERROR))
                        .appendSpace().append(Component.text("by").color(TC_LAYER1))
                        .appendSpace().append(Component.text(faction.getName()).color(TC_INFO));

        player.sendMessage(component);
    }

    public static void printFocusing(PlayerFaction faction, Player initiated, Player player) {
        Component component = Component.text(initiated.getName()).color(TC_NAME)
                        .appendSpace().append(Component.text("wants to focus").color(TC_LAYER1))
                        .appendSpace().append(Component.text(player.getName()).color(NamedTextColor.LIGHT_PURPLE));

        faction.sendMessage(component);
    }

    public static void printNoLongerFocused(PlayerFaction faction, Player player) {
        Component component = Component.text("You are no longer being focused by").color(TC_LAYER1)
                        .appendSpace().append(Component.text(faction.getName()).color(TC_NAME));

        player.sendMessage(component);
    }

    public static void printBattleHornConsumed(Player hornUser, Player receiver, String hornName) {
        Component component = Component.text(hornUser.getName()).color(NamedTextColor.DARK_GREEN)
                        .appendSpace().append(Component.text("has sounded the").color(TC_LAYER1))
                        .appendSpace().append(Component.text(hornName).color(TC_INFO))
                        .appendSpace().append(Component.text("horn!").color(TC_LAYER1));

        receiver.sendMessage(component);
    }

    public static void printBattlepassProgress(Player player, BPObjective objective, int newValue) {
        Component component = Component.text("Battlepass Progress").color(TC_LAYER2).decorate(TextDecoration.BOLD)
                        .append(Component.text(":").color(TC_LAYER1).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                        .appendSpace().append(LegacyComponentSerializer.legacySection().deserialize(objective.getIcon().getDisplayName()).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                        .appendNewline().append(Component.text("Current Status").color(TC_INFO).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                        .append(Component.text(":").color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                        .appendSpace().append(Component.text(newValue + "/" + objective.getAmountRequirement()).color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE));

        player.sendMessage(component);
    }

    public static void printBattlepassComplete(Player player, BPObjective objective, double expMultiplier) {
        int multipliedExp = (int)Math.round(objective.getBaseExp() * expMultiplier);
        int diff = (multipliedExp - objective.getBaseExp());
        Component component = Component.text("Battlepass Completion").color(TC_LAYER2).decorate(TextDecoration.BOLD)
                        .append(Component.text(":").color(TC_LAYER1).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                        .appendSpace().append(LegacyComponentSerializer.legacySection().deserialize(objective.getIcon().getDisplayName()).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                        .appendNewline().append(Component.text("Reward").color(TC_INFO).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                        .append(Component.text(":").color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE))
                        .appendSpace().append(Component.text(objective.getBaseExp() + " EXP").decoration(TextDecoration.BOLD, TextDecoration.State.FALSE));

        if (expMultiplier > 0.0) {
            component = component.append(Component.text("(+" + diff + " Bonus)").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, TextDecoration.State.FALSE));
        }

        player.sendMessage(component);
    }

    public static Component getMythicIdentifier(IMythicItem mythicItem) {
        final String emblem = StringUtil.getMythicEmblem(mythicItem.getMaterial());
        return Component.text(emblem + " Mythic").color(TextColor.color(0xaf59ff));
    }

    public static void printGhostbladeKill(Player viewer, Player killer) {
        Component base = Component.text("Ghostblade").color(Ghostblade.GHOSTBLADE_COLOR)
                .appendSpace().append(Component.text(": You have been granted a").color(TC_MYTHIC_ABILITY_DESC))
                .appendSpace().append(Component.text("Speed Boost").color(Ghostblade.GHOSTBLADE_COLOR));

        if (viewer.getUniqueId().equals(killer.getUniqueId())) {
            base = base.appendSpace().append(Component.text("thanks to your Mythic Item").color(TC_MYTHIC_ABILITY_DESC));
            viewer.sendMessage(base);
            return;
        }

        base = base.appendSpace().append(Component.text("thanks to").color(TC_MYTHIC_ABILITY_DESC)).appendSpace().append(Component.text(killer.getName()).color(Ghostblade.GHOSTBLADE_COLOR));
        viewer.sendMessage(base);
    }

    public static void printGhostbladeRefresh(Player viewer, int seconds) {
        String formatted = seconds + (seconds > 1 ? "s" : "");
        viewer.sendMessage(Component.text("Ghostblade").color(Ghostblade.GHOSTBLADE_COLOR)
                .append(Component.text(":").color(TC_MYTHIC_ABILITY_DESC))
                .appendSpace().append(Component.text("Your speed has been refreshed by " + formatted)).color(TC_MYTHIC_ABILITY_DESC));
    }

    public static void printHullbreaker(Player viewer, int seconds) {
        Component component = Component.text("Hullbreaker").color(Hullbreaker.HULLBREAKER_COLOR)
                        .append(Component.text(":").color(TC_MYTHIC_ABILITY_DESC))
                        .appendSpace().append(Component.text("You have been given Resistance for " + seconds + " seconds.")).color(TC_MYTHIC_ABILITY_DESC);

        viewer.sendMessage(component);
    }

    public static void printCrimsonFangKill(Player viewer, int seconds) {
        Component component = Component.text("Crimson Fang").color(CrimsonFang.CRIMSON_FANG_COLOR)
                        .append(Component.text(":").color(TC_MYTHIC_ABILITY_DESC))
                        .appendSpace().append(Component.text("You have been given Regeneration for " + seconds + " seconds."));

        viewer.sendMessage(component);
    }

    public static void printNeptunesFuryImpaleVictim(Player viewer, double woundDamagePerTick, int woundSeconds) {
        Component component = Component.text("Serpent's Impaler").color(SerpentsImpaler.SERPENTS_IMPALER_COLOR)
                        .append(Component.text(":").color(TC_MYTHIC_ABILITY_DESC))
                        .appendSpace().append(Component.text("Impaled!").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, TextDecoration.State.TRUE))
                        .appendSpace().append(Component.text("You will bleed").color(NamedTextColor.YELLOW))
                        .appendSpace().append(Component.text(woundDamagePerTick + " ♥").color(NamedTextColor.RED))
                        .appendSpace().append(Component.text("over the span of").color(NamedTextColor.YELLOW))
                        .appendSpace().append(Component.text(woundSeconds + " seconds").color(NamedTextColor.BLUE));

        viewer.sendMessage(component);
    }

    public static void printNeptunesFuryImpale(Player viewer, int woundSeconds) {
        Component component = Component.text("Serpent's Impaler").color(SerpentsImpaler.SERPENTS_IMPALER_COLOR)
                        .append(Component.text(":").color(TC_MYTHIC_ABILITY_DESC))
                        .appendSpace().append(Component.text("You have inflicted wounds to your enemy that will cause them to bleed for"))
                        .appendSpace().append(Component.text(woundSeconds + " seconds").color(NamedTextColor.BLUE));

        viewer.sendMessage(component);
    }

    public static void printAdmiralsEmberAblazeVictim(Player viewer, int seconds) {
        Component component = Component.text("Admiral's Ember").color(AdmiralsEmber.ADMIRALS_EMBER_COLOR)
                        .append(Component.text(":").color(TC_MYTHIC_ABILITY_DESC))
                        .appendSpace().append(Component.text("Set Ablaze!").color(NamedTextColor.RED))
                        .appendSpace().append(Component.text("You will burn for").color(TC_MYTHIC_ABILITY_DESC))
                        .appendSpace().append(Component.text(seconds + " seconds").color(NamedTextColor.BLUE));

        viewer.sendMessage(component);
    }

    public static void printAdmiralsEmberAblazeAttacker(Player viewer, int seconds) {
        Component component = Component.text("Admiral's Ember").color(AdmiralsEmber.ADMIRALS_EMBER_COLOR)
                .append(Component.text(":").color(TC_MYTHIC_ABILITY_DESC))
                .appendSpace().append(Component.text("Set Ablaze!").color(NamedTextColor.RED))
                .appendSpace().append(Component.text("You have ignited all nearby enemies for").color(TC_MYTHIC_ABILITY_DESC))
                .appendSpace().append(Component.text(seconds + " seconds").color(NamedTextColor.BLUE));

        viewer.sendMessage(component);
    }

    public static void printAdmiralsEmberOverheatVictim(Player viewer, Player attacker, int seconds) {
        Component component = Component.text("Admiral's Ember").color(AdmiralsEmber.ADMIRALS_EMBER_COLOR)
                .append(Component.text(":").color(TC_MYTHIC_ABILITY_DESC))
                .appendSpace().append(Component.text("Overheating!").color(NamedTextColor.RED))
                .appendSpace().append(Component.text("You will burn for").color(TC_MYTHIC_ABILITY_DESC))
                .appendSpace().append(Component.text(seconds + " seconds").color(NamedTextColor.BLUE));

        viewer.sendMessage(component);
    }

    public static void printAdmiralsEmberOverheatAttacker(Player viewer, LivingEntity victim, int seconds) {
        Component component = Component.text("Admiral's Ember").color(AdmiralsEmber.ADMIRALS_EMBER_COLOR)
                .append(Component.text(":").color(TC_MYTHIC_ABILITY_DESC))
                .appendSpace().append(Component.text("Overheating!").color(NamedTextColor.RED))
                .appendSpace().append(Component.text("You have ignited").color(TC_MYTHIC_ABILITY_DESC))
                .appendSpace().append(Component.text(victim.getName()).color(NamedTextColor.RED))
                .appendSpace().append(Component.text("for").color(TC_MYTHIC_ABILITY_DESC))
                .appendSpace().append(Component.text(seconds + " seconds").color(NamedTextColor.BLUE));

        viewer.sendMessage(component);
    }

    public static void printStaffDeathMessage(Player viewer, String username, Location location) {
        Component component = Component.text("[Teleport to " + username + "'s Death Location]")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tp " + location.getBlockX() + " " + location.getY() + " " + location.getZ() + " " + Objects.requireNonNull(location.getWorld()).getName()))
                .hoverEvent(Component.text("Click to teleport").color(NamedTextColor.BLUE));

        viewer.sendMessage(component);
    }

    public static void printStaffCombatLogger(Player viewer, String username, Location location) {
        Component component = Component.text("[Teleport to " + username + "'s Combat Logger]")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.TRUE)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/tp " + location.getBlockX() + " " + location.getY() + " " + location.getZ() + " " + Objects.requireNonNull(location.getWorld()).getName()))
                .hoverEvent(Component.text("Click to teleport").color(NamedTextColor.BLUE));

        viewer.sendMessage(component);
    }

    public static void printFactionInfo(Factions plugin, Player player, IFaction faction) {
        // ◼⚠⬛⬆⬇▴▶▾
        // ⬆⬇➡
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);
        final DeathbanService dbs = (DeathbanService) plugin.getService(DeathbanService.class);
        final boolean access = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);
        Component output = Component.empty();
        Component comma = Component.text(",").color(TC_LAYER1).appendSpace();
        Component separator = Component.text(Strings.repeat("-", 10)).color(NamedTextColor.GRAY).decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.TRUE);

        if (faction instanceof final ServerFaction serverFaction) {
            output = output.append(separator);
            output = output.appendNewline().append(LegacyComponentSerializer.legacySection().deserialize(serverFaction.getDisplayName()))
                    .appendSpace().append(Component.text("[").color(NamedTextColor.GRAY)
                    .append(LegacyComponentSerializer.legacySection().deserialize(serverFaction.getFlag().getDisplayName()))
                    .append(Component.text("]").color(NamedTextColor.GRAY)));

            if (serverFaction.getHomeLocation() != null) {
                final PLocatable home = serverFaction.getHomeLocation();

                output = output.appendNewline().append(Component.text("Located At:").color(TC_LAYER1))
                        .appendSpace().append(Component.text((int)Math.round(home.getX()))).color(TC_INFO)
                        .appendSpace().append(comma)
                        .append(Component.text((int)Math.round(home.getY())).color(TC_INFO))
                        .appendSpace().append(comma)
                        .append(Component.text((int)Math.round(home.getZ())).color(TC_INFO))
                        .appendSpace().append(comma)
                        .append(Component.text(StringUtils.capitalize(home.getBukkitLocation().getWorld().getEnvironment().name().toLowerCase().replaceAll("_", " "))).color(TC_INFO));
            }

            output = output.appendNewline().append(separator);
            player.sendMessage(output);
            return;
        }

        final PlayerFaction playerFaction = (PlayerFaction)faction;
        final String unformattedDTR = String.format("%.2f", playerFaction.getDtr()) + "/" + String.format("%.2f", playerFaction.getMaxDtr());
        String homeLocation = "Unset";
        Component DTR;

        if (playerFaction.getHomeLocation() != null) {
            final int x = (int)Math.round(playerFaction.getHomeLocation().getX());
            final int y = (int)Math.round(playerFaction.getHomeLocation().getY());
            final int z = (int)Math.round(playerFaction.getHomeLocation().getZ());
            homeLocation = x + ", " + y + ", " + z;
        }

        if (playerFaction.getDtr() >= playerFaction.getMaxDtr()) {
            DTR = Component.text(unformattedDTR + " ➡").color(NamedTextColor.GREEN);
        } else if (!playerFaction.isRaidable() && playerFaction.getDtr() < playerFaction.getMaxDtr() && !playerFaction.isFrozen()) {
            DTR = Component.text(unformattedDTR + " ⬆").color(NamedTextColor.YELLOW);
        } else if (playerFaction.isRaidable() && playerFaction.isFrozen()) {
            DTR = Component.text(unformattedDTR + " ⚠").color(NamedTextColor.RED);
        } else if (playerFaction.isRaidable() && !playerFaction.isFrozen()) {
            DTR = Component.text(unformattedDTR + " ⬆").color(NamedTextColor.RED);
        } else if (playerFaction.isFrozen()) {
            DTR = Component.text(unformattedDTR + " ➡").color(NamedTextColor.YELLOW);
        } else {
            DTR = Component.text(unformattedDTR).color(NamedTextColor.YELLOW);
        }

        output = output.append(separator);
        output = output.appendNewline().append(Component.text(faction.getName()).color(TC_INFO))
                        .appendSpace().append(Component.text("[").color(NamedTextColor.GRAY))
                        .append(Component.text(playerFaction.getOnlineMembers().size()).color(NamedTextColor.GREEN))
                        .append(Component.text("/").color(NamedTextColor.GRAY))
                        .append(Component.text(playerFaction.getMembers().size()).color(NamedTextColor.GRAY))
                        .append(Component.text("]").color(NamedTextColor.GRAY))
                        .appendSpace().append(Component.text("|").color(TC_LAYER1))
                        .appendSpace().append(Component.text("Home:").color(TC_LAYER1))
                        .appendSpace().append(Component.text(homeLocation).color(TC_INFO));

        if (playerFaction.getAnnouncement() != null && (playerFaction.isMember(player.getUniqueId()) || access)) {
            output = output.appendNewline().append(Component.text("Announcement:").color(TC_LAYER1)).appendSpace().append(Component.text(playerFaction.getAnnouncement()).color(TC_INFO));
        }

        if (playerFaction.getRallyLocation() != null && (playerFaction.isMember(player.getUniqueId()) || access)) {
            final PLocatable rally = playerFaction.getRallyLocation();

            output = output.appendNewline().append(Component.text("Rally:").color(TC_LAYER1))
                    .appendSpace().append(Component.text((int)Math.round(rally.getX()))).color(TC_INFO)
                    .append(comma)
                    .append(Component.text((int)Math.round(rally.getY())).color(TC_INFO))
                    .append(comma)
                    .append(Component.text((int)Math.round(rally.getZ())).color(TC_INFO))
                    .append(comma)
                    .append(Component.text(StringUtils.capitalize(rally.getBukkitLocation().getWorld().getEnvironment().name().toLowerCase().replaceAll("_", " "))).color(TC_INFO));
        }

        output = output.appendNewline().append(Component.text("Balance:").color(TC_LAYER1))
                        .appendSpace().append(Component.text("$" + String.format("%.2f", playerFaction.getBalance())).color(TC_INFO))
                        .appendNewline().append(Component.text("Tokens:").color(TC_LAYER1))
                        .appendSpace().append(Component.text(playerFaction.getTokens()).color(TC_INFO))
                        .appendNewline().append(Component.text("Deaths until Raid-able:").color(TC_LAYER1))
                        .appendSpace().append(DTR);

        if (playerFaction.isFrozen()) {
            final FTimer timer = playerFaction.getTimer(ETimerType.FREEZE);

            output = output.appendNewline().append(Component.text("Frozen:").color(TC_LAYER1))
                            .appendSpace().append(Component.text(Time.convertToRemaining(timer.getRemaining())).color(TC_INFO));
        }

        if (plugin.getEventManager().isMajorEventActive()) {
            output = output.appendNewline().append(Component.text("Re-invites:").color(TC_LAYER1))
                    .appendSpace().append(Component.text(playerFaction.getReinvites()).color(TC_INFO));
        }

        if (playerFaction.getAlly() != null) {
            output = output.appendNewline().append(Component.text("Ally:").color(TC_LAYER1))
                    .appendSpace().append(Component.text(playerFaction.getAlly().getName(), TC_INFO).clickEvent(ClickEvent.runCommand("/f who " + playerFaction.getAlly().getName())).hoverEvent(Component.text("Click to learn more about " + playerFaction.getAlly().getName())));
        }

        final Component preQueryComponent = output;

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
                Component postQueryComponent = preQueryComponent;
                Map<PlayerFaction.Rank, List<String>> formattedNames = Maps.newHashMap();

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
                    String displayName = formattedNames.get(PlayerFaction.Rank.LEADER).size() > 1 ? "Leaders:" : "Leader:";
                    postQueryComponent = postQueryComponent.appendNewline().append(Component.text(displayName).color(TC_LAYER1)).appendSpace();

                    for (int i = 0; i < formattedNames.get(PlayerFaction.Rank.LEADER).size(); i++) {
                        final String entry = formattedNames.get(PlayerFaction.Rank.LEADER).get(i);
                        final boolean isLastEntry = i == formattedNames.get(PlayerFaction.Rank.LEADER).size() - 1;
                        postQueryComponent = postQueryComponent.append(LegacyComponentSerializer.legacySection().deserialize(entry));

                        if (!isLastEntry) {
                            postQueryComponent = postQueryComponent.append(Component.text(",").appendSpace());
                        }
                    }
                }

                if (formattedNames.containsKey(PlayerFaction.Rank.OFFICER) && !formattedNames.get(PlayerFaction.Rank.OFFICER).isEmpty()) {
                    String displayName = formattedNames.get(PlayerFaction.Rank.OFFICER).size() > 1 ? "Officers:" : "Officer:";
                    postQueryComponent = postQueryComponent.appendNewline().append(Component.text(displayName).color(TC_LAYER1)).appendSpace();

                    for (int i = 0; i < formattedNames.get(PlayerFaction.Rank.OFFICER).size(); i++) {
                        final String entry = formattedNames.get(PlayerFaction.Rank.OFFICER).get(i);
                        final boolean isLastEntry = i == formattedNames.get(PlayerFaction.Rank.OFFICER).size() - 1;
                        postQueryComponent = postQueryComponent.append(LegacyComponentSerializer.legacySection().deserialize(entry));

                        if (!isLastEntry) {
                            postQueryComponent = postQueryComponent.append(Component.text(",").appendSpace());
                        }
                    }
                }

                if (formattedNames.containsKey(PlayerFaction.Rank.MEMBER) && !formattedNames.get(PlayerFaction.Rank.MEMBER).isEmpty()) {
                    String displayName = formattedNames.get(PlayerFaction.Rank.MEMBER).size() > 1 ? "Members:" : "Member:";
                    postQueryComponent = postQueryComponent.appendNewline().append(Component.text(displayName).color(TC_LAYER1)).appendSpace();

                    for (int i = 0; i < formattedNames.get(PlayerFaction.Rank.MEMBER).size(); i++) {
                        final String entry = formattedNames.get(PlayerFaction.Rank.MEMBER).get(i);
                        final boolean isLastEntry = i == formattedNames.get(PlayerFaction.Rank.MEMBER).size() - 1;
                        postQueryComponent = postQueryComponent.append(LegacyComponentSerializer.legacySection().deserialize(entry));

                        if (!isLastEntry) {
                            postQueryComponent = postQueryComponent.append(Component.text(",").appendSpace());
                        }
                    }
                }

                postQueryComponent = postQueryComponent.appendNewline().append(separator);
                player.sendMessage(postQueryComponent);
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

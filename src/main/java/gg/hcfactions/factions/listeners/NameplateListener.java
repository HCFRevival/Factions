package gg.hcfactions.factions.listeners;

import com.lunarclient.bukkitapi.LunarClientAPI;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.faction.*;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public record NameplateListener(@Getter Factions plugin) implements Listener {
    private void sendNameplates(Player viewer) {
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);

        if (acs == null) {
            plugin.getAresLogger().error("failed to obtain Account Service");
            return;
        }

        if (!plugin.getConfiguration().useLegacyLunarAPI || !LunarClientAPI.getInstance().isRunningLunarClient(viewer)) {
            return;
        }

        final PlayerFaction selfFaction = plugin.getFactionManager().getPlayerFactionByPlayer(viewer);
        final AresAccount selfAccount = acs.getCachedAccount(viewer.getUniqueId());

        for (Player player : Bukkit.getOnlinePlayers()) {
            final PlayerFaction otherFaction = plugin.getFactionManager().getPlayerFactionByPlayer(player);
            final AresAccount otherAccount = acs.getCachedAccount(player.getUniqueId());

            /*
                self is a member of other (friendly)
                self is not a member of other, but they have a faction (enemy)
                self is not a member of any faction (red)
             */

            // send self to other players
            if (otherAccount.getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES) && LunarClientAPI.getInstance().isRunningLunarClient(player)) {
                if (otherFaction != null && otherFaction.isMember(viewer)) {
                    LunarClientAPI.getInstance().overrideNametag(viewer, FMessage.getFriendlyNametag(viewer.getName(), otherFaction.getName()), player);
                }

                else if (selfFaction != null) {
                    LunarClientAPI.getInstance().overrideNametag(viewer, FMessage.getEnemyNametag(viewer.getName(), selfFaction.getName()), player);
                }

                else if (!player.getUniqueId().equals(viewer.getUniqueId())) {
                    LunarClientAPI.getInstance().overrideNametag(viewer, List.of(ChatColor.RED + viewer.getName()), player);
                }
            }

            // self is not null, other player is member of self (friendly)
            // other faction is not null (enemy)
            // other faction is null and viewer is not player (red)
            // player is self (white)

            // send other players to self
            if (selfAccount.getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES) && LunarClientAPI.getInstance().isRunningLunarClient(viewer)) {
                if (selfFaction != null && selfFaction.isMember(player)) {
                    LunarClientAPI.getInstance().overrideNametag(player, FMessage.getFriendlyNametag(player.getName(), selfFaction.getName()), viewer);
                }

                else if (otherFaction != null) {
                    LunarClientAPI.getInstance().overrideNametag(player, FMessage.getEnemyNametag(player.getName(), otherFaction.getName()), viewer);
                }

                else if (!viewer.getUniqueId().equals(player.getUniqueId())) {
                    LunarClientAPI.getInstance().overrideNametag(player, List.of(ChatColor.RED + player.getName()), viewer);
                }

                else {
                    LunarClientAPI.getInstance().overrideNametag(player, List.of(ChatColor.RESET + player.getName()), viewer);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        new Scheduler(plugin).sync(() -> sendNameplates(player)).delay(10L).run();
    }

    @EventHandler
    public void onFactionCreate(FactionCreateEvent event) {
        if (event.isServerFaction()) {
            return;
        }

        final Player player = event.getPlayer();
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);

        if (acs == null) {
            plugin.getAresLogger().error("failed to obtain account service");
            return;
        }

        if (plugin.getConfiguration().useLegacyLunarAPI) {
            if (LunarClientAPI.getInstance().isRunningLunarClient(player) && acs.getCachedAccount(player.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                LunarClientAPI.getInstance().overrideNametag(player, FMessage.getFriendlyNametag(player.getName(), event.getFactionName()), player);
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (player.getUniqueId().equals(onlinePlayer.getUniqueId())) {
                    continue;
                }

                if (!LunarClientAPI.getInstance().isRunningLunarClient(onlinePlayer)) {
                    continue;
                }

                if (LunarClientAPI.getInstance().isRunningLunarClient(onlinePlayer) && acs.getCachedAccount(player.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                    LunarClientAPI.getInstance().overrideNametag(player, FMessage.getEnemyNametag(player.getName(), event.getFactionName()), onlinePlayer);
                }
            }
        }
    }

    @EventHandler
    public void onFactionDisband(FactionDisbandEvent event) {
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);

        if (acs == null) {
            plugin.getAresLogger().error("failed to obtain account service");
            return;
        }

        if (plugin.getConfiguration().useLegacyLunarAPI) {
            event.getFaction().getOnlineMembers().forEach(onlineMember -> {
                final Player bukkitMember = onlineMember.getBukkit();

                if (bukkitMember != null) {
                    if (LunarClientAPI.getInstance().isRunningLunarClient(bukkitMember) && acs.getCachedAccount(bukkitMember.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                        LunarClientAPI.getInstance().overrideNametag(bukkitMember, List.of(ChatColor.RED + bukkitMember.getName()), bukkitMember);
                    }

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if (onlinePlayer.getUniqueId().equals(bukkitMember.getUniqueId())) {
                            continue;
                        }

                        final PlayerFaction onlinePlayerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(onlinePlayer);

                        if (LunarClientAPI.getInstance().isRunningLunarClient(onlinePlayer) && acs.getCachedAccount(onlinePlayer.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                            LunarClientAPI.getInstance().overrideNametag(bukkitMember, List.of(ChatColor.RED + bukkitMember.getName()), onlinePlayer);
                        }

                        if (LunarClientAPI.getInstance().isRunningLunarClient(bukkitMember) && acs.getCachedAccount(bukkitMember.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                            if (onlinePlayerFaction != null) {
                                LunarClientAPI.getInstance().overrideNametag(onlinePlayer, FMessage.getEnemyNametag(onlinePlayer.getName(), onlinePlayerFaction.getName()), bukkitMember);
                            } else {
                                LunarClientAPI.getInstance().overrideNametag(onlinePlayer, List.of(ChatColor.RED + onlinePlayer.getName()), bukkitMember);
                            }
                        }
                    }
                }
            });
        }
    }

    @EventHandler
    public void onFactionMemberJoin(FactionJoinEvent event) {
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);

        if (acs == null) {
            plugin.getAresLogger().error("failed to obtain account service");
            return;
        }

        final Player player = event.getPlayer();

        if (plugin.getConfiguration().useLegacyLunarAPI) {
            if (LunarClientAPI.getInstance().isRunningLunarClient(player) && acs.getCachedAccount(player.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                LunarClientAPI.getInstance().overrideNametag(player, FMessage.getFriendlyNametag(player.getName(), event.getFaction().getName()), player);
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (player.getUniqueId().equals(onlinePlayer.getUniqueId())) {
                    continue;
                }

                if (LunarClientAPI.getInstance().isRunningLunarClient(player) && acs.getCachedAccount(player.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                    if (event.getFaction().isMember(onlinePlayer)) {
                        LunarClientAPI.getInstance().overrideNametag(onlinePlayer, FMessage.getFriendlyNametag(onlinePlayer.getName(), event.getFaction().getName()), player);
                    } else {
                        LunarClientAPI.getInstance().overrideNametag(onlinePlayer, FMessage.getEnemyNametag(onlinePlayer.getName(), event.getFaction().getName()), player);
                    }
                }

                if (LunarClientAPI.getInstance().isRunningLunarClient(onlinePlayer) && acs.getCachedAccount(onlinePlayer.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                    if (event.getFaction().isMember(onlinePlayer)) {
                        LunarClientAPI.getInstance().overrideNametag(player, FMessage.getFriendlyNametag(player.getName(), event.getFaction().getName()), onlinePlayer);
                    } else {
                        LunarClientAPI.getInstance().overrideNametag(player, FMessage.getEnemyNametag(player.getName(), event.getFaction().getName()), onlinePlayer);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFactionMemberLeave(FactionLeaveEvent event) {
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);

        if (acs == null) {
            plugin.getAresLogger().error("failed to obtain account service");
            return;
        }

        final Player player = event.getPlayer();

        if (plugin.getConfiguration().useLegacyLunarAPI) {
            if (LunarClientAPI.getInstance().isRunningLunarClient(player) && acs.getCachedAccount(player.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                LunarClientAPI.getInstance().overrideNametag(player, List.of(ChatColor.RESET + player.getName()), player);
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                final PlayerFaction onlinePlayerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(onlinePlayer);

                if (LunarClientAPI.getInstance().isRunningLunarClient(player) && acs.getCachedAccount(player.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                    if (onlinePlayerFaction != null) {
                        LunarClientAPI.getInstance().overrideNametag(onlinePlayer, FMessage.getEnemyNametag(onlinePlayer.getName(), onlinePlayerFaction.getName()), player);
                    } else {
                        LunarClientAPI.getInstance().overrideNametag(onlinePlayer, List.of(ChatColor.RED + onlinePlayer.getName()), player);
                    }
                }

                if (LunarClientAPI.getInstance().isRunningLunarClient(onlinePlayer)
                        && acs.getCachedAccount(onlinePlayer.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)
                        && !player.getUniqueId().equals(onlinePlayer.getUniqueId())) {

                    LunarClientAPI.getInstance().overrideNametag(player, List.of(ChatColor.RED + player.getName()), onlinePlayer);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFactionRename(FactionRenameEvent event) {
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);

        if (acs == null) {
            plugin.getAresLogger().error("failed to obtain account service");
            return;
        }

        if (event.isCancelled() || !(event.getFaction() instanceof final PlayerFaction pf)) {
            return;
        }

        if (plugin.getConfiguration().useLegacyLunarAPI) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!LunarClientAPI.getInstance().isRunningLunarClient(onlinePlayer)) {
                    continue;
                }

                pf.getOnlineMembers().forEach(onlineMember -> {
                    final Player bukkitMember = onlineMember.getBukkit();

                    if (bukkitMember != null && acs.getCachedAccount(onlinePlayer.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                        if (pf.isMember(onlinePlayer)) {
                            LunarClientAPI.getInstance().overrideNametag(bukkitMember, FMessage.getFriendlyNametag(bukkitMember.getName(), event.getNewName()), onlinePlayer);
                        } else {
                            LunarClientAPI.getInstance().overrideNametag(bukkitMember, FMessage.getEnemyNametag(bukkitMember.getName(), event.getNewName()), onlinePlayer);
                        }
                    }
                });
            }
        }
    }
}

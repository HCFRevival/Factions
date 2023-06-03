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
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);
        final PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (acs == null) {
            plugin.getAresLogger().error("failed to obtain Account Service");
            return;
        }

        if (plugin.getConfiguration().useLegacyLunarAPI) {
            new Scheduler(plugin).sync(() -> {
                if (LunarClientAPI.getInstance().isRunningLunarClient(player) && acs.getCachedAccount(player.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                    // assign self
                    if (playerFaction != null) {
                        LunarClientAPI.getInstance().overrideNametag(player, FMessage.getFriendlyNametag(player.getName(), playerFaction.getName()), player);
                    } else {
                        LunarClientAPI.getInstance().overrideNametag(player, List.of(ChatColor.RESET + player.getName()), player);
                    }

                    // send all online to self
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        final PlayerFaction onlinePlayerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(onlinePlayer);

                        if (!onlinePlayer.getUniqueId().equals(player.getUniqueId())
                                && LunarClientAPI.getInstance().isRunningLunarClient(player)
                                && acs.getCachedAccount(player.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {

                            if (onlinePlayerFaction != null) {
                                if (onlinePlayerFaction.isMember(player)) {
                                    LunarClientAPI.getInstance().overrideNametag(onlinePlayer, FMessage.getFriendlyNametag(onlinePlayer.getName(), onlinePlayerFaction.getName()), player);
                                } else {
                                    LunarClientAPI.getInstance().overrideNametag(onlinePlayer, FMessage.getEnemyNametag(onlinePlayer.getName(), onlinePlayerFaction.getName()), player);
                                }
                            } else {
                                LunarClientAPI.getInstance().overrideNametag(onlinePlayer, List.of(ChatColor.RED + onlinePlayer.getName()), player);
                            }
                        }
                    }
                }
            }).delay(20L).run();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                final PlayerFaction onlinePlayerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(onlinePlayer);

                // send all online players the joining players nameplate
                if (!onlinePlayer.getUniqueId().equals(player.getUniqueId())
                        && LunarClientAPI.getInstance().isRunningLunarClient(onlinePlayer)
                        && acs.getCachedAccount(onlinePlayer.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {

                    if (onlinePlayerFaction != null && onlinePlayerFaction.isMember(player)) {
                        LunarClientAPI.getInstance().overrideNametag(player, FMessage.getFriendlyNametag(player.getName(), onlinePlayerFaction.getName()), onlinePlayer);
                    } else if (playerFaction != null) {
                        LunarClientAPI.getInstance().overrideNametag(player, FMessage.getEnemyNametag(player.getName(), playerFaction.getName()), onlinePlayer);
                    } else {
                        LunarClientAPI.getInstance().overrideNametag(player, List.of(ChatColor.RED + player.getName()), onlinePlayer);
                    }
                }
            }
        }
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

                if (!acs.getCachedAccount(onlinePlayer.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
                    continue;
                }

                LunarClientAPI.getInstance().overrideNametag(player, FMessage.getEnemyNametag(player.getName(), event.getFactionName()), onlinePlayer);
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
                        LunarClientAPI.getInstance().overrideNametag(bukkitMember, List.of(ChatColor.RESET + bukkitMember.getName()), bukkitMember);
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

                if (LunarClientAPI.getInstance().isRunningLunarClient(onlinePlayer) && acs.getCachedAccount(onlinePlayer.getUniqueId()).getSettings().isEnabled(AresAccount.Settings.SettingValue.LUNAR_FACTION_NAMEPLATES)) {
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

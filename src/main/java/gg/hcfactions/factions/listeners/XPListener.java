package gg.hcfactions.factions.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.KOTHCaptureEvent;
import gg.hcfactions.factions.listeners.events.player.BattlepassCompleteEvent;
import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import gg.hcfactions.libs.bukkit.services.impl.xp.XPService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class XPListener implements Listener {
    @Getter public final Factions plugin;
    @Getter public final Map<UUID, Long> loginBonusGranted;
    @Getter public final Map<UUID, Long> loginBonusTimestamps;
    @Getter public final BukkitTask loginBonusTickingTask;

    public XPListener(Factions plugin) {
        this.plugin = plugin;
        this.loginBonusGranted = Maps.newConcurrentMap();
        this.loginBonusTimestamps = Maps.newConcurrentMap();

        this.loginBonusTickingTask = new Scheduler(plugin).sync(() -> {
            if (!plugin.getConfiguration().isXpEnabled()) {
                return;
            }

            // clear out granted bonus entries
            final List<UUID> toRemove = Lists.newArrayList();

            loginBonusGranted.forEach((uid, timestamp) -> {
                if (timestamp <= Time.now()) {
                    toRemove.add(uid);
                }
            });

            toRemove.forEach(loginBonusGranted::remove);
            toRemove.clear();

            // clear our login bonus timestamps and reward accordingly
            loginBonusTimestamps.forEach((uid, timestamp) -> {
                final long sec = (Time.now() - timestamp) / 1000L;

                if (sec >= plugin.getConfiguration().getLoginBonusRequiredTime() && !loginBonusGranted.containsKey(uid)) {
                    toRemove.add(uid);
                }
            });

            toRemove.forEach(uid -> {
                final Player player = Bukkit.getPlayer(uid);
                final XPService xpService = (XPService) plugin.getService(XPService.class);

                loginBonusTimestamps.remove(uid);
                loginBonusGranted.put(uid, (Time.now() + (86400*1000L)));

                if (xpService == null) {
                    plugin.getAresLogger().error("Failed to obtain XP Service");
                    return;
                }

                xpService.getExecutor().addExperience(uid, plugin.getConfiguration().getLoginRewardXp(), "Daily Login Bonus", new Promise() {
                    @Override
                    public void resolve() {}

                    @Override
                    public void reject(String s) {
                        if (player != null) {
                            player.sendMessage(ChatColor.RED + "Failed to grant your daily login bonus: " + s);
                        }

                        plugin.getAresLogger().error("Failed to grant daily login bonus: " + s);
                    }
                });
            });
        }).repeat(60 * 20L, 60 * 20L).run();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfiguration().isXpEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        loginBonusTimestamps.put(player.getUniqueId(), Time.now());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getConfiguration().isXpEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        loginBonusTimestamps.remove(player.getUniqueId());
    }

    @EventHandler
    public void onEventCapture(KOTHCaptureEvent event) {
        if (!plugin.getConfiguration().isXpEnabled()) {
            return;
        }

        final KOTHEvent koth = event.getEvent();
        final XPService xpService = (XPService) plugin.getService(XPService.class);
        final PlayerFaction capturingFaction = event.getCapturingFaction();

        if (xpService == null) {
            plugin.getAresLogger().error("Failed to grant KOTH capture EXP: XP Service not found");
            return;
        }

        final int xpReward = (koth instanceof PalaceEvent) ? plugin.getConfiguration().getPalaceCaptureRewardXp() : plugin.getConfiguration().getKothCaptureRewardXp();

        capturingFaction.getOnlineMembers().forEach(onlineMember ->
                xpService.getExecutor().addExperience(
                        onlineMember.getUniqueId(),
                        xpReward,
                        "Captured " + ChatColor.stripColor(koth.getDisplayName()),
                        new Promise() {
                            @Override
                            public void resolve() {}

                            @Override
                            public void reject(String s) {
                                plugin.getAresLogger().error("Failed to grant Palace capture EXP for " + onlineMember.getUniqueId().toString() + ": " + s);
                            }
                        }));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfiguration().isXpEnabled()) {
            return;
        }

        final Player player = event.getEntity();
        final Player killer = player.getKiller();

        if (killer == null) {
            return;
        }

        final XPService xpService = (XPService) plugin.getService(XPService.class);

        if (xpService == null) {
            plugin.getAresLogger().error("Failed to grant Player Kill EXP for " + killer.getUniqueId() + ": XP Service not found");
            return;
        }

        xpService.getExecutor().addExperience(killer.getUniqueId(), plugin.getConfiguration().getPlayerKillRewardXp(), "Killed " + player.getName(), new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                plugin.getAresLogger().error("Failed to grant Player Kill EXP for " + killer.getUniqueId() + ": " + s);
            }
        });
    }

    @EventHandler
    public void onDragonSlain(EntityDeathEvent event) {
        if (!plugin.getConfiguration().isXpEnabled()) {
            return;
        }

        final LivingEntity entity = event.getEntity();

        if (!(entity instanceof EnderDragon)) {
            return;
        }

        final Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        final XPService xpService = (XPService) plugin.getService(XPService.class);

        if (xpService == null) {
            plugin.getAresLogger().error("Failed to grant Player Kill EXP for " + killer.getUniqueId() + ": XP Service not found");
            return;
        }

        xpService.getExecutor().addExperience(killer.getUniqueId(), plugin.getConfiguration().getDragonKillRewardXp(), "Killed the Ender Dragon", new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                plugin.getAresLogger().error("Failed to grant Dragon Kill EXP for " + killer.getUniqueId() + ": " + s);
            }
        });
    }

    @EventHandler
    public void onBattleplassComplete(BattlepassCompleteEvent event) {
        if (!plugin.getConfiguration().isXpEnabled()) {
            return;
        }

        final Player player = event.getPlayer();
        final BPObjective obj = event.getObjective();
        final RankService rankService = (RankService) plugin.getService(RankService.class);
        final XPService xpService = (XPService) plugin.getService(XPService.class);
        double multiplier = 1.0;

        if (xpService == null) {
            plugin.getAresLogger().error("Failed to grant Battlepass EXP for " + player.getUniqueId().toString() + ": XP Service not found");
            return;
        }

        if (rankService != null) {
            final AresRank rank = rankService.getHighestRank(player);

            if (rank != null) {
                multiplier = plugin.getBattlepassManager().getRankMultipliers().getOrDefault(rank, 1.0);
            }
        }

        final int reward = (int)Math.round(obj.getBaseExp() * multiplier);

        xpService.getExecutor().addExperience(player.getUniqueId(), reward, "Completed " + ChatColor.stripColor(obj.getIcon().getDisplayName()), new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to grant your Battlepass EXP: " + s);
                plugin.getAresLogger().error("Failed to grant Battlepass EXP for " + player.getUniqueId() + ": " + s);
            }
        });
    }
}

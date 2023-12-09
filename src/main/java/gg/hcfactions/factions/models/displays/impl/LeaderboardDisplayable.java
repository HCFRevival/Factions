package gg.hcfactions.factions.models.displays.impl;

import com.google.common.collect.Maps;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.cx.hologram.EHologramOrder;
import gg.hcfactions.cx.hologram.impl.Hologram;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.displays.IDisplayable;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.impl.PlayerStatHolder;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class LeaderboardDisplayable implements IDisplayable {
    @Getter public Factions plugin;
    @Getter public UUID uniqueId;
    @Getter public final EStatisticType type;
    @Getter public final String title;
    @Getter public PLocatable origin;
    @Getter public Hologram hologram;

    public LeaderboardDisplayable(Factions plugin, EStatisticType type, PLocatable origin) {
        this(plugin, UUID.randomUUID(), type, origin);
    }

    public LeaderboardDisplayable(Factions plugin, UUID uniqueId, EStatisticType type, PLocatable origin) {
        this.plugin = plugin;
        this.uniqueId = uniqueId;
        this.type = type;
        this.origin = origin;
        this.title = ChatColor.GOLD + "" + ChatColor.BOLD + type.getDisplayName() + " Leaderboard";

        final CXService cxs = (CXService) plugin.getService(CXService.class);

        if (cxs == null) {
            plugin.getAresLogger().error("Attempted to spawn in a Leaderboard Hologram without CX Service enabled");
            return;
        }

        this.hologram = new Hologram(cxs, -1, Collections.singletonList(title), origin, EHologramOrder.DESCENDING);
    }

    public void update() {
        final AccountService acs = (AccountService) plugin.getService(AccountService.class);

        if (acs == null) {
            plugin.getAresLogger().error("attempted to update entries without Account service initialized");
            return;
        }

        if (!canBeSeen()) {
            return;
        }

        plugin.getStatsManager().getPlayerLeaderboard(type, playerStatHolders -> {
            final List<PlayerStatHolder> sublist = playerStatHolders.subList(0, Math.min(playerStatHolders.size(), 10));

            new Scheduler(plugin).async(() -> {
                final Map<PlayerStatHolder, String> usernames = Maps.newHashMap();

                for (PlayerStatHolder holder : sublist) {
                    final AresAccount account = acs.getAccount(holder.getUniqueId());

                    if (account != null) {
                        usernames.put(holder, account.getUsername());
                        continue;
                    }

                    plugin.getAresLogger().error("failed to load username during display update: " + holder.getUniqueId().toString());
                }

                new Scheduler(plugin).sync(() -> {
                    int pos = 1;

                    for (PlayerStatHolder holder : sublist) {
                        if (!usernames.containsKey(holder)) {
                            continue;
                        }

                        final String username = usernames.get(holder);
                        String value;

                        if (type.equals(EStatisticType.PLAYTIME)) {
                            value = Time.convertToRemaining(holder.getStatistic(type));
                        } else if (type.equals(EStatisticType.LONGSHOT)) {
                            value = holder.getStatistic(type) + " blocks";
                        } else {
                            value = holder.getStatistic(type) + "";
                        }

                        hologram.updateLine(pos, ChatColor.GOLD + "" + pos + ChatColor.YELLOW + ". " + username + " " + ChatColor.BLUE + "(" + value + ")");
                        pos += 1;
                    }
                }).run();
            }).run();
        });
    }

    public void spawn() {
        if (hologram == null) {
            plugin.getAresLogger().error("attempted to spawn a null hologram");
            return;
        }

        hologram.spawn();
        update(); // initial entry update
    }

    public void despawn() {
        if (hologram == null) {
            plugin.getAresLogger().error("attempted to despawn a null hologram");
            return;
        }

        hologram.despawn();
        hologram = null;
    }
}

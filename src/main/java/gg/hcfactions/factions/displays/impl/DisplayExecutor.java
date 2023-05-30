package gg.hcfactions.factions.displays.impl;

import gg.hcfactions.factions.displays.DisplayManager;
import gg.hcfactions.factions.displays.IDisplayExecutor;
import gg.hcfactions.factions.models.displays.IDisplayable;
import gg.hcfactions.factions.models.displays.impl.LeaderboardDisplayable;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class DisplayExecutor implements IDisplayExecutor {
    @Getter public DisplayManager manager;

    @Override
    public void createLeaderboardDisplay(Player player, EStatisticType type) {
        final LeaderboardDisplayable displayable = new LeaderboardDisplayable(manager.getPlugin(), type, new PLocatable(player));

        displayable.spawn();

        manager.getDisplayRepository().add(displayable);
        manager.saveDisplays();
        player.sendMessage(ChatColor.GREEN + "Leaderboard display created");
    }

    @Override
    public void deleteDisplay(Player player, double radius, Promise promise) {
        final PLocatable playerLoc = new PLocatable(player);
        final List<IDisplayable> displays = manager.getDisplayRepository()
                .stream()
                .filter(d -> d.getOrigin().getDistance(playerLoc) < radius && d.getOrigin().getDistance(playerLoc) > 0)
                .collect(Collectors.toList());

        if (displays.isEmpty()) {
            promise.reject("No displays found within " + radius + " of your position");
            return;
        }

        displays.forEach(d -> {
            d.despawn();
            manager.deleteDisplays(d);
        });

        manager.getDisplayRepository().removeAll(displays);
        promise.resolve();
    }
}

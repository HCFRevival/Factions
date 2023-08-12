package gg.hcfactions.factions.listeners;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.horn.IBattleHorn;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;
import java.util.UUID;

public record HornListener(@Getter Factions plugin) implements Listener {
    @EventHandler
    public void onBattleHornConsume(PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (!item.getType().equals(Material.GOAT_HORN)) {
            return;
        }

        final CustomItemService cis = (CustomItemService) plugin.getService(CustomItemService.class);

        if (cis == null) {
            return;
        }

        cis.getItem(item).ifPresent(customItem -> {
            if (customItem instanceof final IBattleHorn battleHorn) {
                final PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(player);
                final Set<UUID> affectedEntityIds = Sets.newHashSet();

                affectedEntityIds.add(player.getUniqueId());

                if (playerFaction != null) {
                    FactionUtil.getNearbyFriendlies(plugin, player, 32.0).forEach(friendly -> affectedEntityIds.add(friendly.getUniqueId())); // TODO: Make configurable
                }

                battleHorn.apply(player, affectedEntityIds);
                event.setItem(new ItemStack(Material.AIR));
            }
        });
    }
}

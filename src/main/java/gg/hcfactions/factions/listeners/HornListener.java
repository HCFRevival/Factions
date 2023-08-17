package gg.hcfactions.factions.listeners;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.horn.IBattleHorn;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class HornListener implements Listener {
    @Getter public final Factions plugin;
    private final Map<UUID, Long> hornCooldowns;

    public HornListener(Factions plugin) {
        this.plugin = plugin;
        this.hornCooldowns = Maps.newConcurrentMap();
    }

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
                final Set<UUID> affectedEntityIds = Sets.newHashSet();
                final PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(player);
                final UUID factionId;
                final UUID playerId = player.getUniqueId();

                affectedEntityIds.add(player.getUniqueId());

                if (hornCooldowns.containsKey(player.getUniqueId())) {
                    final long expire = hornCooldowns.get(player.getUniqueId());
                    FMessage.printLockedTimer(player, "Battle Horns", (expire - Time.now()));
                    event.setCancelled(true);
                    return;
                }

                if (playerFaction != null) {
                    factionId = playerFaction.getUniqueId();

                    if (hornCooldowns.containsKey(playerFaction.getUniqueId())) {
                        final long expire = hornCooldowns.get(playerFaction.getUniqueId());
                        FMessage.printLockedTimer(player, "Battle Horns", (expire - Time.now()));
                        event.setCancelled(true);
                        return;
                    }

                    hornCooldowns.put(playerFaction.getUniqueId(), (Time.now() + (300 * 1000L))); // TODO: Make configurable
                    new Scheduler(plugin).sync(() -> hornCooldowns.remove(factionId)).delay(300*20L).run(); // TODO: Make configurable

                    FactionUtil.getNearbyFriendlies(plugin, player, 32.0).forEach(friendly -> affectedEntityIds.add(friendly.getUniqueId())); // TODO: Make configurable
                }

                battleHorn.apply(player, affectedEntityIds);
                event.setItem(new ItemStack(Material.AIR));

                hornCooldowns.put(player.getUniqueId(), (Time.now() + (300 * 1000L))); // TODO: Make configurable
                new Scheduler(plugin).sync(() -> hornCooldowns.remove(playerId)).delay(300*20L).run(); // TODO: Make configurable
            }
        });
    }
}

package gg.hcfactions.factions.listeners;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.CombatLoggerDeathEvent;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Set;
import java.util.UUID;

public final class CosmeticsListener implements Listener {
    @Getter Factions plugin;
    @Getter public Set<UUID> recentlyPerformedHeadLookup;

    public CosmeticsListener(Factions plugin) {
        this.plugin = plugin;
        this.recentlyPerformedHeadLookup = Sets.newConcurrentHashSet();
    }

    private ItemStack getPlayerHead(UUID uniqueId) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) item.getItemMeta();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);

        if (meta != null) {
            meta.setOwningPlayer(offlinePlayer);
            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onDropPlayerHead(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final ItemStack head = getPlayerHead(player.getUniqueId());
        event.getDrops().add(head);
    }

    @EventHandler
    public void onDropCombatLoggerHead(CombatLoggerDeathEvent event) {
        final UUID ownerUniqueId = event.getLogger().getOwnerId();
        final ItemStack head = getPlayerHead(ownerUniqueId);
        event.getLogger().getLoggerInventory().add(head);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final Block block = event.getClickedBlock();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (block == null || !(block.getState() instanceof final Skull skull)) {
            return;
        }

        if (recentlyPerformedHeadLookup.contains(player.getUniqueId())) {
            return;
        }

        if (skull.getOwningPlayer() == null) {
            return;
        }

        recentlyPerformedHeadLookup.add(uniqueId);
        new Scheduler(plugin).sync(() -> recentlyPerformedHeadLookup.remove(uniqueId)).delay(20L).run();

        player.sendMessage(ChatColor.YELLOW + skull.getOwningPlayer().getName() + "'s Head");
    }
}

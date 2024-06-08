package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.Crowbar;
import gg.hcfactions.factions.listeners.events.player.EventShopTransactionEvent;
import gg.hcfactions.factions.listeners.events.player.ShopTransactionEvent;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public record CrowbarListener(@Getter Factions plugin) implements Listener {
    private void handleCrowbarPurchase(ShopTransactionEvent event) {
        if (!(event.getItem().getType().equals(Material.DIAMOND_HOE))) {
            return;
        }

        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();

        if (
                meta == null
                        || meta.displayName() == null
                        || !PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(meta.displayName())).equalsIgnoreCase("Crowbar")
        ) {
            plugin.getAresLogger().error("Skip #1: {}", PlainTextComponentSerializer.plainText().serialize(meta.displayName()));
            return;
        }

        CustomItemService cis = (CustomItemService)plugin.getService(CustomItemService.class);
        if (cis == null) {
            plugin.getAresLogger().error("Skip #2");
            return;
        }

        cis.getItem(Crowbar.class).ifPresentOrElse(crowbarCustomItem -> {
            event.setItem(crowbarCustomItem.getItem());
            plugin.getAresLogger().info("Updated PDC for Crowbar");
        }, () -> plugin.getAresLogger().error("Failed to apply PDC for Crowbar"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final ItemStack hand = event.getItem();
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (!event.useInteractedBlock().equals(Event.Result.ALLOW)) {
            return;
        }

        if (hand == null) {
            return;
        }

        if (block == null || (!block.getType().equals(Material.END_PORTAL_FRAME) && !block.getType().equals(Material.SPAWNER))) {
            return;
        }

        final CustomItemService cis = (CustomItemService)plugin.getService(CustomItemService.class);
        if (cis == null) {
            return;
        }

        cis.getItem(hand).ifPresent(customItem -> {
            if (customItem instanceof Crowbar) {
                plugin.getCrowbarManager().getExecutor().useCrowbar(player, hand, block, new Promise() {
                    @Override
                    public void resolve() {}

                    @Override
                    public void reject(String s) {
                        player.sendMessage(ChatColor.RED + "Failed to use Crowbar: " + s);
                    }
                });
            }
        });
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Block block = event.getBlock();
        final ItemStack item = event.getItemInHand();

        if (!block.getType().equals(Material.SPAWNER)) {
            return;
        }

        if (item.getItemMeta() == null) {
            return;
        }

        final String displayName = item.getItemMeta().getDisplayName();
        final BlockState state = block.getState();
        final CreatureSpawner spawner = (CreatureSpawner) state;

        final String entityTypeName = net.md_5.bungee.api.ChatColor.stripColor(displayName).toUpperCase()
                .replaceAll(" ", "_")
                .replaceAll("_SPAWNER", "");

        final EntityType type;
        try {
            type = EntityType.valueOf(entityTypeName);
        } catch (IllegalArgumentException e) {
            return;
        }

        spawner.setSpawnedType(type);
        state.update();
    }

    @EventHandler
    public void onShopTransaction(ShopTransactionEvent event) {
        if (!(event.getTransactionType().equals(ShopTransactionEvent.ETransactionType.BUY))) {
            return;
        }

        handleCrowbarPurchase(event);
    }

    @EventHandler
    public void onEventShopTransaction(EventShopTransactionEvent event) {
        handleCrowbarPurchase(event);
    }
}

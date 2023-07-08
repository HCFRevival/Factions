package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.EventBuilderWand;
import gg.hcfactions.factions.models.events.builder.ECEBuildStep;
import gg.hcfactions.factions.models.events.builder.ICaptureEventBuilder;
import gg.hcfactions.factions.models.events.builder.IEventBuilder;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.libs.bukkit.events.impl.ProcessedChatEvent;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public record EventBuilderListener(@Getter Factions plugin) implements Listener {
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (event.isCancelled()) {
            return;
        }

        if (!block.getType().equals(Material.CHEST)) {
            return;
        }

        if (!player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            return;
        }

        for (PalaceEvent palaceEvent : plugin.getEventManager().getPalaceEvents()) {
            if (palaceEvent.getLootChests().isEmpty()) {
                continue;
            }

            for (PalaceLootChest lootChest : palaceEvent.getLootChests()) {
                if (lootChest.getLocation().getX() == block.getX()
                        && lootChest.getLocation().getY() == block.getY()
                        && lootChest.getLocation().getZ() == block.getZ()
                        && lootChest.getLocation().getWorldName().equalsIgnoreCase(block.getWorld().getName())) {
                    player.sendMessage(ChatColor.YELLOW + "Palace chest deleted");
                    palaceEvent.getLootChests().remove(lootChest);
                    plugin.getEventManager().saveCaptureEvent(palaceEvent);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        plugin.getEventManager().getBuilderManager().getBuilderRepository().removeIf(b -> b.getBuilderId().equals(player.getUniqueId()));
    }

    @EventHandler
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final String message = event.getMessage();

        plugin.getEventManager().getBuilderManager().getBuilder(player).ifPresent(builder -> {
            if (builder instanceof final ICaptureEventBuilder ceb) {
                if (ceb.getCurrentStep().equals(ECEBuildStep.DISPLAY_NAME)) {
                    ceb.setDisplayName(message);
                    event.setCancelled(true);
                }

                else if (ceb.getCurrentStep().equals(ECEBuildStep.OWNER)) {
                    ceb.setOwner(message);
                    event.setCancelled(true);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final ItemStack hand = player.getInventory().getItemInMainHand();
        final Action action = event.getAction();

        if (block == null) {
            return;
        }

        if (!action.equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        if (!player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            return;
        }

        final Optional<IEventBuilder> builder = plugin.getEventManager().getBuilderManager().getBuilder(player);

        if (builder.isEmpty()) {
            return;
        }

        final CustomItemService cis = (CustomItemService) plugin.getService(CustomItemService.class);

        if (cis == null) {
            return;
        }

        cis.getItem(hand).ifPresent(ci -> {
            if (ci instanceof EventBuilderWand) {
                final IEventBuilder b = builder.get();

                if (b instanceof final ICaptureEventBuilder ceb) {
                    if (ceb.getCurrentStep().equals(ECEBuildStep.CORNER_A)) {
                        ceb.setCornerA(new BLocatable(block));
                        event.setCancelled(true);
                    }

                    else if (ceb.getCurrentStep().equals(ECEBuildStep.CORNER_B)) {
                        ceb.setCornerB(new BLocatable(block));
                        event.setCancelled(true);
                    }
                }
            }
        });
    }
}

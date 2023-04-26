package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.FactionSubclaimAxe;
import gg.hcfactions.factions.models.claim.EClaimPillarType;
import gg.hcfactions.factions.models.claim.impl.ClaimBuilder;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.subclaim.Subclaim;
import gg.hcfactions.factions.models.subclaim.SubclaimBuilder;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.events.CustomItemInteractEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public record SubclaimBuilderListener(@Getter Factions plugin) implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final SubclaimBuilder builder = plugin.getSubclaimManager().getBuilderManager().getSubclaimBuilder(player);

        if (builder != null) {
            plugin.getSubclaimManager().getBuilderManager().getBuilderRepository().remove(builder);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItemDrop().getItemStack();

        if (!item.getType().equals(Material.STICK)) {
            return;
        }

        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        if (customItemService == null) {
            return;
        }

        customItemService.getItem(item).ifPresent(customItem -> {
            if (customItem instanceof FactionSubclaimAxe) {
                final ClaimBuilder builder = plugin.getClaimManager().getClaimBuilderManager().getClaimBuilder(player);

                if (builder != null) {
                    builder.reset();
                    plugin.getClaimManager().getClaimBuilderManager().getBuilderRepository().remove(builder);
                }
            }
        });
    }

    @EventHandler
    public void onCustomItemInteract(CustomItemInteractEvent event) {
        if (!(event.getItem() instanceof final FactionSubclaimAxe subclaimCreatorAxe)) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();
        final boolean sneaking = player.isSneaking();
        final SubclaimBuilder builder = plugin.getSubclaimManager().getBuilderManager().getSubclaimBuilder(player);

        event.setCancelled(true);

        if (builder == null) {
            player.getInventory().remove(subclaimCreatorAxe.getItem());
            return;
        }

        if (action.equals(Action.LEFT_CLICK_BLOCK) && block != null) {
            builder.setCorner(new BLocatable(block), EClaimPillarType.A);
            return;
        }

        if (action.equals(Action.RIGHT_CLICK_BLOCK) && block != null) {
            builder.setCorner(new BLocatable(block), EClaimPillarType.B);
            return;
        }

        if (action.equals(Action.RIGHT_CLICK_AIR) && sneaking) {
            builder.reset();
            return;
        }

        if (action.equals(Action.LEFT_CLICK_AIR) && sneaking) {
            builder.build(new FailablePromise<>() {
                @Override
                public void resolve(Subclaim subclaim) {
                    final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

                    if (factionPlayer != null) {
                        factionPlayer.hideClaimPillars();
                    }

                    plugin.getSubclaimManager().getSubclaimRepository().add(subclaim);
                    plugin.getSubclaimManager().getBuilderManager().getBuilderRepository().remove(builder);

                    player.getInventory().remove(subclaimCreatorAxe.getItem());
                    player.sendMessage(FMessage.SUCCESS + "Claim has been created");

                    FMessage.printSubclaimCreated(builder.getOwner(), player.getName(), subclaim.getName());
                }

                @Override
                public void reject(String s) {
                    player.sendMessage(FMessage.ERROR + s);
                }
            });
        }
    }
}

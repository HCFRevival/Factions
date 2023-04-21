package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.FactionClaimingStick;
import gg.hcfactions.factions.models.claim.EClaimPillarType;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.claim.impl.ClaimBuilder;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.events.CustomItemInteractEvent;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public record ClaimBuilderListener(@Getter Factions plugin) implements Listener {
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final ClaimBuilder builder = plugin.getClaimManager().getClaimBuilderManager().getClaimBuilder(player);

        if (builder != null) {
            plugin.getClaimManager().getClaimBuilderManager().getBuilderRepository().remove(builder);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItemDrop().getItemStack();

        if (!item.getType().equals(Material.STICK)) {
            return;
        }

        final CustomItemService customItemService = (CustomItemService) plugin.getService(CustomItemService.class);

        if (customItemService == null) {
            return;
        }

        customItemService.getItem(item).ifPresent(customItem -> {
            if (customItem instanceof FactionClaimingStick) {
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
        if (!(event.getItem() instanceof final FactionClaimingStick claimingStick)) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();
        final boolean sneaking = player.isSneaking();
        final ClaimBuilder builder = plugin.getClaimManager().getClaimBuilderManager().getClaimBuilder(player);

        event.setCancelled(true);

        if (builder == null) {
            player.getInventory().remove(claimingStick.getItem());
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
                public void resolve(Claim claim) {
                    final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());
                    final IFaction owner = builder.getFaction();

                    if (factionPlayer != null) {
                        factionPlayer.hideClaimPillars();
                    }

                    plugin.getClaimManager().getClaimRepository().add(claim);
                    plugin.getClaimManager().getClaimBuilderManager().getBuilderRepository().remove(builder);

                    player.getInventory().remove(claimingStick.getItem());

                    if (owner instanceof PlayerFaction) {
                        final PlayerFaction playerFaction = (PlayerFaction) builder.getFaction();

                        playerFaction.sendMessage(FMessage.LAYER_1 + "" + claim.getSize()[0] + " x " + claim.getSize()[1] +
                                FMessage.LAYER_2 + " claim created by " + ChatColor.DARK_GREEN + player.getName() + FMessage.LAYER_2 +
                                " for " + ChatColor.GREEN + "$" + String.format("%.2f", claim.getCost()));

                        if (factionPlayer != null && factionPlayer.hasTimer(ETimerType.PROTECTION) && claim.isInside(new PLocatable(player), false)) {
                            factionPlayer.finishTimer(ETimerType.PROTECTION);
                            player.sendMessage(FMessage.ERROR + "Your protection was removed because you created a claim while standing inside of it");
                        }
                    }

                    player.sendMessage(FMessage.SUCCESS + "Claim has been created");
                }

                @Override
                public void reject(String s) {
                    player.sendMessage(FMessage.ERROR + s);
                }
            });
        }
    }
}

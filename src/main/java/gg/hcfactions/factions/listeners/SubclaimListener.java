package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.subclaim.ChestSubclaim;
import gg.hcfactions.factions.models.subclaim.Subclaim;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public record SubclaimListener(@Getter Factions plugin) implements Listener {
    /**
     * Handles block modifications of subclaims
     *
     * @param cancellable Cancellable
     * @param player      Player
     * @param block       Block
     */
    private void handleBlockModification(Cancellable cancellable, Player player, Block block) {
        final Subclaim subclaim = plugin.getSubclaimManager().getSubclaimAt(new BLocatable(block));
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (subclaim == null) {
            return;
        }

        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionById(subclaim.getOwner());

        if (faction == null) {
            return;
        }

        if (!subclaim.canAccess(player) && !faction.isRaidable() && faction.isMember(player.getUniqueId()) && !bypass) {
            player.sendMessage(FMessage.ERROR + FError.F_SUBCLAIM_NO_ACCESS.getErrorDescription());
            cancellable.setCancelled(true);
        }
    }

    /**
     * Handles block modifications of sign subclaims
     *
     * @param cancellable Cancellable
     * @param player      Player
     * @param block       Block
     */
    private void handleSignSubclaimModification(Cancellable cancellable, Player player, Block block) {
        if (cancellable.isCancelled()) {
            return;
        }

        final ChestSubclaim subclaim = plugin.getSubclaimManager().getExecutor().findChestSubclaimAt(block);

        if (subclaim == null) {
            return;
        }

        final BLocatable locatable = new BLocatable(block);
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(locatable);
        final Subclaim insideSubclaim = plugin.getSubclaimManager().getSubclaimAt(locatable);

        if (insideClaim == null) {
            return;
        }

        final PlayerFaction insideClaimOwner = plugin.getFactionManager().getPlayerFactionById(insideClaim.getOwner());

        if (!player.hasPermission(FPermissions.P_FACTIONS_ADMIN) &&
                        insideClaimOwner != null &&
                        !insideClaimOwner.isRaidable() &&
                        !subclaim.canAccess(player) &&
                        insideClaimOwner.isMember(player.getUniqueId()) &&
                        (insideSubclaim == null || insideSubclaim.canAccess(player))) {

            player.sendMessage(FMessage.ERROR + FError.F_SUBCLAIM_NO_ACCESS.getErrorDescription());
            cancellable.setCancelled(true);

        }
    }

    /**
     * Handles preventing block modifications
     *
     * @param event BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        handleBlockModification(event, event.getPlayer(), event.getBlock());
    }

    /**
     * Handles preventing block modifications
     *
     * @param event BlockPlaceEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockModification(event, event.getPlayer(), event.getBlock());
    }

    /**
     * Handles preventing block modifications
     *
     * @param event PlayerBucketFillEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketFillEvent event) {
        handleBlockModification(event, event.getPlayer(), event.getBlockClicked());
    }

    /**
     * Handles preventing block modifications
     *
     * @param event PlayerBucketEmptyEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        handleBlockModification(event, event.getPlayer(), event.getBlockClicked());
    }

    /**
     * Handles block breaking for sign subclaimed chests
     *
     * @param event BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignSubclaimBreak(BlockBreakEvent event) {
        handleSignSubclaimModification(event, event.getPlayer(), event.getBlock());
    }

    /**
     * Handles block interactions for sign subclaimed chests
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignSubclaimInteract(PlayerInteractEvent event) {
        handleSignSubclaimModification(event, event.getPlayer(), event.getClickedBlock());
    }

    /**
     * Handles preventing interacting in subclaims
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Keeping this as the deprecated value because it's a bitch to swap to the new one
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (bypass) {
            return;
        }

        if (block == null || (block.getType().equals(Material.AIR))) {
            return;
        }

        if (!block.getType().isInteractable()) {
            return;
        }

        handleBlockModification(event, player, block);
    }

    /**
     * Handles creating a new sign subclaimed chest
     *
     * @param event SignChangeEvent
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final Sign sign = (Sign)block.getState();
        final String[] lines = event.getLines();
        final String l0 = lines[0];

        if (!block.getType().name().endsWith("_WALL_SIGN")) {
            return;
        }

        if (!l0.equalsIgnoreCase("[Subclaim]")) {
            return;
        }

        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (faction == null) {
            player.sendMessage(ChatColor.RED + "You are not in a faction");
            return;
        }

        final Claim inside = plugin.getClaimManager().getClaimAt(new PLocatable(player));

        if (inside == null) {
            player.sendMessage(ChatColor.RED + "This block is not inside your faction's claims");
            return;
        }

        if (!inside.getOwner().equals(faction.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "This land is not owned by your faction");
            return;
        }

        plugin.getSubclaimManager().getExecutor().createChestSubclaim(player, block, new Promise() {
            @Override
            public void resolve() {
                event.setLine(0, ChatColor.AQUA + "[Subclaim]");
                sign.setEditable(false);

                player.sendMessage(FMessage.SUCCESS + "Chest has been subclaimed successfully");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + s);
            }
        });
    }
}

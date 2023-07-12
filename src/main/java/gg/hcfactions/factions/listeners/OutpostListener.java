package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@AllArgsConstructor
public final class OutpostListener implements Listener {
    @Getter public final Factions plugin;

    /**
     * Listens for block break events inside Outpost claims
     * then tries to drop the block naturally
     *
     * @param event BlockBreakEvent
     */
    @EventHandler (priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        final Claim insideClaim = plugin.getClaimManager().getClaimAt(new BLocatable(block));

        if (insideClaim == null) {
            return;
        }

        final ServerFaction serverFaction = plugin.getFactionManager().getServerFactionById(insideClaim.getOwner());

        if (serverFaction == null || !serverFaction.getFlag().equals(ServerFaction.Flag.OUTPOST)) {
            return;
        }

        plugin.getOutpostManager().getOutpostBlock(block.getType()).ifPresent(ob -> {
            // cancel event so other listeners down the line can ignore
            event.setCancelled(true);

            // set to placeholder block
            block.breakNaturally(player.getInventory().getItemInMainHand());
            new Scheduler(plugin).sync(() -> block.setType(Material.COBBLESTONE)).run();

            // add to mined blocks so it can be reset shortly
            ob.getMinedBlocks().add(new BLocatable(block));
        });
    }
}

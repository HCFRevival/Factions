package gg.hcfactions.factions.listeners;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

public record FoundOreListener(@Getter Factions plugin) implements Listener {
    private static final ImmutableList<Material> TRACKED_ORES = ImmutableList.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.ANCIENT_DEBRIS);
    private static final String FD_PREFIX = ChatColor.RESET + "[!] ";

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (!TRACKED_ORES.contains(block.getType())) {
            return;
        }

        if (block.hasMetadata("player_placed")) {
            return;
        }

        final int found = countOres(Lists.newArrayList(), block, 0);

        if (block.getType().equals(Material.DIAMOND_ORE) || block.getType().equals(Material.DEEPSLATE_DIAMOND_ORE)) {
            Bukkit.broadcastMessage(FD_PREFIX + ChatColor.AQUA + player.getName() + " found " + found + " Diamond Ore");
            return;
        }

        if (block.getType().equals(Material.ANCIENT_DEBRIS)) {
            Bukkit.broadcastMessage(FD_PREFIX + net.md_5.bungee.api.ChatColor.of("#654641") + player.getName() + " found " + found + " Ancient Debris");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (!TRACKED_ORES.contains(block.getType())) {
            return;
        }

        block.setMetadata("player_placed", new FixedMetadataValue(plugin, player.getUniqueId()));
    }

    private int countOres(List<Block> tracked, Block origin, int iter) {
        tracked.add(origin);
        iter += 1;

        if (iter > 32) {
            return tracked.size();
        }

        for (BlockFace face : BlockFace.values()) {
            if (face.equals(BlockFace.SELF)) {
                continue;
            }

            final Block otherBlock = origin.getRelative(face);

            if (!otherBlock.getType().equals(origin.getType())) {
                continue;
            }

            if (tracked.contains(otherBlock)) {
                continue;
            }

            countOres(tracked, otherBlock, iter);
        }

        return tracked.size();
    }
}

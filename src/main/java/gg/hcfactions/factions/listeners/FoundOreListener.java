package gg.hcfactions.factions.listeners;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import java.util.Set;

public final class FoundOreListener implements Listener {
    private static final ImmutableList<Material> TRACKED_ORES = ImmutableList.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.ANCIENT_DEBRIS);
    private static final String FD_PREFIX = ChatColor.RESET + "[!] ";

    @Getter public Factions plugin;
    @Getter public Set<Block> foundOres;

    public FoundOreListener(Factions plugin) {
        this.plugin = plugin;
        this.foundOres = Sets.newConcurrentHashSet();
    }

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

        if (foundOres.contains(block)) {
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

        // cleanup method to prevent a large heap
        if (foundOres.size() > 500) {
            plugin.getAresLogger().warn("cleared found ores cache (exceeds 500 entries)");
            foundOres.clear();
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
        foundOres.add(origin);

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

            if (foundOres.contains(otherBlock)) {
                continue;
            }

            countOres(tracked, otherBlock, iter);
        }

        return tracked.size();
    }
}

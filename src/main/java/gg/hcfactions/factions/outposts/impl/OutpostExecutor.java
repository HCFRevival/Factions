package gg.hcfactions.factions.outposts.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.models.outpost.IOutpostBlock;
import gg.hcfactions.factions.models.outpost.impl.OutpostBlock;
import gg.hcfactions.factions.outposts.IOutpostExecutor;
import gg.hcfactions.factions.outposts.OutpostManager;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.base.util.Time;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@AllArgsConstructor
public final class OutpostExecutor implements IOutpostExecutor {
    @Getter public OutpostManager manager;

    @Override
    public void addBlock(String materialName, Promise promise) {
        final Material material;

        try {
            material = Material.valueOf(materialName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            promise.reject("Invalid material");
            return;
        }

        final Optional<OutpostBlock> blockQuery = manager.getOutpostBlock(material);

        if (blockQuery.isPresent()) {
            promise.reject("Material is already in use");
            return;
        }

        final OutpostBlock block = new OutpostBlock(manager.getPlugin(), material);
        manager.getBlockRepository().add(block);
        manager.saveBlock(block);

        promise.resolve();
    }

    @Override
    public void removeBlock(String materialName, Promise promise) {
        final Material material;

        try {
            material = Material.valueOf(materialName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            promise.reject("Invalid material");
            return;
        }

        final Optional<OutpostBlock> blockQuery = manager.getOutpostBlock(material);

        if (blockQuery.isEmpty()) {
            promise.reject("Outpost block not found");
            return;
        }

        final OutpostBlock block = blockQuery.get();

        manager.getBlockRepository().remove(block);
        manager.deleteBlock(block);

        promise.resolve();
    }

    @Override
    public void listBlocks(Player player) {
        final String INDENT = ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.BLUE;

        if (manager.getBlockRepository().isEmpty()) {
            player.sendMessage(ChatColor.RED + "Could not find any Outpost blocks");
            return;
        }

        final List<String> res = Lists.newArrayList();

        manager.getBlockRepository().forEach(ob -> {
            final String prettyName = StringUtils.capitalize(ob.getMaterial().name().toLowerCase(Locale.ROOT).replaceAll("_", " "));
            // - Diamond ore (24 broken)
            res.add(INDENT + ChatColor.BLUE + prettyName + ChatColor.GOLD + " (" + ChatColor.YELLOW + ob.getMinedBlocks().size() + " broken" + ChatColor.GOLD + ")");
        });

        player.sendMessage(ChatColor.DARK_PURPLE + "Outpost Blocks" + ChatColor.LIGHT_PURPLE + " (" + ChatColor.GOLD + manager.getBlockRepository().size() + ChatColor.LIGHT_PURPLE + ")");
        res.forEach(player::sendMessage);
    }

    @Override
    public void rescheduleReset(Player player, String duration, Promise promise) {
        final long ms;

        try {
            ms = Time.parseTime(duration);
        } catch (NumberFormatException e) {
            promise.reject("Invalid time format");
            return;
        }

        final long newTime = Time.now() + ms;
        manager.setNextRestockTime(newTime);
        promise.resolve();
    }

    @Override
    public void resetBlock(String materialName, Promise promise) {
        final Material material;

        try {
            material = Material.valueOf(materialName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            promise.reject("Invalid material");
            return;
        }

        final Optional<OutpostBlock> blockQuery = manager.getOutpostBlock(material);

        if (blockQuery.isEmpty()) {
            promise.reject("Outpost block not found");
            return;
        }

        final OutpostBlock block = blockQuery.get();

        block.reset();
        promise.resolve();
    }

    @Override
    public void resetAllBlocks(Promise promise) {
        if (manager.getBlockRepository().isEmpty()) {
            promise.reject("Could not find any Outpost Blocks");
            return;
        }

        manager.getBlockRepository().forEach(IOutpostBlock::reset);
        promise.resolve();
    }
}

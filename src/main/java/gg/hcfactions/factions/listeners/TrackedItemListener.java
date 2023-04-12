package gg.hcfactions.factions.listeners;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.CombatLoggerDeathEvent;
import gg.hcfactions.factions.models.stats.impl.item.TrackedBow;
import gg.hcfactions.factions.models.stats.impl.item.TrackedPickaxe;
import gg.hcfactions.factions.models.stats.impl.item.TrackedWeapon;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Objects;

public record TrackedItemListener(@Getter Factions plugin) implements Listener {
    private static final ImmutableList<Material> TRACKED_ORES = ImmutableList.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.ANCIENT_DEBRIS);

    /**
     * Handles adding kills to bows and swords
     * @param event PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player slain = event.getEntity();

        if (slain.getKiller() != null) {
            final Player killer = slain.getKiller();
            final ItemStack hand = killer.getInventory().getItemInMainHand();

            if (TrackedWeapon.VALID_ITEMS.contains(hand.getType())) {
                final TrackedWeapon trackedSword = new TrackedWeapon().fromItem(hand);
                trackedSword.addEntry(killer.getName(), slain.getName());
                trackedSword.updateItem();
                return;
            }

            if (TrackedBow.VALID_ITEMS.contains(hand.getType())) {
                final TrackedBow trackedBow = new TrackedBow().fromItem(hand);
                trackedBow.addEntry(killer.getName(), slain.getName());
                trackedBow.updateItem();
            }
        }
    }

    /**
     * Handles adding kills to bows and swords with combat loggers
     * @param event CombatLoggerDeathEvent
     */
    @EventHandler
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        if (event.getKiller() != null) {
            final Player killer = event.getKiller();
            final ItemStack hand = killer.getInventory().getItemInMainHand();

            if (TrackedWeapon.VALID_ITEMS.contains(hand.getType())) {
                final TrackedWeapon trackedSword = new TrackedWeapon().fromItem(hand);
                trackedSword.addEntry(killer.getName(), event.getLogger().getOwnerUsername());
                trackedSword.updateItem();
                return;
            }

            if (TrackedBow.VALID_ITEMS.contains(hand.getType())) {
                final TrackedBow trackedBow = new TrackedBow().fromItem(hand);
                trackedBow.addEntry(killer.getName(), event.getLogger().getOwnerUsername());
                trackedBow.updateItem();
            }
        }
    }

    /**
     * Handles adding blocks mined to item lore for pickaxes
     * @param event BlockBreakEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();

        if (event.isCancelled()) {
            return;
        }

        if (!TrackedPickaxe.VALID_ITEMS.contains(hand.getType())) {
            return;
        }

        final boolean silktouch = Objects.requireNonNull(hand.getItemMeta()).hasEnchant(Enchantment.SILK_TOUCH);

        if (!TRACKED_ORES.contains(block.getType())) {
            return;
        }

        final List<Material> canSilk = Lists.newArrayList(TRACKED_ORES);
        canSilk.remove(Material.ANCIENT_DEBRIS);

        if (silktouch && canSilk.contains(block.getType())) {
            return;
        }

        if (block.hasMetadata("player_placed")) {
            return;
        }

        final TrackedPickaxe trackedPickaxe = new TrackedPickaxe().fromItem(hand);
        final int amount = block.getDrops(hand).size();

        trackedPickaxe.addOre(block.getType(), amount);
        trackedPickaxe.updateItem();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Block block = event.getBlockPlaced();
        final Material type = block.getType();

        if (!(type.equals(Material.ANCIENT_DEBRIS))) {
            return;
        }

        if (block.hasMetadata("player_placed")) {
            return;
        }

        block.setMetadata("player_placed", new FixedMetadataValue(plugin, true));
    }
}

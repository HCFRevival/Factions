package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.world.BossSpawnEvent;
import gg.hcfactions.factions.models.boss.impl.BossGiant;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public record OutpostListener(@Getter Factions plugin) implements Listener {
    /**
     * Listens for block break events inside Outpost claims
     * then tries to drop the block naturally
     *
     * @param event BlockBreakEvent
     */
    @EventHandler(priority = EventPriority.LOWEST)
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
            new Scheduler(plugin).sync(() -> block.setType(Material.COBBLED_DEEPSLATE)).run();

            // add to mined blocks so it can be reset shortly
            ob.getMinedBlocks().add(new BLocatable(block));
        });
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onOutpostGiantSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final LivingEntity entity = event.getEntity();

        if (!(entity instanceof Giant)) {
            return;
        }

        final String flag = entity.getPersistentDataContainer().get(plugin.getNamespacedKey(), PersistentDataType.STRING);
        if (flag != null && flag.equalsIgnoreCase("boss")) {
            return;
        }

        event.setCancelled(true);

        final BossGiant giant = new BossGiant(plugin, event.getLocation());
        final BossSpawnEvent spawnEvent = new BossSpawnEvent(giant, event.getLocation());
        Bukkit.getPluginManager().callEvent(spawnEvent);

        if (spawnEvent.isCancelled()) {
            return;
        }

        giant.spawn();
    }

    @EventHandler
    public void onOutpostEntitySpawn(CreatureSpawnEvent event) {
        final LivingEntity entity = event.getEntity();
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(new PLocatable(entity));

        if (insideClaim == null) {
            return;
        }

        final ServerFaction serverFaction = plugin.getFactionManager().getServerFactionById(insideClaim.getOwner());

        if (serverFaction == null || !serverFaction.getFlag().equals(ServerFaction.Flag.OUTPOST)) {
            return;
        }

        if (
                entity.getType().equals(EntityType.ENDERMAN)
                        || entity.getType().equals(EntityType.CREEPER)
                        || entity.getType().equals(EntityType.ARMOR_STAND)
                        || entity.getType().equals(EntityType.VILLAGER)
                        || entity.getType().equals(EntityType.GIANT)) {

            return;
        }

        // skip noMerge mobs from having attributes applied
        if (entity.getPersistentDataContainer().has(plugin.getNamespacedKey(), PersistentDataType.STRING)) {
            if (Objects.requireNonNull(entity.getPersistentDataContainer().get(plugin.getNamespacedKey(), PersistentDataType.STRING)).equalsIgnoreCase("noMerge")) {
                return;
            }
        }

        // prevent natural spawns inside outpost claims
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
            event.setCancelled(true);
            return;
        }

        if (entity instanceof Mob && entity.getEquipment() != null) {
            entity.getEquipment().setHelmet(plugin.getOutpostManager().getRandomHelmet());
            entity.getEquipment().setChestplate(plugin.getOutpostManager().getRandomChestplate());
            entity.getEquipment().setLeggings(plugin.getOutpostManager().getRandomLeggings());
            entity.getEquipment().setBoots(plugin.getOutpostManager().getRandomBoots());

            entity.getEquipment().setHelmetDropChance(0f);
            entity.getEquipment().setChestplateDropChance(0f);
            entity.getEquipment().setLeggingsDropChance(0f);
            entity.getEquipment().setBootsDropChance(0f);
            entity.getEquipment().setItemInMainHandDropChance(0f);

            if (entity.getType().equals(EntityType.SKELETON) || entity.getType().equals(EntityType.STRAY)) {
                entity.getEquipment().setItemInMainHand(plugin.getOutpostManager().getEnhancedBow(false));
            }

            if (entity.getType().equals(EntityType.PILLAGER)) {
                entity.getEquipment().setItemInMainHand(plugin.getOutpostManager().getEnhancedBow(true));
            }

            if (entity.getType().equals(EntityType.ZOMBIE)) {
                entity.getEquipment().setItemInMainHand(plugin.getOutpostManager().getRandomWeapon());
            }
        }

        final AttributeInstance attackDamageInst = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        final AttributeInstance moveSpeedInst = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        final AttributeInstance knockbackResInst = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        final AttributeInstance maxHealthInst = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (attackDamageInst != null) {
            attackDamageInst.setBaseValue(2.5);
        }

        if (moveSpeedInst != null) {
            moveSpeedInst.setBaseValue(0.4);
        }

        if (knockbackResInst != null) {
            knockbackResInst.setBaseValue(0.5);
        }

        if (maxHealthInst != null) {
            maxHealthInst.setBaseValue(32.0);
            entity.setHealth(32.0);
        }
    }
}

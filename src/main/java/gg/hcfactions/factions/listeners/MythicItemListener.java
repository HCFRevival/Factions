package gg.hcfactions.factions.listeners;

import com.google.common.collect.Sets;
import gg.hcfactions.cx.event.EnchantLimitApplyEvent;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class MythicItemListener implements Listener {
    public record ProjectileTracker(@Getter Player shooter, @Getter UUID projectileId, @Getter ItemStack item, @Getter ICustomItem customItem) {}

    @Getter public final Factions plugin;
    @Getter public final CustomItemService customItemService;
    private final Set<ProjectileTracker> trackerRepository;

    public MythicItemListener(Factions plugin) {
        this.plugin = plugin;
        this.customItemService = (CustomItemService) plugin.getService(CustomItemService.class);
        this.trackerRepository = Sets.newConcurrentHashSet();

        if (customItemService == null) {
            plugin.getAresLogger().error("Failed to obtain custom item service");
        }
    }

    private Optional<ProjectileTracker> getTrackerByEntityId(UUID entityId) {
        return trackerRepository.stream().filter(pt -> pt.getProjectileId().equals(entityId)).findFirst();
    }

    private void cancelMythicEvent(ItemStack item, Cancellable event) {
        final Optional<ICustomItem> customItemQuery = customItemService.getItem(item);

        if (customItemQuery.isEmpty()) {
            return;
        }

        final ICustomItem customItem = customItemQuery.get();

        if (!(customItem instanceof IMythicItem)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onEnchantLimitApply(EnchantLimitApplyEvent event) {
        final ItemStack item = event.getItem();
        cancelMythicEvent(item, event);
    }

    @EventHandler
    public void onItemEnchant(PrepareItemEnchantEvent event) {
        final ItemStack item = event.getItem();
        cancelMythicEvent(item, event);
    }

    @EventHandler
    public void onItemMend(PlayerItemMendEvent event) {
        final ItemStack item = event.getItem();
        cancelMythicEvent(item, event);
    }

    @EventHandler
    public void onItemRepair(PrepareItemCraftEvent event) {
        if (!event.isRepair()) {
            return;
        }

        if (event.getRecipe() == null) {
            return;
        }

        final ItemStack item = event.getRecipe().getResult();
        final Optional<ICustomItem> customItemQuery = customItemService.getItem(item);

        if (customItemQuery.isEmpty()) {
            return;
        }

        final ICustomItem customItem = customItemQuery.get();

        if (!(customItem instanceof IMythicItem)) {
            return;
        }

        event.getInventory().setResult(new ItemStack(Material.AIR));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!(event.getDamager() instanceof final Player damager)) {
            return;
        }

        final ItemStack item = damager.getInventory().getItemInMainHand();
        final Optional<ICustomItem> customItemQuery = customItemService.getItem(item);

        if (customItemQuery.isEmpty()) {
            return;
        }

        final ICustomItem customItem = customItemQuery.get();

        if (!(customItem instanceof final IMythicItem mythicItem)) {
            return;
        }

        if (item.getItemMeta() instanceof final Damageable meta) {
            meta.setDamage(meta.getDamage() + mythicItem.getDurabilityCost());
            item.setItemMeta(meta);
            damager.getInventory().setItemInMainHand(item);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        final ItemStack item = event.getResult();

        if (item == null) {
            return;
        }

        final Optional<ICustomItem> customItemQuery = customItemService.getItem(item);

        if (customItemQuery.isEmpty()) {
            return;
        }

        final ICustomItem customItem = customItemQuery.get();

        if (!(customItem instanceof IMythicItem)) {
            return;
        }

        event.setResult(new ItemStack(Material.AIR));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityKill(EntityDeathEvent event) {
        final LivingEntity livingEntity = event.getEntity();
        final Player killer = livingEntity.getKiller();

        if (killer == null) {
            return;
        }

        final ItemStack hand = killer.getInventory().getItemInMainHand();
        final Optional<ICustomItem> customItemQuery = customItemService.getItem(hand);

        if (customItemQuery.isEmpty()) {
            return;
        }

        final ICustomItem customItem = customItemQuery.get();

        if (!(customItem instanceof final IMythicItem mythic)) {
            return;
        }

        if (!mythic.isFriendlyFireEnabled() && livingEntity instanceof final Player killedPlayer) {
            final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(killer);

            if (faction != null && faction.isMember(killedPlayer)) {
                return;
            }
        }

        mythic.onKill(killer, livingEntity);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof final LivingEntity livingEntity)) {
            return;
        }

        if (!(event.getDamager() instanceof final Player player)) {
            return;
        }

        final ItemStack hand = player.getInventory().getItemInMainHand();
        final Optional<ICustomItem> customItemQuery = customItemService.getItem(hand);

        if (customItemQuery.isEmpty()) {
            return;
        }

        final ICustomItem customItem = customItemQuery.get();

        if (!(customItem instanceof final IMythicItem mythic)) {
            return;
        }

        if (!mythic.isFriendlyFireEnabled() && livingEntity instanceof final Player attackedPlayer) {
            final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

            if (faction != null && faction.isMember(attackedPlayer)) {
                return;
            }
        }

        mythic.onAttack(player, livingEntity);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof final Player shooter)) {
            return;
        }

        if (event.getEntity() instanceof final Trident trident) {
            final ItemStack item = trident.getItem();
            final Optional<ICustomItem> customItemQuery = customItemService.getItem(item);

            if (customItemQuery.isEmpty()) {
                return;
            }

            final ProjectileTracker tracker = new ProjectileTracker(shooter, event.getEntity().getUniqueId(), item, customItemQuery.get());
            trackerRepository.add(tracker);
            new Scheduler(plugin).sync(() -> trackerRepository.remove(tracker)).delay(10*20L).run();
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof final Player shooter)) {
            return;
        }

        final ItemStack item = event.getBow();

        if (item == null) {
            return;
        }

        final Optional<ICustomItem> customItemQuery = customItemService.getItem(item);

        if (customItemQuery.isEmpty()) {
            return;
        }

        final ProjectileTracker tracker = new ProjectileTracker(shooter, event.getEntity().getUniqueId(), item, customItemQuery.get());
        trackerRepository.add(tracker);
        new Scheduler(plugin).sync(() -> trackerRepository.remove(tracker)).delay(10*20L).run();
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof final LivingEntity hitEntity)) {
            return;
        }

        final Projectile proj = event.getEntity();

        getTrackerByEntityId(proj.getUniqueId()).ifPresent(pt -> {
            trackerRepository.remove(pt);

            if (pt.getShooter() == null || !pt.getShooter().isOnline()) {
                return;
            }

            if (pt.getCustomItem() instanceof final IMythicItem mythic) {
                if (!mythic.isFriendlyFireEnabled() && hitEntity instanceof final Player hitPlayer) {
                    final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(pt.getShooter());

                    if (faction != null && faction.isMember(hitPlayer)) {
                        return;
                    }
                }

                mythic.onShoot(pt.getShooter(), hitEntity);
            }
        });
    }
}

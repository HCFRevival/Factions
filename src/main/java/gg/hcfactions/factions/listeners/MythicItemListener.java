package gg.hcfactions.factions.listeners;

import gg.hcfactions.cx.event.EnchantLimitApplyEvent;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Optional;

public final class MythicItemListener implements Listener {
    @Getter public final Factions plugin;
    @Getter public final CustomItemService customItemService;

    public MythicItemListener(Factions plugin) {
        this.plugin = plugin;
        this.customItemService = (CustomItemService) plugin.getService(CustomItemService.class);

        if (customItemService == null) {
            plugin.getAresLogger().error("Failed to obtain custom item service");
        }
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

        mythic.onAttack(player, livingEntity);
    }
}

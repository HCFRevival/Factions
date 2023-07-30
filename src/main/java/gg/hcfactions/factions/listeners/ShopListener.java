package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.shop.impl.GenericMerchant;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public record ShopListener(@Getter Factions plugin) implements Listener {
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof final Villager villager)) {
            return;
        }

        plugin.getShopManager().getMerchantByLocation(new BLocatable(villager.getLocation().getBlock())).ifPresent(merchant -> event.setCancelled(true));
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof final Villager villager)) {
            return;
        }

        plugin.getShopManager().getMerchantByLocation(new BLocatable(villager.getLocation().getBlock())).ifPresent(merchant -> event.setCancelled(true));
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final Entity clicked = event.getRightClicked();

        if (!(clicked instanceof Villager)) {
            return;
        }

        plugin.getShopManager().getMerchantByLocation(new BLocatable(clicked.getLocation().getBlock())).ifPresent(merchant ->
                plugin.getShopManager().getExecutor().openMerchant(player, (GenericMerchant) merchant));
    }
}

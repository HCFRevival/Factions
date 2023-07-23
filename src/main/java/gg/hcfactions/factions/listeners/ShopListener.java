package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import lombok.Getter;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public record ShopListener(@Getter Factions plugin) implements Listener {
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof final Villager villager)) {
            return;
        }

        plugin.getShopManager().getMerchantById(villager.getUniqueId()).ifPresent(merchant -> event.setCancelled(true));
    }
}

package gg.hcfactions.factions.listeners;

import gg.hcfactions.cx.event.PreMobstackEvent;
import gg.hcfactions.factions.Factions;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public record BossListener(@Getter Factions plugin) implements Listener {
    @EventHandler
    public void onGiantFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Giant)) {
            return;
        }

        if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onGiantEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Giant)) {
            return;
        }

        if (event.getDamager() instanceof Player) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMobstack(PreMobstackEvent event) {
        final Entity origin = event.getOriginEntity();
        final Entity merged = event.getMergingEntity();
        final PersistentDataContainer originContainer = origin.getPersistentDataContainer();
        final PersistentDataContainer mergedContainer = merged.getPersistentDataContainer();
        final String originValue = originContainer.get(plugin.getNamespacedKey(), PersistentDataType.STRING);
        final String mergedValue = mergedContainer.get(plugin.getNamespacedKey(), PersistentDataType.STRING);

        if (originValue != null && originValue.equalsIgnoreCase("noMerge")) {
            event.setCancelled(true);
            return;
        }

        if (mergedValue != null && mergedValue.equalsIgnoreCase("noMerge")) {
            event.setCancelled(true);
        }
    }
}

package gg.hcfactions.factions.listeners;

import com.google.common.collect.Maps;
import gg.hcfactions.cx.event.PreMobstackEvent;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.world.BossSpawnEvent;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class BossListener implements Listener {
    @Getter public final Factions plugin;
    private final Map<UUID, EntityType> spawnCooldowns;

    public BossListener(Factions plugin) {
        this.plugin = plugin;
        this.spawnCooldowns = Maps.newConcurrentMap();
    }

    @EventHandler
    public void onBossSpawn(BossSpawnEvent event) {
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(new BLocatable(
                Objects.requireNonNull(event.getSpawnLocation().getWorld()).getName(),
                event.getSpawnLocation().getX(),
                event.getSpawnLocation().getY(),
                event.getSpawnLocation().getZ())
        );

        if (insideClaim == null) {
            return;
        }

        final ServerFaction serverFaction = plugin.getFactionManager().getServerFactionById(insideClaim.getOwner());

        if (serverFaction == null) {
            return;
        }

        if (spawnCooldowns.containsKey(serverFaction.getUniqueId())) {
            event.setCancelled(true);
        }
    }

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

    @EventHandler (priority = EventPriority.LOWEST)
    public void onBossLoot(EntityDeathEvent event) {
        if (!event.getEntity().getPersistentDataContainer().has(plugin.getNamespacedKey(), PersistentDataType.STRING)) {
            return;
        }

        if (!event.getEntity().getPersistentDataContainer().get(plugin.getNamespacedKey(), PersistentDataType.STRING).equals("boss")) {
            return;
        }

        final Claim insideClaim = plugin.getClaimManager().getClaimAt(new BLocatable(event.getEntity().getLocation().getBlock()));
        if (insideClaim != null) {
            final ServerFaction owner = plugin.getFactionManager().getServerFactionById(insideClaim.getOwner());

            if (owner != null) {
                final UUID ownerId = owner.getUniqueId();
                spawnCooldowns.put(ownerId, event.getEntityType());
                new Scheduler(plugin).sync(() -> spawnCooldowns.remove(ownerId)).delay(12000L).run();
            }
        }

        event.setDroppedExp(event.getDroppedExp()*3);
        event.getDrops().clear();
        event.getDrops().addAll(plugin.getBossManager().getLootManager().getItems(3));
    }
}

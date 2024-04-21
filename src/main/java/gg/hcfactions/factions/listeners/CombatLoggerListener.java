package gg.hcfactions.factions.listeners;

import com.google.common.collect.Lists;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.cx.rollback.impl.RollbackInventory;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.CombatLoggerDeathEvent;
import gg.hcfactions.factions.listeners.events.player.PlayerDamageCombatLoggerEvent;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.logger.impl.CombatLogger;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public final class CombatLoggerListener implements Listener {
    @Getter public final Factions plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final CombatLogger logger = plugin.getLoggerManager().getLoggerById(player.getUniqueId());

        if (logger == null) {
            return;
        }

        logger.reapply(player);
        logger.getBukkitEntity().remove();
        plugin.getLoggerManager().getLoggerRepository().remove(logger);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer account = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(new PLocatable(player));
        final double radius = 16.0; // TODO: Make this configurable
        boolean combatLog = false;

        if (player.isDead()) {
            return;
        }

        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        if (account == null || player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            return;
        }

        if (account.isSafeDisconnect()) {
            return;
        }

        if (insideClaim != null) {
            final ServerFaction sf = plugin.getFactionManager().getServerFactionById(insideClaim.getOwner());

            if (sf != null && sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                return;
            }
        }

        if (account.hasTimer(ETimerType.COMBAT)) {
            combatLog = true;
        }

        if (player.getNoDamageTicks() > 0) {
            combatLog = true;
        }

        if (player.getFallDistance() >= 4.0) {
            combatLog = true;
        }

        if (player.getFireTicks() > 0 && !player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
            combatLog = true;
        }

        if (!FactionUtil.getNearbyEnemies(plugin, player, radius).isEmpty()) {
            combatLog = true;
        }

        if (!combatLog) {
            return;
        }

        // Print logger messages
        FMessage.broadcastCombatLogger(player);
        Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.hasPermission(FPermissions.P_FACTIONS_ADMIN)).forEach(staff -> FMessage.printStaffCombatLogger(staff, player.getName(), player.getLocation()));

        // Spawn logger in
        final CombatLogger logger = new CombatLogger(player.getLocation(), player, 30); // TODO: Make this configurable
        logger.spawn();
        plugin.getLoggerManager().getLoggerRepository().add(logger);

        new Scheduler(plugin).sync(() -> {
            if (plugin.getLoggerManager().getLoggerRepository().contains(logger)) {
                logger.getBukkitEntity().remove();
                plugin.getLoggerManager().getLoggerRepository().remove(logger);
            }
        }).delay(30 * 20).run(); // TODO: Make this configurable
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        final net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity)event.getEntity()).getHandle();

        if (!(nmsEntity instanceof final CombatLogger logger)) {
            return;
        }

        if (logger.getOwnerId() == null || logger.getOwnerUsername() == null) {
            return;
        }

        final Entity damager = event.getDamager();

        if (damager instanceof Projectile) {
            final ProjectileSource source = ((Projectile) damager).getShooter();

            if (source instanceof final Player playerDamager) {
                final PlayerDamageCombatLoggerEvent loggerEvent = new PlayerDamageCombatLoggerEvent(playerDamager, logger);

                Bukkit.getPluginManager().callEvent(loggerEvent);

                if (loggerEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
            }

            return;
        }

        if (damager instanceof final Player playerDamager) {
            final PlayerDamageCombatLoggerEvent loggerEvent = new PlayerDamageCombatLoggerEvent(playerDamager, logger);
            Bukkit.getPluginManager().callEvent(loggerEvent);

            if (loggerEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Villager)) {
            return;
        }

        final net.minecraft.world.entity.LivingEntity nmsEntity = ((CraftLivingEntity)event.getEntity()).getHandle();

        if (!(nmsEntity instanceof final CombatLogger logger)) {
            return;
        }

        if (logger.getOwnerId() == null || logger.getOwnerUsername() == null) {
            return;
        }

        final CombatLoggerDeathEvent deathEvent = new CombatLoggerDeathEvent(logger, event.getEntity().getKiller());
        Bukkit.getPluginManager().callEvent(deathEvent);
        logger.dropItems(event.getEntity().getLocation());
        plugin.getLoggerManager().getLoggerRepository().remove(logger);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();

        if (event.isCancelled()) {
            return;
        }

        if (!(entity instanceof final LivingEntity livingEntity)) {
            return;
        }

        final net.minecraft.world.entity.LivingEntity asNms = ((CraftLivingEntity)livingEntity).getHandle();

        if (!(asNms instanceof final CombatLogger logger)) {
            return;
        }

        if (logger.getOwnerId() == null || logger.getOwnerUsername() == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();

        plugin.getLoggerManager().getLoggerRepository().stream().filter(logger ->
                logger.getBukkitEntity().getLocation().getChunk().getX() == chunk.getX() &&
                        logger.getBukkitEntity().getLocation().getChunk().getZ() == chunk.getZ() &&
                        logger.getBukkitEntity().getLocation().getWorld() == chunk.getWorld()).forEach(inChunkLogger -> {

            if (plugin.getLoggerManager().getLoggerRepository().contains(inChunkLogger)) {
                inChunkLogger.getBukkitEntity().remove();
                plugin.getLoggerManager().getLoggerRepository().remove(inChunkLogger);
            }
        });
    }

    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        final Entity entity = event.getEntity();

        if (!(entity instanceof final LivingEntity livingEntity)) {
            return;
        }

        final net.minecraft.world.entity.LivingEntity asNms = ((CraftLivingEntity)livingEntity).getHandle();

        if (!(asNms instanceof CombatLogger)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        final CXService cxService = (CXService) plugin.getService(CXService.class);
        if (cxService == null) {
            return;
        }

        final CombatLogger logger = event.getLogger();
        final CraftLivingEntity livingEntity = (CraftLivingEntity) logger.getBukkitEntity();
        final List<ItemStack> armorContents = Lists.newArrayList();

        if (livingEntity.getEquipment() != null) {
            armorContents.addAll(Arrays.asList(livingEntity.getEquipment().getArmorContents()));
        }

        final RollbackInventory inv = cxService.getRollbackManager().createInventory(logger.getOwnerUsername(), logger.getLoggerInventory(), armorContents);
        cxService.getRollbackManager().getRollbackRepository().add(inv);
    }
}

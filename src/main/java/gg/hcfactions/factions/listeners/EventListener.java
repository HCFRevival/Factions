package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.EventStartEvent;
import gg.hcfactions.factions.listeners.events.faction.FactionDisbandEvent;
import gg.hcfactions.factions.listeners.events.player.CombatLoggerDeathEvent;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.logger.impl.CombatLogger;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.Getter;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record EventListener(@Getter Factions plugin) implements Listener {
    private void handleEventDeath(PlayerFaction playerFaction) {
        if (plugin.getEventManager().getActiveKothEvents().isEmpty()) {
            return;
        }

        for (KOTHEvent koth : plugin.getEventManager().getActiveKothEvents()) {
            final int currentTickets = koth.getSession().getTickets(playerFaction);

            if (currentTickets <= 0) {
                continue;
            }

            final int newTickets = currentTickets - plugin.getConfiguration().getEventTicketLossPerDeath();

            if (newTickets <= 0) {
                koth.getSession().getLeaderboard().remove(playerFaction.getUniqueId());
                playerFaction.sendMessage(" ");
                playerFaction.sendMessage(FMessage.KOTH_PREFIX + "Your faction is no longer on the leaderboard for " + koth.getDisplayName());
                playerFaction.sendMessage(" ");
                continue;
            }

            koth.getSession().getLeaderboard().put(playerFaction.getUniqueId(), newTickets);
            playerFaction.sendMessage(" ");
            playerFaction.sendMessage(FMessage.KOTH_PREFIX + "Your faction now has " + FMessage.LAYER_2 + newTickets + " tickets" + FMessage.LAYER_1 + " on the leaderboard for " + koth.getDisplayName());
            playerFaction.sendMessage(" ");
        }
    }

    /**
     * Wipes existing leaderboard data for KOTH events
     * when a faction disbands
     *
     * @param event FactionDisbandEvent
     */
    @EventHandler
    public void onFactionDisband(FactionDisbandEvent event) {
        plugin.getEventManager().getActiveKothEvents().forEach(kothEvent -> kothEvent.getSession().getLeaderboard().remove(event.getFaction().getUniqueId()));
    }

    /**
     * Despawn all monsters inside a claim for an event when it starts
     * @param event EventStartEvent
     */
    @EventHandler
    public void onEventStart(EventStartEvent event) {
        final ServerFaction owner = plugin.getFactionManager().getServerFactionById(event.getEvent().getOwner());

        if (owner == null) {
            return;
        }

        final List<Claim> claims = plugin.getClaimManager().getClaimsByOwner(owner);

        if (claims.isEmpty()) {
            return;
        }

        claims.forEach(claim -> {
            final World world = claim.getCornerA().getBukkitBlock().getWorld();

            world.getLivingEntities().forEach(livingEntity -> {
                if (livingEntity instanceof Monster && claim.isInside(new PLocatable(livingEntity), false)) {
                    livingEntity.remove();
                }
            });
        });
    }

    /**
     * Prevents creatures from spawning inside claims when an event is active
     * @param event CreatureSpawnEvent
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        if (!(event.getEntity() instanceof Monster)) {
            return;
        }

        final PLocatable loc = new PLocatable(event.getEntity());
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(loc);

        if (insideClaim == null) {
            return;
        }

        final ServerFaction sf = plugin.getFactionManager().getServerFactionById(insideClaim.getOwner());

        if (sf == null || !sf.getFlag().equals(ServerFaction.Flag.EVENT)) {
            return;
        }

        final Optional<IEvent> attachedEventQuery = plugin.getEventManager().getEvent(sf);

        if (attachedEventQuery.isEmpty()) {
            return;
        }

        final IEvent attachedEvent = attachedEventQuery.get();

        if (!attachedEvent.isActive()) {
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Subtract tokens on player death
     *
     * @param event PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final PlayerFaction pf = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (pf == null) {
            return;
        }

        handleEventDeath(pf);
    }

    /**
     * Subtract tokens on combat logger death
     *
     * @param event CombatLoggerDeathEvent
     */
    @EventHandler
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        final CombatLogger logger = event.getLogger();
        final UUID uniqueId = logger.getOwnerId();
        final PlayerFaction pf = plugin.getFactionManager().getPlayerFactionByPlayer(uniqueId);

        if (pf == null) {
            return;
        }

        handleEventDeath(pf);
    }

    /**
     * Protects palace loot chests from being opened
     * @param event PlayerInteractEvent
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.useInteractedBlock().equals(Event.Result.DENY) || !event.getClickedBlock().getType().equals(Material.CHEST)) {
            return;
        }

        final Player player = event.getPlayer();

        if (player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            return;
        }

        final Optional<PalaceLootChest> lootChestQuery = plugin.getEventManager().getPalaceLootManager().getLootChestAt(event.getClickedBlock());

        if (lootChestQuery.isEmpty()) {
            return;
        }

        final PalaceLootChest lootChest = lootChestQuery.get();
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(new BLocatable(event.getClickedBlock()));

        if (insideClaim == null) {
            return;
        }

        final Optional<PalaceEvent> palaceEventQuery = plugin.getEventManager().getPalaceEvents().stream().filter(pe -> pe.getOwner().equals(insideClaim.getOwner())).findFirst();

        if (palaceEventQuery.isEmpty()) {
            return;
        }

        final PalaceEvent palaceEvent = palaceEventQuery.get();

        if (palaceEvent.isChestUnlocked(lootChest)) {
            return;
        }

        if (palaceEvent.getCapturingFaction() != null) {
            final PlayerFaction capturingFaction = plugin.getFactionManager().getPlayerFactionById(palaceEvent.getCapturingFaction());

            if (capturingFaction != null && capturingFaction.isMember(player)) {
                return;
            }
        }

        final long unlockTime = palaceEvent.getLootUnlockTimes().getOrDefault(lootChest.getLootTier(), 0L);

        event.setUseInteractedBlock(Event.Result.DENY);
        player.sendMessage(FMessage.ERROR + "This chest will unlock in " + Time.convertToRemaining(unlockTime - Time.now()));
    }

    /**
     * Disables block explosions inside active event claims
     * @param event EntityExplodeEvent
     */
    @EventHandler
    public void onExplosion(ExplosionPrimeEvent event) {
        final BLocatable location = new BLocatable(event.getEntity().getLocation().getBlock());
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(location);

        if (insideClaim == null) {
            return;
        }

        final IFaction insideFaction = plugin.getFactionManager().getFactionById(insideClaim.getOwner());

        if (!(insideFaction instanceof final ServerFaction sf)) {
            return;
        }

        final Optional<IEvent> eventQuery = plugin.getEventManager().getEvent(sf);

        if (eventQuery.isEmpty()) {
            return;
        }

        final IEvent insideEvent = eventQuery.get();

        if (insideEvent.isActive()) {
            if (event.getEntity() instanceof MinecartTNT) {
                event.getEntity().remove();
            }

            event.setCancelled(true);
        }
    }
}

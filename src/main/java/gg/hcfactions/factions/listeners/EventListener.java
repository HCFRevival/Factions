package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.EventStartEvent;
import gg.hcfactions.factions.listeners.events.faction.FactionDisbandEvent;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;
import java.util.Optional;

public record EventListener(@Getter Factions plugin) implements Listener {
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
}

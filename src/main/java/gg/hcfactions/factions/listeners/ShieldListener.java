package gg.hcfactions.factions.listeners;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.claim.IShield;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.claim.impl.CombatShield;
import gg.hcfactions.factions.models.claim.impl.ProtectionShield;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.libs.bukkit.events.impl.PlayerBigMoveEvent;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ShieldListener(@Getter Factions plugin) implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null) {
            profile.hideAllShields();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer profile = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null) {
            profile.hideAllShields();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final PLocatable location = new PLocatable(player);

        if (profile == null) {
            return;
        }

        final boolean tagged = profile.hasTimer(ETimerType.COMBAT);
        final boolean protection = profile.hasTimer(ETimerType.PROTECTION);

        if (profile.getShields().isEmpty() && !tagged && !protection) {
            return;
        }

        new Scheduler(plugin).async(() -> {
            final List<Claim> claims = plugin.getClaimManager().getClaimsNearby(new PLocatable(player), 10);

            // No nearby claims, but existing shield blocks from last update must now be removed
            if (claims.isEmpty() && !profile.getShields().isEmpty()) {
                profile.hideAllShields();
                return;
            }

            // Player has existing shields
            // Here we are checking if they are out of reach of old shield blocks and hiding them if so
            if (!profile.getShields().isEmpty()) {
                final Set<IShield> shields = profile.getShields().stream().filter(shield -> shield.getLocation().getDistance(location) > 10.0).collect(Collectors.toSet());

                if (!shields.isEmpty()) {
                    shields.forEach(IShield::hide);
                    profile.getShields().removeAll(shields);
                }
            }

            if (tagged) {
                final List<Claim> safezones = Lists.newArrayList();

                for (Claim claim : claims) {
                    final IFaction faction = plugin.getFactionManager().getFactionById(claim.getOwner());

                    if (!(faction instanceof final ServerFaction serverFaction)) {
                        continue;
                    }

                    if (!serverFaction.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                        continue;
                    }

                    safezones.add(claim);
                }

                if (!safezones.isEmpty()) {
                    final List<BLocatable> perimeters = Lists.newArrayList();

                    for (Claim claim : safezones) {
                        for (double y = (location.getY() - 1); y < (location.getY() + 5); y++) {
                            final int rounded = (int) Math.round(y);

                            perimeters.addAll(claim.getPerimeter(rounded)
                                    .stream()
                                    .filter(b -> b.getDistance(location) <= 10.0)
                                    .filter(b ->
                                            b.getBukkitBlock().getType().equals(Material.AIR) ||
                                                    b.getBukkitBlock().getType().equals(Material.WATER))
                                    .collect(Collectors.toList()));
                        }
                    }

                    perimeters.stream().filter(shield -> profile.getShieldBlockAt(shield) == null).forEach(shield -> {
                        final CombatShield combatShield = new CombatShield(player, shield);
                        profile.getShields().add(combatShield);
                        combatShield.draw();
                    });
                }
            }

            if (protection) {
                final List<Claim> combatZones = Lists.newArrayList();

                for (Claim claim : claims) {
                    final IFaction faction = plugin.getFactionManager().getFactionById(claim.getOwner());

                    if (faction instanceof PlayerFaction) {
                        combatZones.add(claim);
                        continue;
                    }

                    final ServerFaction serverFaction = (ServerFaction) faction;

                    if (serverFaction.getFlag().equals(ServerFaction.Flag.EVENT)) {
                        combatZones.add(claim);
                    }
                }

                if (!combatZones.isEmpty()) {
                    final List<BLocatable> perimeters = Lists.newArrayList();

                    for (Claim claim : combatZones) {
                        for (double y = (location.getY() - 1.0); y < (location.getY() + 5); y += 1.0) {
                            final int rounded = (int) Math.round(y);

                            perimeters.addAll(claim.getPerimeter(rounded)
                                    .stream()
                                    .filter(b -> b.getDistance(location) <= 10.0)
                                    .filter(b -> b.getBukkitBlock().getType().equals(Material.AIR) || b.getBukkitBlock().getType().equals(Material.WATER))
                                    .collect(Collectors.toList()));
                        }
                    }

                    perimeters.stream().filter(shield -> profile.getShieldBlockAt(shield) == null).forEach(shield -> {
                        final ProtectionShield protShield = new ProtectionShield(player, shield);
                        profile.getShields().add(protShield);
                        protShield.draw();
                    });
                }
            }
        }).run();
    }
}

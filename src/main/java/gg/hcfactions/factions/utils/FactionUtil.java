package gg.hcfactions.factions.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.utils.Players;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public final class FactionUtil {
    public static final ImmutableList<Material> PILLAR_MATS = ImmutableList.of(
            Material.BOOKSHELF, Material.SOUL_SAND, Material.PUMPKIN, Material.SPONGE, Material.EMERALD_BLOCK,
            Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK, Material.IRON_BLOCK,
            Material.SANDSTONE, Material.PRISMARINE, Material.NETHERRACK, Material.NETHER_BRICK, Material.MELON, Material.END_STONE,
            Material.PURPUR_PILLAR, Material.BLACKSTONE, Material.CRIMSON_PLANKS
    );

    public static void cleanPlayer(Factions plugin, FactionPlayer factionPlayer) {
        final Player player = factionPlayer.getBukkit();
        final int protDuration = plugin.getServerStateManager().getCurrentState().equals(EServerState.SOTW)
                ? plugin.getConfiguration().getSotwProtectionDuration()
                : plugin.getConfiguration().getNormalProtectionDuration();

        if (factionPlayer.isPreferScoreboardDisplay() && factionPlayer.getScoreboard() != null) {
            factionPlayer.getTimers().forEach(t -> factionPlayer.getScoreboard().removeLine(t.getType().getScoreboardPosition()));
        }

        factionPlayer.getTimers().clear();
        factionPlayer.addTimer(new FTimer(ETimerType.PROTECTION, protDuration));

        Players.resetHealth(player);
    }

    public static void teleportToSafety(Factions plugin, Player player) {
        final PLocatable location = new PLocatable(player);

        new Scheduler(plugin).async(() -> {
            while (plugin.getClaimManager().getClaimAt(location) != null) {
                location.setX(location.getX() + 1.0);
                location.setZ(location.getZ() + 1.0);
            }

            new Scheduler(plugin).sync(() -> {
                location.setY(Objects.requireNonNull(location.getBukkitLocation().getWorld()).getHighestBlockYAt(location.getBukkitLocation()));
                Players.teleportWithVehicle(plugin, player, location.getBukkitLocation());
            }).run();
        }).run();
    }

    public static ImmutableList<Player> getNearbyFriendlies(Factions plugin, Player player, double distance) {
        final List<Player> result = Lists.newArrayList();

        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player.getUniqueId());
        if (faction == null || faction.getMembers().size() <= 1) {
            return ImmutableList.copyOf(result);
        }

        for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
            if (!(entity instanceof final Player otherPlayer)) {
                continue;
            }

            if (otherPlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            if (faction.getMember(otherPlayer.getUniqueId()) != null) {
                result.add(otherPlayer);
            }
        }

        return ImmutableList.copyOf(result);
    }

    public static ImmutableList<Player> getNearbyEnemies(Factions plugin, Player player, double radius) {
        final List<Player> result = Lists.newArrayList();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof final Player otherPlayer)) {
                continue;
            }

            final FactionPlayer otherAccount = (FactionPlayer) plugin.getPlayerManager().getPlayer(otherPlayer.getUniqueId());

            if (otherAccount == null) {
                continue;
            }

            if (otherPlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            if (!player.canSee(otherPlayer)) {
                continue;
            }

            if (otherPlayer.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                continue;
            }

            if (otherPlayer.isDead()) {
                continue;
            }

            if (faction != null && faction.isMember(otherPlayer.getUniqueId())) {
                continue;
            }

            final Claim insideClaim = plugin.getClaimManager().getClaimAt(new PLocatable(otherPlayer));

            if (insideClaim != null) {
                final IFaction insideOwner = plugin.getFactionManager().getFactionById(insideClaim.getOwner());

                if (insideOwner instanceof final ServerFaction serverFaction) {
                    if (serverFaction.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                        continue;
                    }
                }
            }

            result.add(otherPlayer);
        }

        return ImmutableList.copyOf(result);
    }
}

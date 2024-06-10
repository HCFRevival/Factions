package gg.hcfactions.factions.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.cx.CXService;
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class FactionUtil {
    public static final ImmutableList<Material> PILLAR_MATS = ImmutableList.of(
            Material.BOOKSHELF, Material.SOUL_SAND, Material.PUMPKIN, Material.SPONGE, Material.EMERALD_BLOCK,
            Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK, Material.IRON_BLOCK,
            Material.SANDSTONE, Material.PRISMARINE, Material.NETHERRACK, Material.NETHER_BRICK, Material.MELON, Material.END_STONE,
            Material.PURPUR_PILLAR, Material.BLACKSTONE, Material.CRIMSON_PLANKS
    );

    public static boolean isInteractable(Material material) {
        if (material.isInteractable()) {
            return true;
        }

        if (material.name().endsWith("PRESSURE_PLATE")) {
            return true;
        }

        return false;
    }

    public static boolean isPressurePlate(Material material) {
        return material.name().endsWith("PRESSURE_PLATE");
    }

    public static void cleanPlayer(Factions plugin, FactionPlayer factionPlayer) {
        final CXService cxs = (CXService) plugin.getService(CXService.class);
        final Player player = factionPlayer.getBukkit();
        final int protDuration = plugin.getServerStateManager().getCurrentState().equals(EServerState.SOTW)
                ? plugin.getConfiguration().getSotwProtectionDuration()
                : plugin.getConfiguration().getNormalProtectionDuration();

        factionPlayer.getTimers().forEach(t -> factionPlayer.removeTimer(t.getType()));

        if (protDuration > 0 && !plugin.getServerStateManager().isEOTW()) {
            factionPlayer.addTimer(new FTimer(ETimerType.PROTECTION, protDuration));
        }

        if (plugin.getConfiguration().isStarterKitEnabled() && cxs != null) {
            cxs.getKitManager().getKitByName(plugin.getConfiguration().getStarterKitName()).ifPresent(kit -> kit.give(player, false));
        }

        Players.resetHealth(player);
    }

    public static void teleportToSafety(Factions plugin, Player player) {
        final PLocatable location = new PLocatable(player);
        final World.Environment env = player.getWorld().getEnvironment();

        if (env.equals(World.Environment.THE_END)) {
            Players.teleportWithVehicle(plugin, player, plugin.getConfiguration().getEndSpawn());
            return;
        }

        new Scheduler(plugin).async(() -> {
            while (plugin.getClaimManager().getClaimAt(location) != null) {
                location.setX(location.getX() + 1.0);
                location.setZ(location.getZ() + 1.0);
            }

            new Scheduler(plugin).sync(() -> {
                int y = Objects.requireNonNull(location.getBukkitLocation().getWorld()).getHighestBlockYAt(location.getBukkitLocation());

                // nether ceiling check
                if (env.equals(World.Environment.NETHER)) {
                    for (int i = 125; i > 0; i--) {
                        final int lowerY = i - 1;
                        final int floorY = i - 2;
                        y = i;

                        final Block head = Objects.requireNonNull(location.getBukkitLocation().getWorld()).getBlockAt(location.getBukkitLocation().getBlockX(), y, location.getBukkitLocation().getBlockZ());
                        final Block feet = location.getBukkitLocation().getWorld().getBlockAt(location.getBukkitLocation().getBlockX(), lowerY, location.getBukkitLocation().getBlockZ());
                        final Block floor = location.getBukkitLocation().getWorld().getBlockAt(location.getBukkitLocation().getBlockX(), floorY, location.getBukkitLocation().getBlockZ());

                        if (!head.getType().equals(Material.AIR) || !feet.getType().equals(Material.AIR)) {
                            continue;
                        }

                        if (!floor.getType().isSolid()) {
                            continue;
                        }

                        break;
                    }
                }

                location.setY(y);
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
        return getNearbyEnemies(plugin, player, player.getLocation(), radius);
    }

    public static ImmutableList<Player> getNearbyEnemies(Factions plugin, Player player, Location location, double radius) {
        final List<Player> result = Lists.newArrayList();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        for (Entity entity : Objects.requireNonNull(location.getWorld()).getNearbyEntities(location, radius, radius, radius)) {
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

            if (faction != null && faction.getAlly() != null && faction.getAlly().isMember(otherPlayer)) {
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

    /**
     * Accepts a PlayerFaction
     * @param faction PlayerFaction to compare against
     * @return Total online player count between two factions
     */
    public static int getOnlineAlliesCount(PlayerFaction faction) {
        int count = faction.getOnlineMembers().size();

        if (!faction.hasAlly()) {
            return count;
        }

        final PlayerFaction ally = faction.getAlly();
        if (ally != null) {
            count += ally.getOnlineMembers().size();
        }

        return count;
    }
}

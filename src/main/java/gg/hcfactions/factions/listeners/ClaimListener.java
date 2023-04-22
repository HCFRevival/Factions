package gg.hcfactions.factions.listeners;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.ConsumeClassItemEvent;
import gg.hcfactions.factions.listeners.events.player.PlayerChangeClaimEvent;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.bukkit.events.impl.PlayerBigMoveEvent;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public final class ClaimListener implements Listener {
    @Getter public final Factions plugin;

    /**
     * Handles processing movement in and out of claims
     * @param cancellable Movement Event
     * @param player Player
     * @param from From Location
     * @param to To Location
     */
    private void handleMovement(Cancellable cancellable, Player player, Location from, Location to) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            return;
        }

        final Claim expectedClaim = factionPlayer.getCurrentClaim();
        final Claim predictedClaim = plugin.getClaimManager().getClaimAt(new PLocatable(
                to.getWorld().getName(),
                to.getX(),
                to.getY(),
                to.getZ(),
                to.getYaw(),
                to.getPitch()));

        final World.Environment fromEnv = Objects.requireNonNull(from.getWorld()).getEnvironment();
        final World.Environment toEnv = to.getWorld().getEnvironment();

        if (expectedClaim == null && predictedClaim == null && !fromEnv.equals(toEnv)) {
            if (getEnvironmentName(fromEnv) != null) {
                player.sendMessage(ChatColor.GOLD + "Now Leaving: " + ChatColor.RESET + getEnvironmentName(fromEnv) + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
            }

            if (getEnvironmentName(toEnv) != null) {
                player.sendMessage(ChatColor.GOLD + "Now Entering: " + ChatColor.RESET + getEnvironmentName(toEnv) + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
            }
        }

        if (expectedClaim == predictedClaim) {
            return;
        }

        final PlayerChangeClaimEvent playerChangeClaimEvent = new PlayerChangeClaimEvent(player, from, to, expectedClaim, predictedClaim);
        Bukkit.getPluginManager().callEvent(playerChangeClaimEvent);

        if (playerChangeClaimEvent.isCancelled()) {
            cancellable.setCancelled(true);
            return;
        }

        factionPlayer.setCurrentClaim(predictedClaim);
    }

    private boolean handleBlockModification(Cancellable cancellable, Player player, Block block) {
        final Claim claim = plugin.getClaimManager().getClaimAt(new BLocatable(block));
        final List<Claim> withinBuildBuffer = plugin.getClaimManager().getClaimsNearby(new PLocatable(player), true);
        final boolean bypass = player.hasPermission("ares.factions.claim");

        if (claim == null && !withinBuildBuffer.isEmpty() && !bypass) {
            final Claim insideBuffer = withinBuildBuffer.get(0);
            final ServerFaction bufferFaction = plugin.getFactionManager().getServerFactionById(insideBuffer.getOwner());

            if (bufferFaction != null) {
                player.sendMessage(
                        ChatColor.RED + "You can not edit terrain within " +
                                ChatColor.BLUE + String.format("%.2f", bufferFaction.getBuildBuffer()) +
                                " blocks" + ChatColor.RED + " of " + ChatColor.RESET + bufferFaction.getDisplayName());

                cancellable.setCancelled(true);
                return false;
            }
        }

        if (claim == null) {
            return true;
        }

        final IFaction faction = plugin.getFactionManager().getFactionById(claim.getOwner());

        if (faction == null) {
            return true;
        }

        if (faction instanceof final ServerFaction serverFaction && !bypass) {
            player.sendMessage(ChatColor.RED + "This land is owned by " + serverFaction.getDisplayName());
            cancellable.setCancelled(true);
            return false;
        } else if (faction instanceof final PlayerFaction playerFaction) {
            if (!playerFaction.isRaidable() && !playerFaction.isMember(player.getUniqueId()) && !bypass) {
                player.sendMessage(ChatColor.RED + "This land is owned by " + ChatColor.YELLOW + playerFaction.getName());
                cancellable.setCancelled(true);
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a formatted name of a Bukkit Environment
     * @param environment Environment
     * @return Name
     */
    private String getEnvironmentName(World.Environment environment) {
        if (environment == null) {
            return null;
        }

        if (environment.equals(World.Environment.NORMAL)) {
            return ChatColor.DARK_GREEN + "Overworld";
        } else if (environment.equals(World.Environment.NETHER)) {
            return ChatColor.DARK_RED + "The Nether";
        } else if (environment.equals(World.Environment.THE_END)) {
            return ChatColor.DARK_PURPLE + "The End";
        }

        return null;
    }

    /**
     * Handles moving in and out of claims
     * @param event PlayerBigMoveEvent
     */
    @EventHandler
    public void onPlayerMove(PlayerBigMoveEvent event) {
        handleMovement(event, event.getPlayer(), event.getFrom(), event.getTo());
    }

    /**
     * Handles teleporting in and out of claims
     * @param event PlayerTeleportEvent
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // We handle Ender Pearl teleportation above
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        handleMovement(event, event.getPlayer(), event.getFrom(), event.getTo());
    }

    /**
     * Handles preventing block modifications
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleBlockModification(event, event.getPlayer(), event.getBlock());
    }

    /**
     * Handles preventing block modifications
     * @param event BlockPlaceEvent
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockModification(event, event.getPlayer(), event.getBlock());
    }

    /**
     * Handles preventing block modifications
     * @param event PlayerBucketFillEvent
     */
    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        handleBlockModification(event, event.getPlayer(), event.getBlockClicked());
    }

    /**
     * Handles preventing block modifications
     * @param event PlayerBucketEmptyEvent
     */
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        handleBlockModification(event, event.getPlayer(), event.getBlockClicked());
    }

    /**
     * Handles blocking piston event
     * @param event BlockPistonRetractEvent
     */
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        final Block piston = event.getBlock();
        final Claim pistonClaim = plugin.getClaimManager().getClaimAt(new BLocatable(piston));

        for (Block affected : event.getBlocks()) {
            final Claim affectedClaim = plugin.getClaimManager().getClaimAt(new BLocatable(affected));
            final List<Claim> affectedBuildBuffers = plugin.getClaimManager().getClaimsNearby(new BLocatable(affected), true);

            if (pistonClaim == null && affectedClaim != null) {
                event.setCancelled(true);
                return;
            }

            else if (pistonClaim != null && affectedClaim == null) {
                event.setCancelled(true);
                return;
            }

            else if (pistonClaim != null && !pistonClaim.getUniqueId().equals(affectedClaim.getUniqueId())) {
                event.setCancelled(true);
                return;
            }

            else if (!affectedBuildBuffers.isEmpty()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Handles blocking piston event
     * @param event BlockPistonExtendEvent
     */
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        final Block piston = event.getBlock();
        final Claim pistonClaim = plugin.getClaimManager().getClaimAt(new BLocatable(piston));

        for (Block affected : event.getBlocks()) {
            final Claim affectedClaim = plugin.getClaimManager().getClaimAt(new BLocatable(affected));
            final List<Claim> affectedBuildBuffers = plugin.getClaimManager().getClaimsNearby(new BLocatable(affected), true);

            if (pistonClaim == null && affectedClaim != null) {
                event.setCancelled(true);
                return;
            }

            else if (pistonClaim != null && affectedClaim == null) {
                event.setCancelled(true);
                return;
            }

            else if (pistonClaim != null && !pistonClaim.getUniqueId().equals(affectedClaim.getUniqueId())) {
                event.setCancelled(true);
                return;
            }

            else if (!affectedBuildBuffers.isEmpty()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Handles preventing the creation of a portal inside a
     * @param event PortalCreateEvent
     */
    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final List<BlockState> blocks = event.getBlocks();

        if (!event.getReason().equals(PortalCreateEvent.CreateReason.NETHER_PAIR)) {
            return;
        }

        for (BlockState blockState : blocks) {
            final Block block = blockState.getBlock();
            final Claim inside = plugin.getClaimManager().getClaimAt(new BLocatable(block));

            if (inside != null) {
                final ServerFaction serverFaction = plugin.getFactionManager().getServerFactionById(inside.getOwner());

                if (serverFaction != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Handles preventing interacting in claims
     * @param event PlayerInteractEvent
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Action action = event.getAction();
        final Block block = event.getClickedBlock();
        final boolean admin = player.hasPermission("ares.factions.claim");

        if (!event.useInteractedBlock().equals(Event.Result.ALLOW)) {
            return;
        }

        if (admin) {
            return;
        }

        if (block == null || (block.getType().equals(Material.AIR))) {
            return;
        }

        if (!block.getType().isInteractable()) {
            return;
        }

        final Claim inside = plugin.getClaimManager().getClaimAt(new BLocatable(block));

        if (inside == null) {
            return;
        }

        final IFaction owner = plugin.getFactionManager().getFactionById(inside.getOwner());

        if (owner == null) {
            return;
        }

        if (owner instanceof final ServerFaction sf) {
            if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                if (!action.equals(Action.PHYSICAL)) {
                    player.sendMessage(ChatColor.RED + "This land is owned by " + ChatColor.RESET + sf.getDisplayName());
                }

                event.setUseInteractedBlock(Event.Result.DENY);
            }
        } else if (owner instanceof final PlayerFaction pf) {
            if (!pf.isRaidable() && pf.getMember(player.getUniqueId()) == null && !admin) {
                if (!action.equals(Action.PHYSICAL)) {
                    player.sendMessage(ChatColor.RED + "This land is owned by " + ChatColor.YELLOW + pf.getName());
                }

                event.setUseInteractedBlock(Event.Result.DENY);
            }
        }
    }

    /**
     * Handles preventing food level
     * @param event FoodLevelChangeEvent
     */
    @EventHandler
    public void onPlayerHungerChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final FactionPlayer profile = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null || profile.getCurrentClaim() == null) {
            return;
        }

        final ServerFaction faction = plugin.getFactionManager().getServerFactionById(profile.getCurrentClaim().getOwner());

        if (faction != null && faction.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.setExhaustion(0);

            event.setCancelled(true);
        }
    }

    /**
     * Handles preventing friendly entity damage within Safezone claims
     * @param event EntityDamageEvent
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final LivingEntity entity)) {
            return;
        }

        final Claim inside = plugin.getClaimManager().getClaimAt(new PLocatable(entity));

        if (entity instanceof Monster) {
            return;
        }

        if (inside != null) {
            final ServerFaction faction = plugin.getFactionManager().getServerFactionById(inside.getOwner());

            if (faction != null && faction.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles preventing monster spawning inside safezone claims
     * @param event CreatureSpawnEvent
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        final Claim inside = plugin.getClaimManager().getClaimAt(new PLocatable(event.getEntity()));

        if (inside != null) {
            final ServerFaction faction = plugin.getFactionManager().getServerFactionById(inside.getOwner());

            if (faction != null && faction.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles preventing entity targeting players standing in Safezone claims
     * @param event EntityTargetEvent
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof final Player player)) {
            return;
        }

        final Claim inside = plugin.getClaimManager().getClaimAt(new PLocatable(player));

        if (inside != null) {
            final ServerFaction faction = plugin.getFactionManager().getServerFactionById(inside.getOwner());

            if (faction != null) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles preventing entity exploding and destroying blocks in claimed land
     * @param event EntityExplodeEvent
     */
    @EventHandler
    public void onEntityExlode(EntityExplodeEvent event) {
        final List<Block> toRemove = Lists.newArrayList();

        for (Block block : event.blockList()) {
            final Claim inside = plugin.getClaimManager().getClaimAt(new BLocatable(block));
            final List<Claim> insideBuildBuffer = plugin.getClaimManager().getClaimsNearby(new BLocatable(block), true);

            if (inside != null || !insideBuildBuffer.isEmpty()) {
                toRemove.add(block);
            }
        }

        event.blockList().removeAll(toRemove);
    }

    /**
     * Handles preventing entity block changes inside claimed land
     * @param event EntityChangeBlockEvent
     */
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            return;
        }

        final Claim inside = plugin.getClaimManager().getClaimAt(new BLocatable(event.getBlock()));
        final List<Claim> insideBuildBuffer = plugin.getClaimManager().getClaimsNearby(new BLocatable(event.getBlock()), true);

        if (inside != null || !insideBuildBuffer.isEmpty()) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents players from enderpearling in to claims they are not allowed to enter
     * @param event PlayerTeleportEvent
     */
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer profile = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final FTimer timer = profile.getTimer(ETimerType.ENDERPEARL);
        final Claim inside = plugin.getClaimManager().getClaimAt(new PLocatable(
                event.getTo().getWorld().getName(),
                event.getTo().getX(),
                event.getTo().getY(),
                event.getTo().getZ(),
                event.getTo().getYaw(),
                event.getTo().getPitch()));

        if (timer == null) {
            return;
        }

        if (inside == null) {
            return;
        }

        final IFaction owner = plugin.getFactionManager().getFactionById(inside.getOwner());

        if (owner == null) {
            return;
        }

        if (owner instanceof PlayerFaction) {
            if (profile.getTimer(ETimerType.PROTECTION) != null) {
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                player.sendMessage(ChatColor.RED + "Your enderpearl landed in a claim you are not allowed to enter");

                profile.getTimers().remove(timer);

                event.setCancelled(true);
            }
        } else if (owner instanceof ServerFaction) {
            final ServerFaction sf = (ServerFaction)owner;
            //final EventsAddon eventsAddon = (EventsAddon) plugin.getAddonManager().get(EventsAddon.class);
            //final AresEvent aresEvent = eventsAddon.getManager().getEventByOwnerId(sf.getUniqueId());

            if (profile.getTimer(ETimerType.COMBAT) != null && sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                player.sendMessage(ChatColor.RED + "Your enderpearl landed in a claim you are not allowed to enter");

                profile.getTimers().remove(timer);

                event.setCancelled(true);
            }

            if (profile.getTimer(ETimerType.PROTECTION) != null && sf.getFlag().equals(ServerFaction.Flag.EVENT)) {
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                player.sendMessage(ChatColor.RED + "Your enderpearl landed in a claim you are not allowed to enter");

                profile.getTimers().remove(timer);

                event.setCancelled(true);
            }

            /* if (aresEvent instanceof PalaceEvent) { TODO: Reimpl PalaceEvent
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                player.sendMessage(ChatColor.RED + "You can not enderpearl in Palace claims");

                profile.getTimers().remove(timer);

                event.setCancelled(true);
            } */
        }
    }

    /**
     * Handles escorting players who have logged out in event claims outside the claim
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final Claim inside = plugin.getClaimManager().getClaimAt(new PLocatable(player));

        if (inside != null) {
            final IFaction owner = plugin.getFactionManager().getFactionById(inside.getOwner());
            final PlayerChangeClaimEvent changeClaimEvent = new PlayerChangeClaimEvent(player, null, player.getLocation(), null, inside);
            Bukkit.getPluginManager().callEvent(changeClaimEvent);

            if (changeClaimEvent.isCancelled()) {
                FactionUtil.teleportToSafety(plugin, player);
                player.sendMessage(ChatColor.DARK_PURPLE + "You have been escorted outside of the claim you were logged out in");
                return;
            }

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.Flag.EVENT)) {
                    FactionUtil.teleportToSafety(plugin, player);
                    player.sendMessage(ChatColor.DARK_PURPLE + "You have been escorted outside of the claim you were logged out in");
                    return;
                }
            }

            profile.setCurrentClaim(inside);
        }
    }

    /**
     * Handles issuing or revoking class consumable effects for players based on claims
     * @param event ConsumeClassItemEvent
     */
    @EventHandler (priority = EventPriority.HIGH)
    public void onConsume(ConsumeClassItemEvent event) {
        final Player player = event.getPlayer();
        final Claim inside = plugin.getClaimManager().getClaimAt(new PLocatable(player));
        final IFaction faction = (inside != null) ? plugin.getFactionManager().getFactionById(inside.getOwner()) : null;

        if (faction instanceof ServerFaction && ((ServerFaction) faction).getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
            player.sendMessage(ChatColor.RED + "You can not consume class items while in a Safezone");
            event.setCancelled(true);
            return;
        }

        final List<UUID> toRemove = Lists.newArrayList();

        for (UUID affectedUuid : event.getAffectedPlayers().keySet()) {
            final Player affected = Bukkit.getPlayer(affectedUuid);

            if (affected == null) {
                toRemove.add(affectedUuid);
                continue;
            }

            final Claim affectedInside = plugin.getClaimManager().getClaimAt(new PLocatable(affected));

            if (affectedInside == null) {
                continue;
            }

            final IFaction affectedInsideFaction = plugin.getFactionManager().getFactionById(affectedInside.getOwner());

            if (affectedInsideFaction == null) {
                continue;
            }

            if (affectedInsideFaction instanceof ServerFaction) {
                final ServerFaction affectedInsideServerFaction = (ServerFaction)affectedInsideFaction;

                if (affectedInsideServerFaction.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    toRemove.add(affectedUuid);
                }
            }
        }

        for (UUID removed : toRemove) {
            event.getAffectedPlayers().remove(removed);
        }
    }

    /**
     * Handles preventing entering claims not allowed with Protection or Combat Tag
     * @param event PlayerChangeClaimEvent
     */
    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerChangeClaim(PlayerChangeClaimEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final Claim to = event.getClaimTo();

        if (to != null) {
            final IFaction toFaction = plugin.getFactionManager().getFactionById(to.getOwner());

            if (toFaction == null) {
                return;
            }

            if (toFaction instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)toFaction;

                if (profile.hasTimer(ETimerType.COMBAT) && sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    player.sendMessage(ChatColor.RED + "You can not enter this claim while combat-tagged");
                    event.setCancelled(true);
                    return;
                }

                if (profile.hasTimer(ETimerType.PROTECTION) && sf.getFlag().equals(ServerFaction.Flag.EVENT)) {
                    player.sendMessage(ChatColor.RED + "You can not enter this claim while you have PvP Protection");
                    event.setCancelled(true);
                }
            } else if (toFaction instanceof PlayerFaction) {
                if (profile.hasTimer(ETimerType.PROTECTION)) {
                    player.sendMessage(ChatColor.RED + "You can not enter this claim while you have PvP Protection");
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Handles rendering a chat message for entering and leaving claims
     * @param event PlayerChangeClaimEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onClaimChange(PlayerChangeClaimEvent event) {
        final Player player = event.getPlayer();
        final Claim from = event.getClaimFrom();
        final Claim to = event.getClaimTo();
        final World.Environment fromEnv = (event.getLocationFrom() != null ? event.getLocationFrom().getWorld().getEnvironment() : null);
        final World.Environment toEnv = event.getLocationTo().getWorld().getEnvironment();

        if (event.isCancelled()) {
            return;
        }

        if (from != null && to != null && from.getOwner().equals(to.getOwner())) {
            return;
        }

        if (from != null) {
            final IFaction owner = plugin.getFactionManager().getFactionById(from.getOwner());

            if (owner != null) {
                if (owner instanceof ServerFaction) {
                    final ServerFaction serverFaction = (ServerFaction)owner;
                    player.sendMessage(ChatColor.GOLD + "Now Leaving: " + ChatColor.RESET + serverFaction.getDisplayName() + ChatColor.GOLD + " (" + serverFaction.getFlag().getDisplayName() + ChatColor.GOLD + ")");
                } else {
                    final PlayerFaction playerFaction = (PlayerFaction)owner;
                    final ChatColor color = (playerFaction.isMember(player.getUniqueId()) ? ChatColor.GREEN : ChatColor.RED);
                    player.sendMessage(ChatColor.GOLD + "Now Leaving: " + color + playerFaction.getName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
                }
            }
        } else if (getEnvironmentName(fromEnv) != null) {
            player.sendMessage(ChatColor.GOLD + "Now Leaving" + ChatColor.RESET + ": " + getEnvironmentName(fromEnv) + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
        }

        if (to != null) {
            final IFaction owner = plugin.getFactionManager().getFactionById(to.getOwner());

            if (owner != null) {
                if (owner instanceof final ServerFaction serverFaction) {
                    player.sendMessage(ChatColor.GOLD + "Now Entering: " + ChatColor.RESET + serverFaction.getDisplayName() + ChatColor.GOLD + " (" + serverFaction.getFlag().getDisplayName() + ChatColor.GOLD + ")");
                } else {
                    final PlayerFaction playerFaction = (PlayerFaction)owner;
                    final ChatColor color = (playerFaction.isMember(player.getUniqueId()) ? ChatColor.GREEN : ChatColor.RED);
                    player.sendMessage(ChatColor.GOLD + "Now Entering: " + color + playerFaction.getName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
                }
            }
        } else if (getEnvironmentName(toEnv) != null) {
            player.sendMessage(ChatColor.GOLD + "Now Entering" + ChatColor.RESET + ": " + getEnvironmentName(toEnv) + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
        }
    }

    /**
     * Handles freezing timers when entering and leaving claims
     * @param event PlayerChangeClaimEvent
     */
    @EventHandler (priority = EventPriority.HIGH)
    public void onClaimChangeTimerFreeze(PlayerChangeClaimEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final Claim to = event.getClaimTo();
        boolean safezone = false;

        if (factionPlayer == null) {
            return;
        }

        final FTimer protectionTimer = factionPlayer.getTimer(ETimerType.PROTECTION);

        if (protectionTimer == null) {
            return;
        }

        if (to != null) {
            final IFaction insideFaction = plugin.getFactionManager().getFactionById(to.getOwner());

            if (insideFaction instanceof ServerFaction) {
                final ServerFaction serverFaction = (ServerFaction)insideFaction;
                safezone = serverFaction.getFlag().equals(ServerFaction.Flag.SAFEZONE);
            }
        }

        if (safezone && !protectionTimer.isFrozen()) {
            protectionTimer.setFrozen(true);
        } else if (!safezone && protectionTimer.isFrozen()) {
            protectionTimer.setFrozen(false);
        }
    }
}
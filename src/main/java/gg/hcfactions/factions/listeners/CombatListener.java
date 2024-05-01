package gg.hcfactions.factions.listeners;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.faction.FactionMemberDeathEvent;
import gg.hcfactions.factions.listeners.events.player.CombatLoggerDeathEvent;
import gg.hcfactions.factions.listeners.events.player.PlayerDamageCombatLoggerEvent;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.logger.impl.CombatLogger;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.impl.PlayerStatHolder;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.libs.bukkit.events.impl.DetailedPlayerDeathEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerLingeringSplashEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerSplashPlayerEvent;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
public final class CombatListener implements Listener {
    private final Factions plugin;
    public final Set<UUID> recentlyPrintedDeathMessage;

    public CombatListener(Factions plugin) {
        this.plugin = plugin;
        this.recentlyPrintedDeathMessage = Sets.newConcurrentHashSet();
    }

    private void handlePlayerAttack(Cancellable event, Player attacker, Player attacked, boolean printFeedback) {
        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        final FactionPlayer attackerProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null || attackedProfile == null) {
            return;
        }

        if (attackerProfile.hasTimer(ETimerType.PROTECTION)) {
            if (printFeedback) {
                attacker.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_ATTACK_PVP_PROT.getErrorDescription());
            }

            event.setCancelled(true);
            return;
        }

        if (attackedProfile.hasTimer(ETimerType.PROTECTION)) {
            if (printFeedback) {
                attacker.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_ATTACK_PVP_PROT_OTHER.getErrorDescription());
            }

            event.setCancelled(true);
            return;
        }

        final Claim attackerClaim = plugin.getClaimManager().getClaimAt(new PLocatable(attacker));
        final Claim attackedClaim = plugin.getClaimManager().getClaimAt(new PLocatable(attacked));

        if (attackerClaim != null) {
            final ServerFaction owner = plugin.getFactionManager().getServerFactionById(attackerClaim.getOwner());

            if (owner != null) {
                if (owner.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    if (printFeedback) {
                        FMessage.printCanNotFightInClaim(attacker, owner.getDisplayName());
                    }

                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedClaim != null) {
            final ServerFaction owner = plugin.getFactionManager().getServerFactionById(attackedClaim.getOwner());

            if (owner != null) {
                if (owner.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    if (printFeedback) {
                        FMessage.printCanNotFightInClaim(attacker, owner.getDisplayName());
                    }

                    event.setCancelled(true);
                }
            }
        }

        final PlayerFaction attackerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(attacker);

        if (attackerFaction != null && attackerFaction.getMember(attacked.getUniqueId()) != null) {
            if (printFeedback) {
                FMessage.printCanNotAttackFactionMembers(attacker);
            }

            event.setCancelled(true);
        }
    }

    /**
     * Prints death coordinates upon player death
     * @param event PlayerDeathEvent
     */
    @EventHandler
    public void onPrintDeathLocation(PlayerDeathEvent event) {
        Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.hasPermission(FPermissions.P_FACTIONS_ADMIN)).forEach(staff ->
                FMessage.printStaffDeathMessage(staff, event.getEntity().getName(), event.getEntity().getLocation()));
    }

    /**
     * Prints combat logger coordinates upon player death
     * @param event CombatLoggerDeathEvent
     */
    @EventHandler
    public void onPrintLoggerDeathLocation(CombatLoggerDeathEvent event) {
        Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.hasPermission(FPermissions.P_FACTIONS_ADMIN)).forEach(staff ->
                FMessage.printStaffDeathMessage(staff, event.getLogger().getOwnerUsername(), event.getLogger().getBukkitEntity().getLocation()));
    }

    /**
     * Handles rendering display for Faction member deaths
     *
     * @param event FactionMemberDeathEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMemberDeath(FactionMemberDeathEvent event) {
        final String username = event.getUsername();
        final PlayerFaction faction = event.getFaction();
        final double subtracted = event.getSubtractedDTR();

        FMessage.printMemberDeath(faction, username, subtracted);
    }

    @EventHandler
    public void onEntityKnockbackByEntity(EntityKnockbackByEntityEvent event) {
        if (!(event.getEntity() instanceof final Player attacked)) {
            return;
        }

        if (!(event.getHitBy() instanceof final WindCharge windCharge)) {
            return;
        }

        if (!(windCharge.getShooter() instanceof final Player attacker)) {
            return;
        }

        handlePlayerAttack(event, attacker, attacked, true);
    }

    /**
     * Handles enforcing physical combat restrictions
     *
     * @param event PlayerDamagePlayerEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPhysicalDamage(PlayerDamagePlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();

        handlePlayerAttack(event, attacker, attacked, true);
    }

    /**
     * Handles enforcing fire aspect and flame enchantments
     *
     * @param event EntityCombustByEntityEvent
     */
    @EventHandler
    public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
        if (!(event.getCombuster() instanceof final Projectile projectile) || !(event.getEntity() instanceof final Player attacked)) {
            return;
        }

        final ProjectileSource source = projectile.getShooter();

        if (!(source instanceof final Player attacker)) {
            return;
        }

        handlePlayerAttack(event, attacker, attacked, false);
    }

    @EventHandler
    public void onPlayerLingeringSplash(PlayerLingeringSplashEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final AreaEffectCloud cloud = event.getCloud();

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        if (cloud == null
                || cloud.getBasePotionType() == null
                || cloud.getBasePotionType().getPotionEffects().stream().noneMatch(eff ->
                    eff.getType().equals(PotionEffectType.INSTANT_DAMAGE)
                    && eff.getType().equals(PotionEffectType.WEAKNESS)
                    && eff.getType().equals(PotionEffectType.SLOWNESS)
                    && eff.getType().equals(PotionEffectType.POISON)
                    && eff.getType().equals(PotionEffectType.SLOW_FALLING))) {
            return;
        }

        final FactionPlayer attackerProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null || attackedProfile == null) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.hasTimer(ETimerType.PROTECTION) || attackedProfile.hasTimer(ETimerType.PROTECTION)) {
            event.setCancelled(true);
            return;
        }

        final Claim attackerClaim = plugin.getClaimManager().getClaimAt(new PLocatable(attacker));
        final Claim attackedClaim = plugin.getClaimManager().getClaimAt(new PLocatable(attacked));

        if (attackerClaim != null) {
            final ServerFaction owner = plugin.getFactionManager().getServerFactionById(attackerClaim.getOwner());

            if (owner != null) {
                if (owner.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedClaim != null) {
            final ServerFaction owner = plugin.getFactionManager().getServerFactionById(attackedClaim.getOwner());

            if (owner != null) {
                if (owner.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Handles enforcing splash potions with timers and claims
     *
     * @param event PlayerSplashPlayerEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSplashPlayer(PlayerSplashPlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final ThrownPotion potion = event.getPotion();
        boolean isDebuff = false;

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        for (PotionEffect effect : potion.getEffects()) {
            if (effect.getType().equals(PotionEffectType.POISON) ||
                    effect.getType().equals(PotionEffectType.SLOWNESS) ||
                    effect.getType().equals(PotionEffectType.WEAKNESS) ||
                    effect.getType().equals(PotionEffectType.INSTANT_DAMAGE) ||
                    effect.getType().equals(PotionEffectType.SLOW_FALLING)) {
                isDebuff = true;
                break;
            }
        }

        if (!isDebuff) {
            return;
        }

        final FactionPlayer attackerProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null || attackedProfile == null) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.hasTimer(ETimerType.PROTECTION) || attackedProfile.hasTimer(ETimerType.PROTECTION)) {
            event.setCancelled(true);
            return;
        }

        final Claim attackerClaim = plugin.getClaimManager().getClaimAt(new PLocatable(attacker));
        final Claim attackedClaim = plugin.getClaimManager().getClaimAt(new PLocatable(attacked));

        if (attackerClaim != null) {
            final ServerFaction owner = plugin.getFactionManager().getServerFactionById(attackerClaim.getOwner());

            if (owner != null) {
                if (owner.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    FMessage.printCanNotFightInClaim(attacker, owner.getDisplayName());
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedClaim != null) {
            final ServerFaction owner = plugin.getFactionManager().getServerFactionById(attackedClaim.getOwner());

            if (owner != null) {
                if (owner.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    FMessage.printCanNotFightInClaim(attacker, owner.getDisplayName());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerDamageLogger(PlayerDamageCombatLoggerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final CombatLogger combatLogger = event.getLogger();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (factionPlayer == null) {
            return;
        }

        if (factionPlayer.hasTimer(ETimerType.PROTECTION)) {
            player.sendMessage(ChatColor.RED + FError.P_CAN_NOT_ATTACK_PVP_PROT.getErrorDescription());
            event.setCancelled(true);
            return;
        }

        if (factionPlayer.getCurrentClaim() != null) {
            final ServerFaction owner = plugin.getFactionManager().getServerFactionById(factionPlayer.getCurrentClaim().getOwner());

            if (owner != null && owner.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                FMessage.printCanNotFightInClaim(player, owner.getDisplayName());
                event.setCancelled(true);
                return;
            }
        }

        if (playerFaction != null && playerFaction.isMember(combatLogger.getOwnerId())) {
            FMessage.printCanNotAttackFactionMembers(player);
            event.setCancelled(true);
        }
    }

    /**
     * Handles reducing faction power loss in nether/end if it's enabled in the Factions config
     *
     * @param event FactionMemberDeathEvent
     */
    @EventHandler
    public void onFactionMemberDeath(FactionMemberDeathEvent event) {
        final PLocatable locatable = event.getLocatable();
        final Location asBukkit = locatable.getBukkitLocation();
        final World world = asBukkit.getWorld();

        if (world == null) {
            plugin.getAresLogger().error("attempted to parse for world in FactionMemberDeathEvent but received null");
            return;
        }

        // reduce nether
        if (world.getEnvironment().equals(World.Environment.NETHER) && plugin.getConfiguration().getNetherPowerLossReduction() < event.getSubtractedDTR()) {
            event.setSubtractedDTR(plugin.getConfiguration().getNetherPowerLossReduction());
        }

        // reduce end
        if (world.getEnvironment().equals(World.Environment.THE_END) && plugin.getConfiguration().getEndPowerLossReduction() < event.getSubtractedDTR()) {
            event.setSubtractedDTR(plugin.getConfiguration().getEndPowerLossReduction());
        }

        // reduce event claim
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(locatable);
        if (insideClaim != null) {
            final ServerFaction owner = plugin.getFactionManager().getServerFactionById(insideClaim.getOwner());

            if (owner != null) {
                if (owner.getFlag().equals(ServerFaction.Flag.OUTPOST)) {
                    // TODO: Make this customizable, currently uses values for events
                    event.setSubtractedDTR(plugin.getConfiguration().getEventPowerLossReduction());
                }

                if (owner.getFlag().equals(ServerFaction.Flag.EVENT)) {
                    final IEvent activeEvent = plugin.getEventManager().getActiveEvents()
                            .stream()
                            .filter(e -> e.getOwner() != null && e.getOwner().equals(owner.getUniqueId()))
                            .findFirst()
                            .orElse(null);

                    if (activeEvent != null && plugin.getConfiguration().getEventPowerLossReduction() < event.getSubtractedDTR()) {
                        event.setSubtractedDTR(plugin.getConfiguration().getEventPowerLossReduction());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDetailedDeath(DetailedPlayerDeathEvent event) {
        final Player slain = event.getPlayer();
        final UUID uniqueId = slain.getUniqueId();

        if (recentlyPrintedDeathMessage.contains(uniqueId)) {
            return;
        }

        final EntityDamageEvent.DamageCause cause = event.getCause();
        final PlayerStatHolder slainStats = plugin.getStatsManager().getPlayerStatistics(slain.getUniqueId());
        final int slainKillCount = slainStats != null ? (int)slainStats.getStatistic(EStatisticType.KILL) : 0;
        final String prefix = ChatColor.DARK_RED + "RIP: " + ChatColor.RESET;
        final ChatColor entityColor = ChatColor.RED;
        final ChatColor detailColor = ChatColor.BLUE;
        final ChatColor bodyColor = ChatColor.YELLOW;
        final ChatColor heldItemBracket = ChatColor.GRAY;
        final String slainUsername = entityColor + slain.getName() + detailColor + "[" + slainKillCount + "]";
        String killerSuffix = null;
        String killerUsername;
        String killerItem;

        recentlyPrintedDeathMessage.add(uniqueId);
        new Scheduler(plugin).sync(() -> recentlyPrintedDeathMessage.remove(uniqueId)).delay(100L).run();

        if (event.getKiller() instanceof final Player killerPlayer && !killerPlayer.getUniqueId().equals(slain.getUniqueId())) {
            final PlayerStatHolder killerStats = plugin.getStatsManager().getPlayerStatistics(killerPlayer.getUniqueId());
            final int killerKillCount = killerStats != null ? (int)killerStats.getStatistic(EStatisticType.KILL) : 0;
            final ItemStack hand = killerPlayer.getInventory().getItemInMainHand();
            killerUsername = entityColor + killerPlayer.getName() + detailColor + "[" + killerKillCount + "]";
            killerItem = "their fists";

            if (!hand.getType().equals(Material.AIR)) {
                if (hand.hasItemMeta() && Objects.requireNonNull(hand.getItemMeta()).hasDisplayName()) {
                    killerItem = heldItemBracket + "[" + hand.getItemMeta().getDisplayName() + heldItemBracket + "]";
                } else {
                    killerItem = ChatColor.RESET + StringUtils.capitalize(hand.getType().name().replace("_", " ").toLowerCase());
                }
            }

            // player specific ENTITY_ATTACK message
            if (cause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " has been slain by " + killerUsername + bodyColor + " using " + killerItem);
                return;
            }

            // player specific PROJECTILE message
            if (cause.equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                final String distance = String.format("%.2f", killerPlayer.getLocation().distance(slain.getLocation()));
                Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " has been shot and killed by " + killerUsername + bodyColor + " from a distance of " + detailColor + distance + " blocks");
                return;
            }

            killerSuffix = bodyColor + " while fighting " + killerUsername + bodyColor + " using " + killerItem;
        } else if (event.getKiller() instanceof LivingEntity && !event.getKiller().getUniqueId().equals(slain.getUniqueId())) {
            killerSuffix = bodyColor + " while fighting a " + entityColor + StringUtils.capitalize(event.getKiller().getName().toLowerCase(Locale.ROOT).replaceAll("_", " "));
        }

        if (cause.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            final String entityName = event.getKiller().getName();
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " has been slain" + ((event.getKiller() != null) ? " by a " + entityColor + entityName : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
            final String entityName = event.getKiller().getName();
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " has been shot and killed" + ((event.getKiller() != null) ? " by a " + entityColor + entityName : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || cause.equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " blew up" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.CONTACT)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " ran in to a sharp object" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.DROWNING)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " ran out of oxygen" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.FALL)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " fell " + detailColor + String.format("%.2f", event.getFallDistance()) + " blocks" + bodyColor + " to their death" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.FALLING_BLOCK)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " forgot to pack their umbrella" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.FIRE) || cause.equals(EntityDamageEvent.DamageCause.FIRE_TICK) || cause.equals(EntityDamageEvent.DamageCause.HOT_FLOOR)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " didn't stop, drop and roll" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.LAVA)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " didn't notice the LiveLeak logo" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.FLY_INTO_WALL)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " wrapped themselves around a tree" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.FREEZE)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " got iced out" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.LIGHTNING)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " was struck by lightning" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.SONIC_BOOM)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " was vaporized" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.STARVATION)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " starved to death" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.SUFFOCATION)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " suffocated" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.VOID)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " slipped and fell in to the void" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        if (cause.equals(EntityDamageEvent.DamageCause.WITHER)) {
            Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " withered away" + ((killerSuffix != null) ? killerSuffix : ""));
            return;
        }

        Bukkit.broadcastMessage(prefix + slainUsername + bodyColor + " died");
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player killerPlayer = event.getKiller();
        final CombatLogger logger = event.getLogger();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(logger.getOwnerId());
        final ChatColor entityColor = ChatColor.RED;
        final ChatColor detailColor = ChatColor.BLUE;
        final ChatColor bodyColor = ChatColor.YELLOW;
        final String prefix = ChatColor.DARK_RED + "RIP: " + ChatColor.RESET;
        final String slainUsername = ChatColor.DARK_RED + "(Combat-Logger) " + entityColor + logger.getOwnerUsername() + ChatColor.RESET;
        final ChatColor heldItemBracket = ChatColor.GRAY;
        String killerUsername;
        String killerItem;
        String deathMessage = prefix + slainUsername + bodyColor + " died";

        if (killerPlayer != null) {
            final PlayerStatHolder killerStats = plugin.getStatsManager().getPlayerStatistics(killerPlayer.getUniqueId());
            final int killerKillCount = killerStats != null ? (int)killerStats.getStatistic(EStatisticType.KILL) : 0;
            final ItemStack hand = killerPlayer.getInventory().getItemInMainHand();
            killerUsername = entityColor + killerPlayer.getName() + detailColor + "[" + killerKillCount + "]";
            killerItem = "their fists";

            if (!hand.getType().equals(Material.AIR)) {
                if (hand.hasItemMeta() && Objects.requireNonNull(hand.getItemMeta()).hasDisplayName()) {
                    killerItem = heldItemBracket + "[" + hand.getItemMeta().getDisplayName() + heldItemBracket + "]";
                } else {
                    killerItem = ChatColor.RESET + StringUtils.capitalize(hand.getType().name().replace("_", " ").toLowerCase());
                }
            }

            deathMessage = prefix + slainUsername + bodyColor + " slain by " + killerUsername + bodyColor + " while using " + killerItem;
        }

        Bukkit.broadcastMessage(deathMessage);
        logger.getBukkitEntity().getWorld().strikeLightningEffect(logger.getBukkitEntity().getLocation());

        if (faction != null) {
            final FactionMemberDeathEvent memberDeathEvent = new FactionMemberDeathEvent(logger.getOwnerId(), logger.getOwnerUsername(), faction,
                    new PLocatable(
                            logger.getBukkitEntity().getLocation().getWorld().getName(),
                            logger.getBukkitEntity().getLocation().getX(),
                            logger.getBukkitEntity().getLocation().getY(),
                            logger.getBukkitEntity().getLocation().getZ(),
                            logger.getBukkitEntity().getLocation().getYaw(),
                            logger.getBukkitEntity().getLocation().getPitch()),
                    1.0, plugin.getConfiguration().getFreezeDuration());

            Bukkit.getPluginManager().callEvent(memberDeathEvent);

            if (!(plugin.getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_1) || plugin.getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2))) {
                faction.setDtr(Math.max((faction.getDtr() - memberDeathEvent.getSubtractedDTR()), -0.99));
                faction.addTimer(new FTimer(ETimerType.FREEZE, memberDeathEvent.getFreezeDuration()));
            }
        }

        new Scheduler(plugin).async(() -> {
            final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().loadPlayer(Filters.eq("uuid", logger.getOwnerId().toString()), false);

            if (factionPlayer != null) {
                factionPlayer.setResetOnJoin(true);
                plugin.getPlayerManager().savePlayer(factionPlayer);
            }
        }).run();
    }

    /**
     * Handles player deaths
     *
     * @param event PlayerDeathEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player slain = event.getEntity();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(slain.getUniqueId());

        // Apply member death if they're in a faction
        if (faction != null) {
            final ServerStateManager serverStateManager = plugin.getServerStateManager();
            final FactionMemberDeathEvent memberDeathEvent = new FactionMemberDeathEvent(slain.getUniqueId(), slain.getName(), faction, new PLocatable(slain), 1.0, plugin.getConfiguration().getFreezeDuration());

            Bukkit.getPluginManager().callEvent(memberDeathEvent);

            if (serverStateManager.getCurrentState().equals(EServerState.SOTW) || serverStateManager.getCurrentState().equals(EServerState.NORMAL)) {
                faction.setDtr(Math.max((faction.getDtr() - memberDeathEvent.getSubtractedDTR()), -0.99));
                faction.addTimer(new FTimer(ETimerType.FREEZE, memberDeathEvent.getFreezeDuration()));
            }
        }

        event.setDeathMessage(null);
        slain.getWorld().strikeLightningEffect(slain.getLocation());
    }
}

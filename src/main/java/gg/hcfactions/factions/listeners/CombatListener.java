package gg.hcfactions.factions.listeners;

import com.mongodb.client.model.Filters;
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
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerLingeringSplashEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerSplashPlayerEvent;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Objects;

public record CombatListener(@Getter Factions plugin) implements Listener {
    /**
     * Handles enforcing physical combat restrictions
     *
     * @param event PlayerDamagePlayerEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPhysicalDamage(PlayerDamagePlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        final FactionPlayer attackerProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null || attackedProfile == null) {
            return;
        }

        if (attackerProfile.hasTimer(ETimerType.PROTECTION)) {
            attacker.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_ATTACK_PVP_PROT.getErrorDescription());
            event.setCancelled(true);
            return;
        }

        if (attackedProfile.hasTimer(ETimerType.PROTECTION)) {
            attacker.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_ATTACK_PVP_PROT_OTHER.getErrorDescription());
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

        final PlayerFaction attackerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(attacker);

        if (attackerFaction != null && attackerFaction.getMember(attacked.getUniqueId()) != null) {
            FMessage.printCanNotAttackFactionMembers(attacker);
            event.setCancelled(true);
        }
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

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        final FactionPlayer attackerProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null || attackedProfile == null) {
            return;
        }

        if (attackerProfile.hasTimer(ETimerType.PROTECTION)) {
            event.setCancelled(true);
            return;
        }

        if (attackedProfile.hasTimer(ETimerType.PROTECTION)) {
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

        final PlayerFaction attackerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(attacker.getUniqueId());

        if (attackerFaction != null && attackerFaction.getMember(attacked.getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLingeringSplash(PlayerLingeringSplashEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final AreaEffectCloud cloud = event.getCloud();

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        if (cloud == null || cloud.getBasePotionData().getType().getEffectType() == null) {
            return;
        }

        if (!cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.HARM) &&
                !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.WEAKNESS) &&
                !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.SLOW) &&
                !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.POISON) &&
                !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.SLOW_FALLING)) {
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
                    effect.getType().equals(PotionEffectType.SLOW) ||
                    effect.getType().equals(PotionEffectType.WEAKNESS) ||
                    effect.getType().equals(PotionEffectType.HARM) ||
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

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final CombatLogger logger = event.getLogger();
        final Player killer = event.getKiller();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(logger.getOwnerId());
        final String prefix = ChatColor.RED + "RIP:" + ChatColor.RESET;
        final String slainUsername = ChatColor.DARK_RED + "(Combat-Logger) " + ChatColor.GOLD + logger.getOwnerUsername() + ChatColor.RESET;
        final ChatColor cA = ChatColor.RED;
        final ChatColor cB = ChatColor.BLUE;
        String deathMessage = prefix + " " + slainUsername + cA + " died";

        if (event.getKiller() != null) {
            final PlayerStatHolder killerStats = plugin.getStatsManager().getPlayerStatistics(killer.getUniqueId());
            final int killerKillCount = (int)(killerStats != null ? killerStats.getStatistic(EStatisticType.KILL) : 0);

            String hand = ChatColor.RESET + "their fists";

            if (!killer.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                if (killer.getInventory().getItemInMainHand().getItemMeta() != null && killer.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) {
                    hand = ChatColor.GRAY + "[" + killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName() + ChatColor.GRAY + "]";
                } else {
                    hand = ChatColor.RESET + StringUtils.capitalize(killer.getItemInHand().getType().name().replace("_", " ").toLowerCase());
                }
            }

            deathMessage = prefix + " " + slainUsername + cA + " slain by " + ChatColor.GOLD + killer.getName() + ChatColor.BLUE + "[" + killerKillCount + "]" + cA + " while using " + hand;
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

    /**
     * Handles player deaths
     *
     * @param event PlayerDeathEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player slain = event.getEntity();
        final Player killer = slain.getKiller();
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

        if (slain.getLastDamageCause() == null) {
            event.setDeathMessage(null);
            return;
        }

        final PlayerStatHolder slainStats = plugin.getStatsManager().getPlayerStatistics(slain.getUniqueId());
        final int slainKillCount = slainStats != null ? (int)slainStats.getStatistic(EStatisticType.KILL) : 0;
        final EntityDamageEvent.DamageCause reason = slain.getLastDamageCause().getCause();
        final String prefix = ChatColor.RED + "RIP:" + ChatColor.RESET;
        final String slainUsername = ChatColor.GOLD + slain.getName() + ChatColor.BLUE + "[" + slainKillCount + "]" + ChatColor.RESET;
        final ChatColor cA = ChatColor.RED;
        final ChatColor cB = ChatColor.BLUE;

        slain.getWorld().strikeLightningEffect(slain.getLocation());

        if (killer != null && !killer.getUniqueId().equals(slain.getUniqueId())) {
            final PlayerStatHolder killerStats = plugin.getStatsManager().getPlayerStatistics(killer.getUniqueId());
            final int killerKillCount = killerStats != null ? (int)killerStats.getStatistic(EStatisticType.KILL) + 1 : 0;
            final String killerUsername = ChatColor.GOLD + killer.getName() + ChatColor.BLUE + "[" + killerKillCount + ChatColor.BLUE + "]" +  ChatColor.RESET;
            String hand = ChatColor.RESET + "their fists";

            if (!killer.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                if (killer.getInventory().getItemInMainHand().hasItemMeta() && Objects.requireNonNull(killer.getInventory().getItemInMainHand().getItemMeta()).hasDisplayName()) {
                    hand = ChatColor.GRAY + "[" + killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName() + ChatColor.GRAY + "]";
                } else {
                    hand = ChatColor.RESET + StringUtils.capitalize(killer.getInventory().getItemInMainHand().getType().name().replace("_", " ").toLowerCase());
                }
            }

            final String defaultDeathMessage = prefix + " " + slainUsername + cA + " slain by " + killerUsername + cA + " while using " + hand;

            if (reason.equals(EntityDamageEvent.DamageCause.FALL)) {
                if (slain.getFallDistance() > 3.0) {
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " fell " + cB + String.format("%.2f", slain.getFallDistance()) + " blocks" + cA + " to their death while fighting " + killerUsername);
                } else {
                    event.setDeathMessage(defaultDeathMessage);
                }

                return;
            }

            if (reason.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                event.setDeathMessage(defaultDeathMessage);
                return;
            }

            if (reason.equals(EntityDamageEvent.DamageCause.PROJECTILE) && slain.getLastDamageCause() instanceof final EntityDamageByEntityEvent pveEvent) {
                final Projectile projectile = (Projectile) pveEvent.getDamager();

                if (projectile.getShooter() instanceof final LivingEntity shooter) {
                    final String distance = String.format("%.2f", shooter.getLocation().distance(slain.getLocation()));
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " was shot and killed by " + killerUsername + cA + " from a distance of " + cB + distance + " blocks");
                    return;
                }
            }

            switch (reason) {
                case FIRE:
                case FIRE_TICK:
                case LAVA:
                case MELTING:
                case DRAGON_BREATH:
                case HOT_FLOOR:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " burned to death while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case MAGIC:
                case CUSTOM:
                case SUICIDE:
                case POISON:
                case LIGHTNING:
                case THORNS:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " died by magic while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case DROWNING:
                case SUFFOCATION:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " suffocated while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION:
                case SONIC_BOOM:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " blew up while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case VOID:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " fell in to the void while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case WITHER:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " withered away while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case STARVATION:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " starved to death while fighting " + killerUsername + cA + " while using " + hand);
                    break;
                case FLY_INTO_WALL:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " slammed in to a wall while fight " + killerUsername + cA + " while using " + hand);
                case FREEZE:
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " froze to death while fighting " + killerUsername + cA + " while using " + hand);
                default:
                    event.setDeathMessage(defaultDeathMessage);
                    break;
            }

            return;
        }

        if (reason.equals(EntityDamageEvent.DamageCause.FALL)) {
            if (slain.getFallDistance() >= 3.0) {
                event.setDeathMessage(prefix + " " + slainUsername + cA + " fell " + cB + String.format("%.2f", slain.getFallDistance()) + " blocks" + cA + " to their death");
            } else {
                event.setDeathMessage(prefix + " " + slainUsername + cA + " died");
            }

            return;
        }

        switch (reason) {
            case FIRE:
            case FIRE_TICK:
            case LAVA:
            case MELTING:
            case DRAGON_BREATH:
            case HOT_FLOOR:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " burned to death");
                break;
            case MAGIC:
            case CUSTOM:
            case POISON:
            case LIGHTNING:
            case THORNS:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " died by magic");
                break;
            case DROWNING:
            case SUFFOCATION:
            case CRAMMING:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " suffocated");
                break;
            case CONTACT:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " hugged a cactus");
            case ENTITY_EXPLOSION:
            case BLOCK_EXPLOSION:
            case SONIC_BOOM:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " blew up");
                break;
            case VOID:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " fell in to the void");
                break;
            case WITHER:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " withered away");
                break;
            case STARVATION:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " starved to death");
                break;
            case FLY_INTO_WALL:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " slammed in to a wall");
            case FREEZE:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " froze to death");
            default:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " died");
                break;
        }
    }
}

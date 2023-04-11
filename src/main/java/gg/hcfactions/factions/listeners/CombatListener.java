package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.faction.FactionMemberDeathEvent;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerLingeringSplashEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerSplashPlayerEvent;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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

        if (attackerProfile.getCurrentClaim() != null) {
            final Claim claim = attackerProfile.getCurrentClaim();
            final IFaction owner = plugin.getFactionManager().getFactionById(claim.getOwner());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction) owner;

                if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    FMessage.printCanNotFightInClaim(attacker, sf.getDisplayName());
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final Claim claim = attackedProfile.getCurrentClaim();
            final IFaction owner = plugin.getFactionManager().getFactionById(claim.getOwner());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction) owner;

                if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    FMessage.printCanNotFightInClaim(attacker, sf.getDisplayName());
                    event.setCancelled(true);
                    return;
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
        if (!(event.getCombuster() instanceof Projectile) || !(event.getEntity() instanceof Player)) {
            return;
        }

        final Player attacked = (Player) event.getEntity();
        final Projectile projectile = (Projectile) event.getCombuster();
        final ProjectileSource source = projectile.getShooter();

        if (!(source instanceof Player)) {
            return;
        }

        final Player attacker = (Player) source;

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

        if (attackerProfile.getCurrentClaim() != null) {
            final Claim claim = attackerProfile.getCurrentClaim();
            final IFaction owner = plugin.getFactionManager().getFactionById(claim.getOwner());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction) owner;

                if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final Claim claim = attackedProfile.getCurrentClaim();
            final IFaction owner = plugin.getFactionManager().getFactionById(claim.getOwner());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction) owner;

                if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    event.setCancelled(true);
                    return;
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

        if (attackerProfile.getCurrentClaim() != null) {
            final Claim claim = attackerProfile.getCurrentClaim();
            final IFaction owner = plugin.getFactionManager().getFactionById(claim.getOwner());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction) owner;

                if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final Claim claim = attackedProfile.getCurrentClaim();
            final IFaction owner = plugin.getFactionManager().getFactionById(claim.getOwner());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction) owner;

                if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
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
    @EventHandler(priority = EventPriority.HIGH)
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
                    effect.getType().equals(PotionEffectType.HARM)) {

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

        if (attackerProfile.getCurrentClaim() != null) {
            final Claim claim = attackerProfile.getCurrentClaim();
            final IFaction owner = plugin.getFactionManager().getFactionById(claim.getOwner());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction) owner;

                if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final Claim claim = attackedProfile.getCurrentClaim();
            final IFaction owner = plugin.getFactionManager().getFactionById(claim.getOwner());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction) owner;

                if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /* @EventHandler (priority = EventPriority.LOW)
    public void onClassConsume(ConsumeClassItemEvent event) {
        final ClassAddon addon = (ClassAddon)getPlugin().getAddonManager().get(ClassAddon.class);

        if (addon == null) {
            return;
        }

        final Player player = event.getPlayer();
        final AresClass playerClass = addon.getManager().getCurrentClass(player);
        final FactionPlayer profile = getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            player.sendMessage(ChatColor.RED + "You can not use consumable effects while you have PvP Protection");
            event.setCancelled(true);
            return;
        }

        // TODO: Eventually fix this but it's expected only bard will give effects for now
        if (!(playerClass instanceof Bard)) {
            return;
        }

        final Bard bard = (Bard)playerClass;

        if (event.getConsumable().getApplicationType().equals(Consumable.ConsumableApplicationType.INDIVIDUAL)) {
            return;
        }

        if (event.getConsumable().getApplicationType().equals(Consumable.ConsumableApplicationType.ALL)) {
            final List<Player> friendlies = FactionUtil.getNearbyFriendlies(plugin, player, bard.getRange());
            final List<Player> enemies = FactionUtil.getNearbyEnemies(plugin, player, bard.getRange());

            friendlies.forEach(friendly -> event.getAffectedPlayers().put(friendly.getUniqueId(), true));
            enemies.forEach(enemy -> event.getAffectedPlayers().put(enemy.getUniqueId(), false));

            return;
        }

        if (event.getConsumable().getApplicationType().equals(Consumable.ConsumableApplicationType.FRIENDLY_ONLY)) {
            final List<Player> friendlies = FactionUtil.getNearbyFriendlies(plugin, player, bard.getRange());
            friendlies.forEach(friendly -> event.getAffectedPlayers().put(friendly.getUniqueId(), true));
            return;
        }

        if (event.getConsumable().getApplicationType().equals(Consumable.ConsumableApplicationType.ENEMY_ONLY)) {
            final List<Player> enemies = FactionUtil.getNearbyEnemies(plugin, player, bard.getRange());
            enemies.forEach(enemy -> event.getAffectedPlayers().put(enemy.getUniqueId(), false));
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerDamageLogger(PlayerDamageCombatLoggerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final CombatLogger combatLogger = event.getLogger();
        final FactionPlayer factionPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final PlayerFaction playerFaction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            return;
        }

        if (factionPlayer.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            player.sendMessage(ChatColor.RED + "You can not attack players while you have PvP Protection");
            event.setCancelled(true);
            return;
        }

        if (factionPlayer.getCurrentClaim() != null) {
            final ServerFaction serverFaction = plugin.getFactionManager().getServerFactionById(factionPlayer.getCurrentClaim().getOwnerId());

            if (serverFaction != null && serverFaction.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                player.sendMessage(ChatColor.RED + "PvP is disabled in " + ChatColor.RESET + serverFaction.getDisplayName());
                event.setCancelled(true);
                return;
            }
        }

        if (playerFaction != null && playerFaction.isMember(combatLogger.getOwnerId())) {
            player.sendMessage(ChatColor.RED + "PvP is disabled between " + ChatColor.RESET + "Faction Members");
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
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(logger.getOwnerId());
        final String prefix = ChatColor.RED + "RIP:" + ChatColor.RESET;
        final String slainUsername = ChatColor.DARK_RED + "(Combat-Logger) " + ChatColor.GOLD + logger.getOwnerUsername() + ChatColor.RESET;
        final ChatColor cA = ChatColor.RED;
        final ChatColor cB = ChatColor.BLUE;
        String deathMessage = prefix + " " + slainUsername + cA + " died";

        if (event.getKiller() != null) {
            String hand = ChatColor.RESET + "their fists";

            if (killer.getItemInHand() != null && !killer.getItemInHand().getType().equals(Material.AIR)) {
                if (killer.getItemInHand().hasItemMeta() && killer.getItemInHand().getItemMeta().hasDisplayName()) {
                    hand = ChatColor.GRAY + "[" + killer.getItemInHand().getItemMeta().getDisplayName() + ChatColor.GRAY + "]";
                } else {
                    hand = ChatColor.RESET + StringUtils.capitaliseAllWords(killer.getItemInHand().getType().name().replace("_", " ").toLowerCase());
                }
            }

            deathMessage = prefix + " " + slainUsername + cA + " slain by " + killer.getName() + cA + " while using " + hand;
        }

        Bukkit.broadcastMessage(deathMessage);
        logger.getBukkitEntity().getWorld().strikeLightningEffect(logger.getBukkitEntity().getLocation());

        if (faction != null) {
            final ServerStateAddon serverStateAddon = (ServerStateAddon)plugin.getAddonManager().get(ServerStateAddon.class);
            final FactionMemberDeathEvent memberDeathEvent = new FactionMemberDeathEvent(logger.getOwnerId(), logger.getOwnerUsername(), faction,
                    new PLocatable(
                            logger.getBukkitEntity().getLocation().getWorld().getName(),
                            logger.getBukkitEntity().getLocation().getX(),
                            logger.getBukkitEntity().getLocation().getY(),
                            logger.getBukkitEntity().getLocation().getZ(),
                            logger.getBukkitEntity().getLocation().getYaw(),
                            logger.getBukkitEntity().getLocation().getPitch()),
                    1.0, plugin.getTimerManager().getConfig().getFactionFreezeDuration());

            Bukkit.getPluginManager().callEvent(memberDeathEvent);

            if (serverStateAddon != null && !(serverStateAddon.getConfig().getCurrentState().equals(ServerStateAddon.State.EOTW_PHASE_1) || serverStateAddon.getConfig().getCurrentState().equals(ServerStateAddon.State.EOTW_PHASE_2))) {
                faction.updateDTR(faction.getDTR() - memberDeathEvent.getSubtractedDTR());
                faction.addTimer(new FactionTimer(FactionTimer.FactionTimerType.FREEZE, memberDeathEvent.getFreezeDuration()));
            }
        }

        new Scheduler(plugin).async(() -> {

            final FactionPlayer factionPlayer = plugin.getPlayerManager().load(logger.getUniqueID());

            if (factionPlayer != null) {
                factionPlayer.setResetOnJoin(true);
                factionPlayer.save();
            }

        }).run();
    } */

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

        if (world.getEnvironment().equals(World.Environment.NETHER) && plugin.getConfiguration().isReducePowerLossInNether()) {
            event.setSubtractedDTR(event.getSubtractedDTR() / 2.0);
        }

        if (world.getEnvironment().equals(World.Environment.THE_END) && plugin.getConfiguration().isReducePowerLossInEnd()) {
            event.setSubtractedDTR(event.getSubtractedDTR() / 2.0);
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
                faction.setDtr(faction.getDtr() - memberDeathEvent.getSubtractedDTR());
                faction.addTimer(new FTimer(ETimerType.FREEZE, memberDeathEvent.getFreezeDuration()));
            }
        }

        if (slain.getLastDamageCause() == null) {
            event.setDeathMessage(null);
            return;
        }

        final EntityDamageEvent.DamageCause reason = slain.getLastDamageCause().getCause();
        final String prefix = ChatColor.RED + "RIP:" + ChatColor.RESET;
        final String slainUsername = ChatColor.GOLD + slain.getName() + ChatColor.RESET;
        final ChatColor cA = ChatColor.RED;
        final ChatColor cB = ChatColor.BLUE;

        slain.getWorld().strikeLightningEffect(slain.getLocation());

        if (killer != null && !killer.getUniqueId().equals(slain.getUniqueId())) {
            final String killerUsername = ChatColor.GOLD + killer.getName() + ChatColor.RESET;
            String hand = ChatColor.RESET + "their fists";

            if (!killer.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                if (killer.getInventory().getItemInMainHand().hasItemMeta() && Objects.requireNonNull(killer.getInventory().getItemInMainHand().getItemMeta()).hasDisplayName()) {
                    hand = ChatColor.GRAY + "[" + killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName() + ChatColor.GRAY + "]";
                } else {
                    hand = ChatColor.RESET + StringUtils.capitalize(killer.getItemInHand().getType().name().replace("_", " ").toLowerCase());
                }
            }

            final String defaultDeathMessage = prefix + " " + slainUsername + cA + " slain by " + killerUsername + cA + " while using " + hand;

            if (reason.equals(EntityDamageEvent.DamageCause.FALL)) {
                final double distance = Math.floor(slain.getFallDistance());

                if (distance > 3.0) {
                    event.setDeathMessage(prefix + " " + slainUsername + cA + " fell " + cB + distance + " blocks" + cA + " to their death while fighting " + killerUsername);
                } else {
                    event.setDeathMessage(defaultDeathMessage);
                }

                return;
            }

            if (reason.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                event.setDeathMessage(defaultDeathMessage);
                return;
            }

            if (reason.equals(EntityDamageEvent.DamageCause.PROJECTILE) && slain.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                final EntityDamageByEntityEvent pveEvent = (EntityDamageByEntityEvent) slain.getLastDamageCause();
                final Projectile projectile = (Projectile) pveEvent.getDamager();

                if (projectile.getShooter() instanceof LivingEntity) {
                    final LivingEntity shooter = (LivingEntity) projectile.getShooter();
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
                default:
                    event.setDeathMessage(defaultDeathMessage);
                    break;
            }

            return;
        }

        if (reason.equals(EntityDamageEvent.DamageCause.FALL)) {
            final double distance = Math.floor(slain.getFallDistance());

            if (distance >= 3.0) {
                event.setDeathMessage(prefix + " " + slainUsername + cA + " fell " + cB + distance + " blocks" + cA + " to their death");
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
            default:
                event.setDeathMessage(prefix + " " + slainUsername + cA + " died");
                break;
        }
    }
}

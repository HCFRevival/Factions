package gg.hcfactions.factions.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.*;
import gg.hcfactions.factions.models.classes.EEffectScoreboardMapping;
import gg.hcfactions.factions.models.classes.EPlayerHand;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import gg.hcfactions.factions.models.classes.impl.Archer;
import gg.hcfactions.factions.models.classes.impl.Diver;
import gg.hcfactions.factions.models.classes.impl.Rogue;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.remap.ERemappedEffect;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.utils.Colors;
import gg.hcfactions.libs.bukkit.utils.Players;
import gg.hcfactions.libs.bukkit.utils.Worlds;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ClassListener implements Listener {
    @Getter public final Factions plugin;
    @Getter public final Set<UUID> recentlyLoggedIn;

    public ClassListener(Factions plugin) {
        this.plugin = plugin;
        this.recentlyLoggedIn = Sets.newConcurrentHashSet();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        recentlyLoggedIn.add(player.getUniqueId());

        new Scheduler(plugin).sync(() -> {
            final IClass playerClass = plugin.getClassManager().getClassByArmor(player);

            if (playerClass != null) {
                final ClassReadyEvent readyEvent = new ClassReadyEvent(player, playerClass);
                readyEvent.setMessagePrinted(false);
                Bukkit.getPluginManager().callEvent(readyEvent);
            }

            recentlyLoggedIn.remove(player.getUniqueId());
        }).delay(3L).run();

        player.getActivePotionEffects()
                .stream()
                .filter(e -> e.getDuration() > 25000)
                .forEach(infE -> player.removePotionEffect(infE.getType()));
    }

    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (event.getOldItem() != null && event.getNewItem() != null && event.getOldItem().getType().equals(event.getNewItem().getType())) {
            return;
        }

        final Player player = event.getPlayer();

        if (recentlyLoggedIn.contains(player.getUniqueId())) {
            return;
        }

        final IClass actualClass = getPlugin().getClassManager().getCurrentClass(player);
        final IClass expectedClass = getPlugin().getClassManager().getClassByArmor(player);

        if (expectedClass != null) {
            if (actualClass != null) {
                final ClassDeactivateEvent deactivateEvent = new ClassDeactivateEvent(player, actualClass);
                Bukkit.getPluginManager().callEvent(deactivateEvent);
                actualClass.deactivate(player);
            }

            final ClassReadyEvent readyEvent = new ClassReadyEvent(player, expectedClass);
            readyEvent.setMessagePrinted(true);
            Bukkit.getPluginManager().callEvent(readyEvent);

            return;
        }

        if (actualClass != null) {
            final ClassDeactivateEvent deactivateEvent = new ClassDeactivateEvent(player, actualClass);
            Bukkit.getPluginManager().callEvent(deactivateEvent);
            actualClass.deactivate(player);
        } else {
            final ClassUnreadyEvent unreadyEvent = new ClassUnreadyEvent(player);
            Bukkit.getPluginManager().callEvent(unreadyEvent);
        }
    }

    @EventHandler /* Removes player from class upon disconnect */
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (playerClass == null) {
            return;
        }

        final ClassDeactivateEvent deactivateEvent = new ClassDeactivateEvent(player, playerClass);
        Bukkit.getPluginManager().callEvent(deactivateEvent);

        playerClass.deactivate(player);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        final Entity damaged = event.getEntity();
        double damage = event.getFinalDamage();

        if (event.isCancelled()) {
            return;
        }

        if (!(damager instanceof Arrow)) {
            return;
        }

        if (!(damaged instanceof LivingEntity)) {
            return;
        }

        final Projectile arrow = (Projectile)damager;

        if (!(arrow.getShooter() instanceof final Player player)) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof final Archer archerClass)) {
            return;
        }

        final double maxDamage = archerClass.getMaxDealtDamage();
        final double damagePerBlock = archerClass.getDamagePerBlock();
        final Location locA = player.getLocation().clone();
        final Location locB = damaged.getLocation().clone();

        // flatten to 2D then calculate
        locA.setY(0.0);
        locB.setY(0.0);
        final double distance = locA.distance(locB);

        archerClass.addHit(player, (LivingEntity) damaged, 10); // TODO: Make configurable

        final int hitCount = archerClass.getHitCount(player, (LivingEntity) damaged);
        final double distanceDamage = (damagePerBlock * distance);
        final double consecutiveDamage = (archerClass.getConsecutiveBase() * (hitCount * archerClass.getConsecutiveMultiplier()));

        final double finalDamage = Math.min((distanceDamage + consecutiveDamage + damage), maxDamage);

        event.setDamage(finalDamage);

        final double healthPre = ((LivingEntity) damaged).getHealth();

        if (damaged instanceof final Player playerDamaged) {
            playerDamaged.sendMessage(ChatColor.RED + "You have been shot by an " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "ARCHER!");
        }

        new Scheduler(plugin).sync(() -> {
            final double healthPost = ((LivingEntity) damaged).getHealth();
            final double diff = (healthPre - healthPost) / 2;

            final String name = ((LivingEntity)damaged).hasPotionEffect(PotionEffectType.INVISIBILITY) ? ChatColor.GRAY + "? ? ?" :
                    (damaged instanceof Player) ? ChatColor.GOLD + damaged.getName() :
                            ChatColor.GOLD + WordUtils.capitalize(damaged.getType().name().toLowerCase().replace("_", " "));

            player.sendMessage(ChatColor.YELLOW + "Your arrow has" + ChatColor.RED + " pierced " + name +
                    ChatColor.YELLOW + " from a distance of " + ChatColor.BLUE + String.format("%.2f", distance) + " blocks " +
                    ChatColor.YELLOW + "(" + ChatColor.RED + String.format("%.2f", diff) + " ❤" + (hitCount > 1 ? ChatColor.GOLD + " x" + hitCount : "")
                    + ChatColor.YELLOW + ")");

        }).delay(1L).run();
    }

    @EventHandler
    public void onSeaCall(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();

        if (event.getItem() == null) {
            return;
        }

        final ItemStack hand = event.getItem();
        final EPlayerHand handType = (event.getHand() == null || event.getHand().equals(EquipmentSlot.HAND) ? EPlayerHand.MAIN : EPlayerHand.OFFHAND);
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        // Prevents ASSERTION ERROR: TRAP
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        if (!hand.getType().equals(Material.HEART_OF_THE_SEA)) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof final Diver diver)) {
            return;
        }

        if (!world.getEnvironment().equals(World.Environment.NORMAL)) {
            player.sendMessage(ChatColor.RED + "It can not rain in this world");
            return;
        }

        if (world.hasStorm()) {
            player.sendMessage(ChatColor.RED + "It is already raining");
            return;
        }

        if (diver.hasSeaCallCooldown(player)) {
            final long timeUntilNextCall = (diver.getSeaCallCooldowns().getOrDefault(player.getUniqueId(), 0L) - Time.now());
            player.sendMessage(ChatColor.RED + "Heart of the Sea is locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(timeUntilNextCall) + ChatColor.RED + "s");
            return;
        }

        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else if (handType.equals(EPlayerHand.MAIN)) {
            player.getInventory().setItemInMainHand(null);
        } else {
            player.getInventory().setItemInOffHand(null);
        }

        world.setStorm(true);
        world.setThunderDuration(diver.getSeaCallDuration()*20);
        world.setWeatherDuration(diver.getSeaCallDuration()*20);

        // The sea calls for johnsama...
        final RankService rankService = (RankService) plugin.getService(RankService.class);
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        Bukkit.getOnlinePlayers().stream().filter(op -> op.getWorld().equals(world)).forEach(worldPlayer -> {
            String displayName = rankService.getFormattedName(player);

            if (faction != null) {
                if (faction.isMember(worldPlayer)) {
                    displayName = ChatColor.DARK_GREEN + "[" + faction.getName() + "] " + FMessage.P_NAME + rankService.getFormattedName(player);
                } else {
                    displayName = FMessage.LAYER_2 + "[" + FMessage.LAYER_1 + faction.getName() + FMessage.LAYER_2 + "] " + FMessage.P_NAME + rankService.getFormattedName(player);
                }
            }

            Players.playSound(worldPlayer, Sound.ITEM_GOAT_HORN_SOUND_3);

            worldPlayer.sendMessage(ChatColor.RESET + " ");
            worldPlayer.sendMessage(Colors.DARK_AQUA.toBukkit() + "The sea calls for " + displayName);
            worldPlayer.sendMessage(ChatColor.RESET + " ");
        });

        diver.getSeaCallCooldowns().put(player.getUniqueId(), (Time.now() + (diver.getSeaCallCooldown()*1000L)));
    }

    @EventHandler
    public void onConsume(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getItem() == null) {
            return;
        }

        final ItemStack hand = event.getItem();
        final EPlayerHand handType = (event.getHand() == null || event.getHand().equals(EquipmentSlot.HAND) ? EPlayerHand.MAIN : EPlayerHand.OFFHAND);
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        // Prevents ASSERTION ERROR: TRAP
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (hand == null || hand.getType().equals(Material.AIR)) {
            return;
        }

        if (playerClass == null || playerClass.getConsumables().isEmpty()) {
            return;
        }

        final IConsumeable consumable = playerClass.getConsumableByMaterial(hand.getType());

        if (consumable == null) {
            return;
        }

        // Prevents the physical item from being used
        if (consumable.getMaterial().equals(Material.ENDER_EYE)) {
            event.setCancelled(true);
        }

        if (consumable.hasCooldown(player)) {
            player.sendMessage(ChatColor.RED + WordUtils.capitalize(consumable.getEffectType().getName().toLowerCase().replace("_", " ")) + " is locked for " +
                    ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(consumable.getPlayerCooldown(player) - Time.now()) + ChatColor.RED + "s");

            return;
        }

        final ConsumeClassItemEvent consumeClassItemEvent = new ConsumeClassItemEvent(player, playerClass, consumable);
        Bukkit.getPluginManager().callEvent(consumeClassItemEvent);

        if (consumeClassItemEvent.isCancelled()) {
            return;
        }

        if (factionPlayer != null) {
            if (!factionPlayer.hasTimer(ETimerType.COMBAT)) {
                FMessage.printCombatTag(player, (plugin.getConfiguration().getAttackerCombatTagDuration()*1000L));
            }

            factionPlayer.addTimer(new FTimer(ETimerType.COMBAT, plugin.getConfiguration().getAttackerCombatTagDuration()));
        }

        consumable.consume(player, event.getItem(), handType);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();

        if (!event.getType().equals(PlayerDamagePlayerEvent.DamageType.PHYSICAL)) {
            return;
        }

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        final Location attackerLocation = attacker.getLocation();
        final Location attackedLocation = attacked.getLocation();

        attackerLocation.setPitch(0F);
        attackedLocation.setPitch(0F);

        final Vector attackerDirection = attackerLocation.getDirection();
        final Vector attackedDirection = attackedLocation.getDirection();
        final double dot = attackerDirection.dot(attackedDirection);
        final UUID attackerUUID = attacker.getUniqueId();

        if (attacked.getHealth() <= 0.0 || attacked.isDead()) {
            return;
        }

        if (!attacker.getInventory().getItemInHand().getType().equals(Material.GOLDEN_SWORD)) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(attacker);

        if (!(playerClass instanceof final Rogue rogue)) {
            return;
        }

        if (rogue.hasBackstabCooldown(attacker)) {
            final long timeUntilNextAttack = (rogue.getBackstabCooldowns().getOrDefault(attacker.getUniqueId(), 0L) - Time.now());
            attacker.sendMessage(ChatColor.RED + "Backstab is locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(timeUntilNextAttack) + ChatColor.RED + "s");
            return;
        }

        if (dot >= 0.825 && dot <= 1.0) {
            final RogueBackstabEvent backstabEvent = new RogueBackstabEvent(attacker, attacked);
            Bukkit.getPluginManager().callEvent(backstabEvent);

            if (backstabEvent.isCancelled()) {
                return;
            }

            new Scheduler(plugin).sync(() -> {
                if (attacked.isDead() || attacked.getHealth() <= 0.0) {
                    return;
                }

                attacked.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "BACKSTABBED!");

                for (int i = 1; i <= 3; i++) {
                    new Scheduler(plugin).sync(() -> {
                        if (!attacked.isOnline() || attacked.isDead() || attacked.getHealth() <= 0.0) {
                            return;
                        }

                        // Consume absorption hearts first
                        if (attacked.getAbsorptionAmount() > 0) {
                            final double health = Math.max((attacked.getAbsorptionAmount() - rogue.getBackstabDamage()), 0.0);
                            attacked.damage(0.0);
                            attacked.setAbsorptionAmount(health);
                            return;
                        }

                        final double health = Math.max((attacked.getHealth() - rogue.getBackstabDamage()), 0.0);
                        attacked.damage(0.0); // Create the illusion of the player taking flinching damage
                        attacked.setHealth(health);
                    }).delay(((long) i * rogue.getBackstabTickrate())).run();
                }

                attacker.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                Objects.requireNonNull(attackedLocation.getWorld()).spawnParticle(Particle.HEART, attackedLocation.getX(), attackedLocation.getY() + 1.0, attackerLocation.getZ(), 3, 2.0, 2.0, 2.0);
                Worlds.playSound(attackedLocation, Sound.ENTITY_ITEM_BREAK);

                if (!attacked.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    attacker.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.RED + "backstabbed" + ChatColor.GOLD + " " + attacked.getName());
                } else {
                    attacker.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.RED + "backstabbed" + ChatColor.GRAY + " ? ? ?");
                }

                final long nextBackstab = Time.now() + (rogue.getBackstabCooldown() * 1000L);
                rogue.getBackstabCooldowns().put(attackerUUID, nextBackstab);

                new Scheduler(plugin).sync(() -> {
                    rogue.getBackstabCooldowns().remove(attackerUUID);

                    if (Bukkit.getPlayer(attackerUUID) != null) {
                        Objects.requireNonNull(Bukkit.getPlayer(attackerUUID)).sendMessage(ChatColor.GREEN + rogue.getName() + " backstab is ready");
                        Players.playSound(Objects.requireNonNull(Bukkit.getPlayer(attackerUUID)), Sound.BLOCK_NOTE_BLOCK_HARP); // TODO: Test sound and match to original
                    }
                }).delay(rogue.getBackstabCooldown() * 20L).run();
            }).run();
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onTridentLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Trident)) {
            return;
        }

        if (!(event.getEntity().getShooter() instanceof final Player player)) {
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof Diver)) {
            player.sendMessage(ChatColor.RED + "Tridents can only be used by the Diver class");
            event.setCancelled(true);
            return;
        }

        if (factionPlayer != null) {
            final FTimer existing = factionPlayer.getTimer(ETimerType.TRIDENT);

            if (existing != null && !existing.isExpired()) {
                FMessage.printLockedTimer(player, "trident", existing.getRemaining());
                event.setCancelled(true);
                return;
            }

            factionPlayer.addTimer(new FTimer(ETimerType.TRIDENT, plugin.getConfiguration().getTridentDuration()));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onTridentHitPlayer(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getDamager() instanceof final Trident trident)) {
            return;
        }

        if (!(trident.getShooter() instanceof final Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof final LivingEntity attacked)) {
            return;
        }

        final double pre = event.getDamage();
        final IClass playerClass = plugin.getClassManager().getCurrentClass(attacker);

        if (!(playerClass instanceof final Diver diverClass)) {
            return;
        }

        final Location locA = attacker.getLocation().clone();
        final Location locB = attacked.getLocation().clone();
        final double dist = locA.distance(locB);

        if (dist < diverClass.getMinimumRange()) {
            return;
        }

        event.setDamage(pre*diverClass.getDamageMultiplier());

        if (attacked instanceof final Player playerDamaged) {
            playerDamaged.sendMessage(ChatColor.RED + "You have been pierced by a " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "TRIDENT!");
        }

        final double healthPre = attacked.getHealth();
        new Scheduler(plugin).sync(() -> {
            final double healthPost = attacked.getHealth();
            final double diff = (healthPre - healthPost) / 2;

            final String name = attacked.hasPotionEffect(PotionEffectType.INVISIBILITY) ? ChatColor.GRAY + "? ? ?" :
                    (attacked instanceof Player) ? ChatColor.GOLD + attacked.getName() :
                            ChatColor.GOLD + WordUtils.capitalize(attacked.getType().name().toLowerCase().replace("_", " "));

            attacker.sendMessage(ChatColor.YELLOW + "Your trident has" + ChatColor.RED + " pierced " + name +
                    ChatColor.YELLOW + " from a distance of " + ChatColor.BLUE + String.format("%.2f", dist) + " blocks " +
                    ChatColor.YELLOW + "(" + ChatColor.RED + String.format("%.2f", diff) + " ❤" + ChatColor.YELLOW + ")");

        }).delay(1L).run();
    }

    @EventHandler
    public void onPlayerRiptide(PlayerRiptideEvent event) {
        final Player player = event.getPlayer();
        final Location prevLoc = player.getLocation();
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);
        final FactionPlayer factionPlayer = (FactionPlayer)plugin.getPlayerManager().getPlayer(player);
        final FTimer existingTimer = factionPlayer.getTimer(ETimerType.TRIDENT);

        if (!(playerClass instanceof Diver)) {
            new Scheduler(plugin).sync(() -> {
                player.teleport(prevLoc);
                player.sendMessage(ChatColor.RED + "Tridents can only be used by the Diver class");
            }).delay(1L).run();

            return;
        }

        if (existingTimer != null) {
            new Scheduler(plugin).sync(() -> {
                player.teleport(prevLoc);
                FMessage.printLockedTimer(player, "trident", existingTimer.getRemaining());
            }).delay(1L).run();

            return;
        }

        factionPlayer.addTimer(new FTimer(ETimerType.TRIDENT, plugin.getConfiguration().getTridentDuration()));
    }

    /**
     * Clear expired consumable effects from scoreboard
     * @param event ClassConsumableReadyEvent
     */
    @EventHandler
    public void onConsumableReady(ClassConsumableReadyEvent event) {
        final Player player = event.getPlayer();
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (playerClass == null) {
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        // hide scoreboard entry
        if (factionPlayer != null && factionPlayer.getScoreboard() != null) {
            final ERemappedEffect remapped = ERemappedEffect.getRemappedEffect(event.getConsumable().getEffectType());

            if (remapped != null) {
                final EEffectScoreboardMapping mapping = EEffectScoreboardMapping.getByRemappedEffect(remapped);

                if (mapping != null) {
                    factionPlayer.getScoreboard().removeLine(mapping.getScoreboardPosition());
                }
            }

            if (playerClass.getConsumables().stream().noneMatch(c -> c.hasCooldown(player))) {
                factionPlayer.getScoreboard().removeLine(29);
                factionPlayer.getScoreboard().removeLine(52);
            }
        }
    }

    /**
     * Clear scoreboard entries for player class cooldowns
     * @param event ClassDeactivateEvent
     */
    @EventHandler
    public void onPlayerClassDeactivate(ClassDeactivateEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null || factionPlayer.getScoreboard() == null) {
            return;
        }

        factionPlayer.getScoreboard().removeLine(29);
        factionPlayer.getScoreboard().removeLine(52);

        for (EEffectScoreboardMapping mapping : EEffectScoreboardMapping.values()) {
            factionPlayer.getScoreboard().removeLine(mapping.getScoreboardPosition());
        }
    }

    /**
     * Reapplies class effects after a player uses a totem
     * @param event EntityResurrectEvent
     */
    @EventHandler
    public void onPlayerResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (playerClass == null) {
            return;
        }

        new Scheduler(plugin).sync(() -> playerClass.getPassiveEffects().forEach((passive, amplifier) -> player.addPotionEffect(new PotionEffect(passive, PotionEffect.INFINITE_DURATION, amplifier)))).run();
    }
}

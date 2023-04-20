package gg.hcfactions.factions.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.*;
import gg.hcfactions.factions.models.classes.EPlayerHand;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import gg.hcfactions.factions.models.classes.impl.Archer;
import gg.hcfactions.factions.models.classes.impl.Rogue;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.utils.Players;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
                    ChatColor.YELLOW + "(" + ChatColor.RED + String.format("%.2f", diff) + " ❤" + ChatColor.YELLOW + ")");

        }).delay(1L).run();
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

                        final double health = Math.max((attacked.getHealth() - rogue.getBackstabDamage()), 0.0);

                        attacked.damage(0.0); // Create the illusion of the player taking flinching damage
                        attacked.setHealth(health);
                    }).delay(((long) i * rogue.getBackstabTickrate())).run();
                }

                attacker.getInventory().setItemInHand(new ItemStack(Material.AIR));
                Players.playSound(attacker, Sound.ENTITY_ITEM_BREAK); // TODO: Test this sound and match it to original item break
                // Players.spawnEffect(attacker, attacked.getLocation(), Effect.HEART, 15, 1); TODO: Change this to new particles

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
}

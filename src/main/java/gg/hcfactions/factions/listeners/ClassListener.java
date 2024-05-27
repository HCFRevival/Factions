package gg.hcfactions.factions.listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.*;
import gg.hcfactions.factions.models.classes.*;
import gg.hcfactions.factions.models.classes.impl.*;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.events.impl.*;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.utils.Players;
import gg.hcfactions.libs.bukkit.utils.Worlds;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.*;

public final class ClassListener implements Listener {
    @Getter public final Factions plugin;
    @Getter public final Set<UUID> recentlyLoggedIn;

    public ClassListener(Factions plugin) {
        this.plugin = plugin;
        this.recentlyLoggedIn = Sets.newConcurrentHashSet();
    }

    private void handleUncloak(Player player, String reason) {
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof Rogue rogue)) {
            return;
        }

        // Failsafe to prevent hidden players
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (!onlinePlayer.canSee(player)) {
                onlinePlayer.showPlayer(plugin, player);
            }
        });

        if (!rogue.getInvisibilityStates().containsKey(player.getUniqueId()) || rogue.getInvisibilityStates().get(player.getUniqueId()).equals(Rogue.InvisibilityState.NONE)) {
            return;
        }

        rogue.unvanishPlayer(player, reason);
        rogue.getInvisibilityStates().remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        recentlyLoggedIn.add(player.getUniqueId());

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) {
                continue;
            }

            final ItemMeta meta = armor.getItemMeta();

            if (meta == null) {
                continue;
            }

            if (!meta.getPersistentDataContainer().has(plugin.getNamespacedKey(), PersistentDataType.STRING)) {
                continue;
            }

            final String value = meta.getPersistentDataContainer().get(plugin.getNamespacedKey(), PersistentDataType.STRING);

            if (value == null || !value.equalsIgnoreCase("removeOnLogin")) {
                continue;
            }

            armor.setType(Material.AIR);
        }

        new Scheduler(plugin).sync(() -> {
            final IClass playerClass = plugin.getClassManager().getClassByArmor(player);

            if (playerClass != null) {
                final ClassReadyEvent readyEvent = new ClassReadyEvent(player, playerClass);
                readyEvent.setMessagePrinted(false);
                Bukkit.getPluginManager().callEvent(readyEvent);
            }

            recentlyLoggedIn.remove(player.getUniqueId());

            player.getActivePotionEffects()
                    .stream()
                    .filter(e -> e.getDuration() >= 10000 || e.getDuration() == -1)
                    .forEach(infE -> player.removePotionEffect(infE.getType()));
        }).delay(3L).run();
    }

    @EventHandler /* Removes player from class upon disconnect */
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        player.getActivePotionEffects()
                .stream()
                .filter(e -> e.getDuration() >= 10000 || e.getDuration() == -1)
                .forEach(infE -> player.removePotionEffect(infE.getType()));

        if (playerClass == null) {
            return;
        }

        final ClassDeactivateEvent deactivateEvent = new ClassDeactivateEvent(player, playerClass);
        Bukkit.getPluginManager().callEvent(deactivateEvent);

        playerClass.deactivate(player);
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

        plugin.getClassManager().validateClass(player);
    }

    @EventHandler (priority = EventPriority.MONITOR) /* Play an audio cue when a consumable is consumed */
    public void onClassConsumeItem(ConsumeClassItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Worlds.playSound(event.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEffectExpire(EntityPotionEffectEvent event) {
        final PotionEffect effect = event.getOldEffect();

        if (effect == null) {
            return;
        }

        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        final EntityPotionEffectEvent.Action action = event.getAction();

        if (!action.equals(EntityPotionEffectEvent.Action.REMOVED) && !action.equals(EntityPotionEffectEvent.Action.CLEARED)) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (playerClass == null) {
            return;
        }

        if (!playerClass.getPassiveEffects().containsKey(effect.getType())) {
            return;
        }

        final int amplifier = playerClass.getPassiveEffects().get(effect.getType());

        new Scheduler(plugin).sync(() -> {
            final IClass futurePlayerClass = plugin.getClassManager().getCurrentClass(player);

            if (playerClass == futurePlayerClass) {
                player.addPotionEffect(new PotionEffect(effect.getType(), PotionEffect.INFINITE_DURATION, amplifier));
            }
        }).delay(1L).run();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onArcherTagDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof final Player damaged)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        final Archer archerClass = (Archer)plugin.getClassManager().getClassByName("Archer");

        if (archerClass == null) {
            return;
        }

        if (archerClass.getMarkedEntities().contains(damaged.getUniqueId())) {
            event.setDamage(event.getDamage() + event.getDamage() * archerClass.getMarkPercentage());
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onArcherTag(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getDamager() instanceof final SpectralArrow arrow)) {
            return;
        }

        if (!(arrow.getShooter() instanceof final Player shooter)) {
            return;
        }

        if (!(event.getEntity() instanceof final LivingEntity damaged)) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(shooter);

        if (!(playerClass instanceof final Archer archerClass)) {
            return;
        }

        if (archerClass.getMarkedEntities().contains(damaged.getUniqueId())) {
            return;
        }

        final String name = damaged.hasPotionEffect(PotionEffectType.INVISIBILITY) ? ChatColor.GRAY + "? ? ?" :
                (damaged instanceof Player) ? ChatColor.GOLD + damaged.getName() :
                        ChatColor.GOLD + WordUtils.capitalize(damaged.getType().name().toLowerCase().replace("_", " "));

        final int percent = (int)Math.round(archerClass.getMarkPercentage() * 100);

        final ArcherMarkEvent markEvent = new ArcherMarkEvent(shooter, damaged, archerClass.getMarkDuration() * 20);
        Bukkit.getPluginManager().callEvent(markEvent);
        if (markEvent.isCancelled()) {
            return;
        }

        arrow.setGlowingTicks(markEvent.getTicks());
        archerClass.mark(damaged);

        shooter.sendMessage(ChatColor.YELLOW + "Your arrow has" + ChatColor.BLUE + " marked " + name + ChatColor.YELLOW + " for " + ChatColor.BLUE + archerClass.getMarkDuration() + " seconds");

        if (damaged instanceof final Player damagedPlayer) {
            final FactionPlayer factionPlayer = (FactionPlayer)plugin.getPlayerManager().getPlayer(damagedPlayer);

            if (factionPlayer != null) {
                factionPlayer.addTimer(new FTimer(ETimerType.ARCHER_MARK, archerClass.getMarkDuration()));
            }

            damaged.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "MARKED! " + ChatColor.YELLOW + "You will take " + ChatColor.RED + percent + "% Increased Damage" + ChatColor.YELLOW + " for " + ChatColor.BLUE + archerClass.getMarkDuration() + " seconds");
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onDistanceArcher(EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        final Entity damaged = event.getEntity();
        double damage = event.getFinalDamage();

        if (event.isCancelled()) {
            return;
        }

        if (!(damager instanceof final Arrow arrow)) {
            return;
        }

        // Use archer tag instead
        if (arrow instanceof SpectralArrow) {
            return;
        }

        if (!(damaged instanceof LivingEntity)) {
            return;
        }

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
        final int hitCount = archerClass.getHitCount(player, (LivingEntity) damaged);
        final double distanceDamage = (damagePerBlock * distance);
        final double consecutiveDamage = (archerClass.getConsecutiveBase() * ((hitCount + 1) * archerClass.getConsecutiveMultiplier()));
        final double finalDamage = Math.min((distanceDamage + consecutiveDamage + damage), maxDamage);

        final ArcherTagEvent tagEvent = new ArcherTagEvent(player, (LivingEntity) damaged, finalDamage, distance, hitCount);
        Bukkit.getPluginManager().callEvent(tagEvent);
        if (tagEvent.isCancelled()) {
            return;
        }

        archerClass.addHit(player, (LivingEntity) damaged, 10); // TODO: Make configurable
        event.setDamage(tagEvent.getDamage());

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
                    ChatColor.YELLOW + "(" + ChatColor.RED + String.format("%.2f", diff) + " ❤" + ((hitCount + 1) > 1 ? ChatColor.GOLD + " x" + (hitCount + 1) : "")
                    + ChatColor.YELLOW + ")");

        }).delay(1L).run();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onRogueGrappleEntity(PlayerFishEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (event.getHand() == null) {
            return;
        }

        final ItemStack hand = player.getInventory().getItem(event.getHand());
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof Rogue rogueClass)) {
            return;
        }

        if (factionPlayer.hasTimer(ETimerType.GRAPPLE)) {
            FMessage.printLockedTimer(player, "Grapple", factionPlayer.getTimer(ETimerType.GRAPPLE).getRemaining());
            event.setCancelled(true);
            event.getHook().setHookedEntity(null);
            event.getHook().remove();
            return;
        }

        if (event.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY) && event.getCaught() instanceof final LivingEntity attachedEntity) {
            final PlayerGrappleEvent grappleEvent = new PlayerGrappleEvent(player, hand, player.getLocation(), attachedEntity.getLocation(), attachedEntity);
            Bukkit.getPluginManager().callEvent(grappleEvent);

            if (grappleEvent.isCancelled()) {
                return;
            }

            factionPlayer.addTimer(new FTimer(ETimerType.GRAPPLE, rogueClass.getGrappleCooldown()));

            final Vector velocity = attachedEntity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            player.setVelocity(velocity.multiply(rogueClass.getGrappleHorizontalSpeed()).setY(rogueClass.getGrappleVerticalSpeed()));
            event.setCancelled(true);
            event.getHook().setHookedEntity(null);
            event.getHook().remove();
            return;
        }

        if (!event.getState().equals(PlayerFishEvent.State.IN_GROUND)) {
            return;
        }

        final PlayerGrappleEvent grappleEvent = new PlayerGrappleEvent(player, hand, player.getLocation(), event.getHook().getLocation(), null);
        Bukkit.getPluginManager().callEvent(grappleEvent);

        if (grappleEvent.isCancelled()) {
            return;
        }

        factionPlayer.addTimer(new FTimer(ETimerType.GRAPPLE, rogueClass.getGrappleCooldown()));

        final Vector velocity = event.getHook().getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
        player.setVelocity(velocity.multiply(rogueClass.getGrappleHorizontalSpeed()).setY(rogueClass.getGrappleVerticalSpeed()));
        event.setCancelled(true);
        event.getHook().remove();
    }

    @EventHandler
    public void onRogueCloak(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final ItemStack hand = event.getItem();
        final EPlayerHand handType = (event.getHand() == null || event.getHand().equals(EquipmentSlot.HAND) ? EPlayerHand.MAIN : EPlayerHand.OFFHAND);

        if (hand == null) {
            return;
        }

        if (!hand.getType().equals(Material.ENDER_EYE)) {
            return;
        }

        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof final Rogue rogue)) {
            return;
        }

        event.setCancelled(true);

        if (rogue.isInvisible(player)) {
            player.sendMessage(ChatColor.RED + "You are already cloaked");
            return;
        }

        if (rogue.hasInvisCooldown(player)) {
            final long timeUntilNextCall = (rogue.getInvisCooldowns().getOrDefault(player.getUniqueId(), 0L) - Time.now());
            player.sendMessage(ChatColor.RED + "Cloak is locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(timeUntilNextCall) + ChatColor.RED + "s");
            return;
        }

        final Rogue.InvisibilityState entryState = rogue.getExpectedInvisibilityState(player);

        if (!entryState.equals(Rogue.InvisibilityState.FULL)) {
            player.sendMessage(ChatColor.RED + "You can not cloak while enemies are nearby");
            return;
        }

        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else if (handType.equals(EPlayerHand.MAIN)) {
            player.getInventory().setItemInMainHand(null);
        } else {
            player.getInventory().setItemInOffHand(null);
        }

        final Rogue.InvisibilityState currentInvisState = rogue.getInvisibilityStates().getOrDefault(player.getUniqueId(), Rogue.InvisibilityState.NONE);

        if (!currentInvisState.equals(Rogue.InvisibilityState.NONE)) {
            player.sendMessage(ChatColor.RED + "Your cloak is already active");
            return;
        }

        rogue.getInvisCooldowns().put(player.getUniqueId(), (Time.now() + (rogue.getInvisibilityCooldown()*1000L)));
        rogue.getInvisibilityStates().put(player.getUniqueId(), Rogue.InvisibilityState.FULL);
        rogue.vanishPlayer(player);
        rogue.updateVisibility(player, rogue.getExpectedInvisibilityState(player));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0));
        new Scheduler(plugin).sync(() -> rogue.getInvisCooldowns().remove(uniqueId)).delay(rogue.getInvisibilityCooldown()*20L).run();
    }

    @EventHandler
    public void onSeaCall(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
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
            Component displayName = rankService.getFormattedNameComponent(player);
            NamedTextColor factionNameColor = null;
            String factionName = null;

            if (faction != null && faction.isMember(worldPlayer)) {
                factionNameColor = NamedTextColor.DARK_GREEN;
                factionName = faction.getName();
            } else if (faction != null && faction.isAlly(worldPlayer)) {
                factionNameColor = NamedTextColor.BLUE;
                factionName = faction.getName();
            } else {
                factionNameColor = NamedTextColor.RED;
            }

            Players.playSound(worldPlayer, Sound.ITEM_GOAT_HORN_SOUND_3);

            Component message = Component.empty().appendNewline().append(Component.text("The sea calls for", NamedTextColor.DARK_AQUA)).appendSpace();
            if (factionName != null) {
                message = message.append(Component.text("[" + factionName + "]", factionNameColor));
            }
            message = message.append(displayName).appendNewline();

            worldPlayer.sendMessage(message);
        });

        diver.getSeaCallCooldowns().put(player.getUniqueId(), (Time.now() + (diver.getSeaCallCooldown()*1000L)));
        new Scheduler(plugin).sync(() -> diver.getSeaCallCooldowns().remove(uniqueId)).delay(diver.getSeaCallCooldown()*20L).run();
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

        if (!attacker.getInventory().getItemInMainHand().getType().equals(Material.GOLDEN_SWORD)) {
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

                attacked.sendMessage(Component.text("You have been", NamedTextColor.RED).appendSpace().append(Component.text("BACKSTABBED!", NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, TextDecoration.State.TRUE)));

                double toSubtract = rogue.getBackstabDamage(); // TODO: Make configurable
                double totalAbsorption = attacked.getAbsorptionAmount();

                if (totalAbsorption > 0) {
                    if (totalAbsorption >= toSubtract) {
                        attacked.setAbsorptionAmount((totalAbsorption - toSubtract));
                    } else {
                        attacked.setAbsorptionAmount(0);
                        toSubtract -= totalAbsorption;
                    }
                }

                double newHealth = Math.max(attacked.getHealth() - toSubtract, 0.0);
                attacked.setHealth(newHealth);
                event.setDamage(0.0);

                // Bleeding damage code
                /* for (int i = 1; i <= 3; i++) {
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

                        final double health = Math.max((attacked.getHealth() - rogue.getBackstabDamage()), 0.5);
                        attacked.damage(0.0); // Create the illusion of the player taking flinching damage
                        attacked.setHealth(health);
                    }).delay(((long) i * rogue.getBackstabTickrate())).run();
                } */

                // Remove sword
                attacker.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                // Play effect
                attackedLocation.getWorld().spawnParticle(Particle.RAID_OMEN, attackedLocation.getX(), attackedLocation.getY() + 1.5, attackedLocation.getZ(), 8, 0.5, 0.5, 0.5, 1);
                Worlds.playSound(attackedLocation, Sound.ENTITY_ITEM_BREAK);

                // Print backstab to attacker
                Component attackedComponent;

                if (!attacked.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    attackedComponent = Component.text(attacked.getName(), NamedTextColor.GOLD);
                } else {
                    attackedComponent = Component.text("???", NamedTextColor.GRAY);
                }

                attacker.sendMessage(Component.text("You have backstabbed", NamedTextColor.YELLOW).appendSpace().append(attackedComponent));

                // Apply cooldown
                final long nextBackstab = Time.now() + (rogue.getBackstabCooldown() * 1000L);
                rogue.getBackstabCooldowns().put(attackerUUID, nextBackstab);

                new Scheduler(plugin).sync(() -> {
                    rogue.getBackstabCooldowns().remove(attackerUUID);

                    if (Bukkit.getPlayer(attackerUUID) != null) {
                        Objects.requireNonNull(Bukkit.getPlayer(attackerUUID)).sendMessage(Component.text(rogue.getName() + " backstab is ready", NamedTextColor.GREEN));
                        Players.playSound(Objects.requireNonNull(Bukkit.getPlayer(attackerUUID)), Sound.BLOCK_NOTE_BLOCK_HARP);
                    }
                }).delay(rogue.getBackstabCooldown() * 20L).run();
            }).run();
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onTippedArrowLaunch(EntityShootBowEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);
        final ItemStack item = event.getConsumable();

        if (item != null && item.getType().equals(Material.TIPPED_ARROW) && !(playerClass instanceof Archer)) {
            player.sendMessage(ChatColor.RED + "Tipped Arrows can only be used by the Archer class");
            event.setConsumeItem(false);
            event.setCancelled(true);
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

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof Diver)) {
            player.sendMessage(ChatColor.RED + "Tridents can only be used by the Diver class");
            event.setCancelled(true);
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

        final double damage = pre*diverClass.getDamageMultiplier();

        final DiverPierceEvent pierceEvent = new DiverPierceEvent(attacker, attacked, damage, dist);
        Bukkit.getPluginManager().callEvent(pierceEvent);
        if (pierceEvent.isCancelled()) {
            return;
        }

        event.setDamage(pierceEvent.getDamage());

        if (attacked instanceof final Player playerDamaged) {
            // reset trident CD on non-riptide trident hit
            final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(attacker);

            if (factionPlayer.hasTimer(ETimerType.TRIDENT)) {
                factionPlayer.finishTimer(ETimerType.TRIDENT);
            }

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
     * Scale tank class when it activates
     * @param event ClassActivateEvent
     */
    @EventHandler
    public void onTankResize(ClassActivateEvent event) {
        final Player player = event.getPlayer();
        final IClass playerClass = event.getPlayerClass();

        if (playerClass instanceof Tank) {
            final CXService cxs = (CXService) plugin.getService(CXService.class);

            if (cxs != null) {
                cxs.getAttributeManager().scale(player, 1.25, 20, false);
            }
        }
    }

    /**
     * Scale tank class when it deactivates
     * @param event ClassDeactivateEvent
     */
    @EventHandler
    public void onTankResize(ClassDeactivateEvent event) {
        final Player player = event.getPlayer();
        final IClass playerClass = event.getPlayerClass();

        if (!player.isOnline()) {
            return;
        }

        if (playerClass instanceof Tank) {
            final CXService cxs = (CXService) plugin.getService(CXService.class);

            if (cxs != null) {
                cxs.getAttributeManager().scale(player, 1.0, 20, false);
            }
        }
    }

    /**
     * Class cleanup
     * @param event ClassDeactivateEvent
     */
    @EventHandler
    public void onPlayerClassDeactivate(ClassDeactivateEvent event) {
        final Player player = event.getPlayer();

        if (event.getPlayerClass() instanceof final Rogue rogue) {
            if (rogue.isInvisible(player)) {
                rogue.unvanishPlayer(player, "your class deactivated");
            }

            rogue.getInvisibilityStates().remove(player.getUniqueId());
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

    @EventHandler
    public void onPlayerHeldItemChange(PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (item == null) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof final IHoldableClass holdableClass)) {
            return;
        }

        final Optional<IClassHoldable> holdableQuery = holdableClass.getHoldable(item.getType());

        if (holdableQuery.isEmpty()) {
            return;
        }

        final IClassHoldable holdable = holdableQuery.get();

        if (!holdableClass.shouldReapplyHoldable(player.getUniqueId(), holdable)) {
            return;
        }

        final double range = (playerClass instanceof final Bard bard) ? bard.getBardRange() : 16.0;
        holdable.apply(player, holdableClass.getHoldableUpdateRate(), range, true);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerShield(PlayerShieldEvent event) {
        if (event.isRaised() && event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof final Tank tankClass)) {
            if (event.isRaised()) {
                player.sendMessage(ChatColor.RED + "Shields can only be used by the Tank class");
                event.setCancelled(true);
            }

            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            player.sendMessage(ChatColor.RED + "Failed to load your player profile: CLNP");
            event.setCancelled(true);
            return;
        }

        if (event.isRaised()) {
            factionPlayer.addTimer(new FTimer(ETimerType.GUARD, tankClass.getShieldWarmup()));
            return;
        }

        final TankShieldUnreadyEvent unreadyEvent = new TankShieldUnreadyEvent(player, tankClass);
        Bukkit.getPluginManager().callEvent(unreadyEvent);

        if (factionPlayer.hasTimer(ETimerType.GUARD)) {
            factionPlayer.removeTimer(ETimerType.GUARD);
        }
    }

    @EventHandler
    public void onTankUseShield(PlayerShieldEvent event) {
        final Player player = event.getPlayer();
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof final Tank tankClass)) {
            return;
        }

        if (!tankClass.canUseStamina(player)) {
            player.sendMessage(ChatColor.RED + "You're out of stamina. Please wait a moment and try again.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTankMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof final Tank tankClass)) {
            return;
        }

        if (!tankClass.isGuarding(player)) {
            return;
        }

        tankClass.setGuardPoint(player);
    }

    @EventHandler
    public void onTankShieldUnready(TankShieldUnreadyEvent event) {
        final Player player = event.getPlayer();
        final Tank tankClass = event.getTankClass();

        tankClass.deactivateShield(player);
    }

    @EventHandler
    public void onTankGuardApply(AreaEffectCloudApplyEvent event) {
        final AreaEffectCloud cloud = event.getEntity();
        final List<LivingEntity> affectedEntities = Lists.newArrayList(event.getAffectedEntities());

        if (cloud.getBasePotionType() == null || !cloud.getBasePotionType().equals(PotionType.TURTLE_MASTER)) {
            return;
        }

        if (!(cloud.getSource() instanceof final Player player)) {
            return;
        }

        if (!(plugin.getClassManager().getCurrentClass(player) instanceof final Tank tankClass)) {
            return;
        }

        final PlayerFaction pf = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        event.setCancelled(true);
        event.getAffectedEntities().clear();

        for (LivingEntity entity : affectedEntities) {
            if (!(entity instanceof final Player otherPlayer)) {
                continue;
            }

            // prevent applying to anyone but self if not in fac
            if (pf == null && !entity.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            // prevent applying to enemies when in fac
            if (pf != null && !pf.isMember(otherPlayer)) {
                continue;
            }

            for (PotionEffectType effectType : tankClass.getGuardEffects().keySet()) {
                final int amplifier = tankClass.getGuardEffects().get(effectType);

                if (effectType.equals(PotionEffectType.ABSORPTION) && entity.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                    continue;
                }

                if (effectType.equals(PotionEffectType.HEALTH_BOOST) && entity.hasPotionEffect(PotionEffectType.HEALTH_BOOST)) {
                    continue;
                }

                final TankGuardApplyEvent applyEvent = new TankGuardApplyEvent(player, entity, effectType);
                Bukkit.getPluginManager().callEvent(applyEvent);

                if (applyEvent.isCancelled()) {
                    continue;
                }

                entity.addPotionEffect(new PotionEffect(effectType, 40, amplifier));
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onTankShieldDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        if (event instanceof final EntityDamageByEntityEvent entityDamageEntEvent) {
            final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

            if (faction != null && entityDamageEntEvent.getDamager() instanceof final Player playerDamager && faction.isMember(playerDamager)) {
                return;
            }
        }

        if (!player.isHandRaised()) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof final Tank tankClass)) {
            return;
        }

        if (!tankClass.isGuarding(player)) {
            return;
        }

        if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                || event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
                || event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)
                || event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {

            final double damage = event.getDamage();

            tankClass.damageStamina(player, damage * tankClass.getStaminaDamageDivider());
            event.setCancelled(true);
            player.damage(damage * tankClass.getShieldDamageReduction());
        }
    }

    @EventHandler
    public void onTankStaminaChange(TankStaminaChangeEvent event) {
        final Player player = event.getPlayer();
        final Tank tankClass = (Tank) plugin.getClassManager().getClassByName("Guardian"); // TODO: Probably make this safer
        final ItemStack oldBanner = tankClass.getBanner(event.getFrom());
        final ItemStack newBanner = tankClass.getBanner(event.getTo());

        if (!oldBanner.getType().equals(newBanner.getType())) {
            Objects.requireNonNull(player.getEquipment()).setHelmet(newBanner);
            Worlds.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT);
            player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 1.5, 0), 8, 0.5, 2.0, 0.5);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();
        final ItemStack pre = player.getInventory().getItemInOffHand();

        new Scheduler(plugin).sync(() -> {
            final ItemStack post = player.getInventory().getItemInOffHand();

            if (!pre.getType().equals(post.getType())) {
                plugin.getClassManager().validateClass(player);
            }
        }).delay(1L).run();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerHeldSwap(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        new Scheduler(plugin).sync(() -> plugin.getClassManager().validateClass(player)).delay(1L).run();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final List<ItemStack> drops = event.getDrops();
        final List<ItemStack> toRemove = Lists.newArrayList();

        drops.forEach(drop -> {
            if (drop.getType().name().endsWith("_BANNER")) {
                final ItemMeta meta = drop.getItemMeta();

                if (meta != null && meta.getPersistentDataContainer().has(plugin.getNamespacedKey(), PersistentDataType.STRING)) {
                    final String value = meta.getPersistentDataContainer().get(plugin.getNamespacedKey(), PersistentDataType.STRING);

                    if (value != null && value.equalsIgnoreCase("removeOnLogin")) {
                        toRemove.add(drop);
                    }
                }
            }
        });

        toRemove.forEach(drops::remove);
    }

    @EventHandler
    public void onRogueBlockBreak(BlockBreakEvent event) {
        handleUncloak(event.getPlayer(), "broke a block");
    }

    @EventHandler
    public void onRogueBlockPlace(BlockPlaceEvent event) {
        handleUncloak(event.getPlayer(), "placed a block");
    }

    @EventHandler
    public void onRogueBlockInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        if (!FactionUtil.isInteractable(event.getClickedBlock().getType())) {
            return;
        }

        handleUncloak(event.getPlayer(), "interacted with a block");
    }

    @EventHandler
    public void onRogueLaunchProjectile(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof final Player player)) {
            return;
        }

        if (event.getEntity() instanceof FishHook) {
            return;
        }

        handleUncloak(player, "launched a projectile");
    }

    @EventHandler
    public void onRogueGrapple(PlayerGrappleEvent event) {
        handleUncloak(event.getPlayer(), "used your grapple");
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof final Player player) {
            handleUncloak(player, "took damage");
            return;
        }

        if (event.getDamager() instanceof final Player player) {
            handleUncloak(player, "inflicted damage");
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        handleUncloak(player, "changed worlds");
    }

    @EventHandler
    public void onPlayerSplashPlayer(PlayerSplashPlayerEvent event) {
        final Player damaged = event.getDamaged();
        final Player damager = event.getDamager();

        handleUncloak(damaged, "were impacted by a potion");
        handleUncloak(damager, "impacted a player with your potion");
    }

    @EventHandler
    public void onLingeringSplash(PlayerLingeringSplashEvent event) {
        final Player damaged = event.getDamaged();
        final Player damager = event.getDamager();

        handleUncloak(damaged, "were impacted by a potion");
        handleUncloak(damager, "impacted a player with your potion");
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof final Player player)) {
            return;
        }

        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);
        if (!(playerClass instanceof Rogue rogue)) {
            return;
        }

        if (rogue.isInvisible(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRogueDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        handleUncloak(player, "died");
    }

    @EventHandler
    public void onRogueQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof final Rogue rogue)) {
            return;
        }

        if (rogue.isInvisible(player)) {
            rogue.unvanishPlayer(player);
        }
    }

    @EventHandler
    public void onJoinRogueVanish(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);
        final Rogue rogue = (Rogue)plugin.getClassManager().getClassByName("Rogue");

        if (rogue == null) {
            return;
        }

        rogue.getInvisiblePlayers().forEach((id, state) -> {
            final Player roguePlayer = Bukkit.getPlayer(id);

            if (roguePlayer == null) {
                return;
            }

            if (!state.equals(Rogue.InvisibilityState.NONE) && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                if (faction != null && faction.isMember(player)) {
                    return;
                }

                player.hidePlayer(plugin, roguePlayer);
            }
        });
    }
}

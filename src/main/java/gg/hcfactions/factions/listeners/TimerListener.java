package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.ClassReadyEvent;
import gg.hcfactions.factions.listeners.events.player.ClassUnreadyEvent;
import gg.hcfactions.factions.listeners.events.player.ConsumeClassItemEvent;
import gg.hcfactions.factions.listeners.events.player.PlayerDamageCombatLoggerEvent;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.events.impl.PlayerBigMoveEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerLingeringSplashEvent;
import gg.hcfactions.libs.bukkit.events.impl.PlayerSplashPlayerEvent;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public record TimerListener(@Getter Factions plugin) implements Listener {
    /**
     * Handles applying combat tag to a PvP event
     *
     * @param damager Damaging Player
     * @param damaged Damaged Player
     */
    private void handleAttack(Player damager, Player damaged) {
        if (damager.equals(damaged)) {
            return;
        }

        final FactionPlayer damagedProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(damaged.getUniqueId());
        final FactionPlayer damagerProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(damager.getUniqueId());

        if (damagedProfile == null || damagerProfile == null) {
            return;
        }

        final int attackerDuration = plugin.getConfiguration().getAttackerCombatTagDuration();
        final int attackedDuration = plugin.getConfiguration().getAttackedCombatTagDuration();

        final FTimer damagedTimer = damagedProfile.getTimer(ETimerType.COMBAT);
        final FTimer damagerTimer = damagerProfile.getTimer(ETimerType.COMBAT);

        if (damagedTimer == null) {
            damagedProfile.addTimer(new FTimer(ETimerType.COMBAT, attackedDuration));
            FMessage.printCombatTag(damaged, (attackedDuration * 1000L));
        } else if (damagedTimer.getRemainingSeconds() < attackedDuration) {
            damagedTimer.setExpire(Time.now() + (attackedDuration * 1000L));
        }

        if (damagerTimer == null) {
            damagerProfile.addTimer(new FTimer(ETimerType.COMBAT, attackerDuration));
            FMessage.printCombatTag(damager, (attackerDuration * 1000L));
        } else if (damagerTimer.getRemainingSeconds() < attackerDuration) {
            damagerTimer.setExpire(Time.now() + (attackerDuration * 1000L));
        }
    }

    /**
     * Handles issuing class warmpup for players
     * @param event ClassReadyEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClassReadyEvent(ClassReadyEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your player data");
            return;
        }

        final IClass playerClass = event.getPlayerClass();

        if (playerClass == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your class");
            return;
        }

        if (factionPlayer.hasTimer(ETimerType.CLASS)) {
            factionPlayer.removeTimer(ETimerType.CLASS);
        }

        final int warmup = playerClass.getWarmup();

        FMessage.printClassPreparing(player, playerClass);
        factionPlayer.addTimer(new FTimer(ETimerType.CLASS, warmup));
    }

    /**
     * Handles unreadying class if player armor state changes
     * @param event ClassUnreadyEvent
     */
    @EventHandler
    public void onClassUnreadyEvent(ClassUnreadyEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            return;
        }

        final IClass currentClass = plugin.getClassManager().getCurrentClass(player);

        if (currentClass != null) {
            FMessage.printClassCancelled(player, currentClass);
        }

        if (factionPlayer.hasTimer(ETimerType.CLASS)) {
            factionPlayer.removeTimer(ETimerType.CLASS);
        }
    }

    /**
     * Handles Combat Tag
     *
     * @param event PlayerDamagePlayerEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCombatTagPhysical(PlayerDamagePlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        handleAttack(event.getDamager(), event.getDamaged());
    }

    /**
     * Handles Combat Tag
     *
     * @param event PlayerSplashPlayerEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCombatTagSplash(PlayerSplashPlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        boolean isDebuff = false;

        for (PotionEffect effect : event.getPotion().getEffects()) {
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

        handleAttack(event.getDamager(), event.getDamaged());
    }

    /**
     * Handles Combat Tag
     *
     * @param event PlayerSplashPlayerEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCombatTagSplash(PlayerLingeringSplashEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final AreaEffectCloud cloud = event.getCloud();

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

        handleAttack(event.getDamager(), event.getDamaged());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamageLogger(PlayerDamageCombatLoggerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer damagerProfile = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (damagerProfile == null) {
            return;
        }

        final FTimer timer = damagerProfile.getTimer(ETimerType.COMBAT);
        final int attackerDuration = plugin.getConfiguration().getAttackerCombatTagDuration();

        if (timer == null) {
            damagerProfile.addTimer(new FTimer(ETimerType.COMBAT, attackerDuration));
            FMessage.printCombatTag(player, attackerDuration);
        } else if (timer.getRemainingSeconds() < attackerDuration) {
            timer.setExpire(Time.now() + (attackerDuration * 1000L));
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onClassConsume(ConsumeClassItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            player.sendMessage(Component.text(FError.P_COULD_NOT_LOAD_P.getErrorDescription(), NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        if (!factionPlayer.hasTimer(ETimerType.COMBAT)) {
            FMessage.printCombatTag(player, (plugin.getConfiguration().getAttackerCombatTagDuration()*1000L));
        }

        factionPlayer.addTimer(new FTimer(ETimerType.COMBAT, plugin.getConfiguration().getAttackerCombatTagDuration()));
    }

    /**
     * Handles cancelling timers which require the player to not move
     *
     * @param event PlayerBigMoveEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerBigMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            return;
        }

        if (
                !factionPlayer.hasTimer(ETimerType.HOME)
                        && !factionPlayer.hasTimer(ETimerType.LOGOUT)
                        && !factionPlayer.hasTimer(ETimerType.STUCK)
        ) {
            return;
        }

        for (FTimer timer : factionPlayer.getTimers()) {
            if (
                    !timer.getType().equals(ETimerType.HOME)
                            && !timer.getType().equals(ETimerType.LOGOUT)
                            && !timer.getType().equals(ETimerType.STUCK)
            ) {
                continue;
            }

            factionPlayer.removeTimer(timer.getType());
            FMessage.printTimerCancelled(player, ChatColor.stripColor(timer.getType().getLegacyDisplayName()), "moved");
        }
    }

    /**
     * Handles cancelling timers which require the player to not take damage
     *
     * @param event EntityDamageEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            return;
        }

        if (
                !factionPlayer.hasTimer(ETimerType.HOME)
                        && !factionPlayer.hasTimer(ETimerType.LOGOUT)
                        && !factionPlayer.hasTimer(ETimerType.STUCK)
        ) {
            return;
        }

        for (FTimer timer : factionPlayer.getTimers()) {
            if (!timer.getType().equals(ETimerType.HOME) && !timer.getType().equals(ETimerType.LOGOUT) && !timer.getType().equals(ETimerType.STUCK)) {
                continue;
            }

            factionPlayer.removeTimer(timer.getType());
            FMessage.printTimerCancelled(player, ChatColor.stripColor(timer.getType().getLegacyDisplayName()), "took damage");
        }
    }

    /**
     * Handles applying the enderpearl cooldown
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderpearl(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        final ItemStack hand = event.getItem();

        if (hand == null || !hand.getType().equals(Material.ENDER_PEARL)) {
            return;
        }

        final FactionPlayer account = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (account == null) {
            return;
        }

        final FTimer existing = account.getTimer(ETimerType.ENDERPEARL);

        if (existing != null && !existing.isExpired()) {
            event.setCancelled(true);
            FMessage.printLockedTimer(player, "enderpearls", existing.getRemaining());
        }
    }

    /**
     * Handles preventing enderpearls from being thrown while on cooldown
     *
     * @param event ProjectileLaunchEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderpearlLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Projectile projectile = event.getEntity();

        if (!(projectile instanceof EnderPearl)) {
            return;
        }

        if (!(projectile.getShooter() instanceof final Player player)) {
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            return;
        }

        final FTimer existing = factionPlayer.getTimer(ETimerType.ENDERPEARL);

        if (existing != null && !existing.isExpired()) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        factionPlayer.addTimer(new FTimer(ETimerType.ENDERPEARL, plugin.getConfiguration().getEnderpearlDuration()));
    }

    /**
     * Handles applying the wind charge cooldown
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWindCharge(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        final ItemStack hand = event.getItem();

        if (hand == null || !hand.getType().equals(Material.WIND_CHARGE)) {
            return;
        }

        final FactionPlayer account = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (account == null) {
            return;
        }

        final FTimer existing = account.getTimer(ETimerType.WIND_CHARGE);

        if (existing != null && !existing.isExpired()) {
            event.setCancelled(true);
            FMessage.printLockedTimer(player, "wind charges", existing.getRemaining());
        }
    }

    /**
     * Handles preventing wind charges from being thrown while on cooldown
     *
     * @param event ProjectileLaunchEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWindChargeLaunch(ProjectileLaunchEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Projectile projectile = event.getEntity();

        if (!(projectile instanceof WindCharge)) {
            return;
        }

        if (!(projectile.getShooter() instanceof final Player player)) {
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            return;
        }

        final FTimer existing = factionPlayer.getTimer(ETimerType.WIND_CHARGE);

        if (existing != null && !existing.isExpired()) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        factionPlayer.addTimer(new FTimer(ETimerType.WIND_CHARGE, plugin.getConfiguration().getWindChargeDuration()));
    }

    /**
     * Handles applying consumable item cooldowns
     *
     * @param event PlayerItemConsumeEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final ItemStack item = event.getItem();

        if (factionPlayer == null) {
            return;
        }

        if (item.getType().equals(Material.GOLDEN_APPLE)) {
            final FTimer existing = factionPlayer.getTimer(ETimerType.CRAPPLE);

            if (existing != null && !existing.isExpired()) {
                FMessage.printLockedTimer(player, "crapples", existing.getRemaining());
                event.setCancelled(true);
                return;
            }

            factionPlayer.addTimer(new FTimer(ETimerType.CRAPPLE, plugin.getConfiguration().getCrappleDuration()));
        } else if (item.getType().equals(Material.ENCHANTED_GOLDEN_APPLE)) {
            final FTimer existing = factionPlayer.getTimer(ETimerType.GAPPLE);

            if (existing != null && !existing.isExpired()) {
                FMessage.printLockedTimer(player, "gapples", existing.getRemaining());
                event.setCancelled(true);
                return;
            }

            factionPlayer.addTimer(new FTimer(ETimerType.GAPPLE, plugin.getConfiguration().getGappleDuration()));
        } else if (item.getType().equals(Material.CHORUS_FRUIT)) {
            final FTimer existing = factionPlayer.getTimer(ETimerType.CHORUS_FRUIT);

            if (existing != null && !existing.isExpired()) {
                FMessage.printLockedTimer(player, "chorus fruit", existing.getRemaining());
                event.setCancelled(true);
                return;
            }

            factionPlayer.addTimer(new FTimer(ETimerType.CHORUS_FRUIT, plugin.getConfiguration().getChorusDuration()));
        }
    }

    /**
     * Handles applying totem cooldown
     *
     * @param event EntityResurrectEvent
     */
    @EventHandler
    public void onPlayerResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        if (!player.getInventory().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING)
                && !player.getInventory().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)) {
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            event.setCancelled(true);
            return;
        }

        final FTimer existing = factionPlayer.getTimer(ETimerType.TOTEM);

        if (existing != null && !existing.isExpired()) {
            FMessage.printLockedTimer(player, "totems", existing.getRemaining());
            event.setCancelled(true);
            return;
        }

        factionPlayer.addTimer(new FTimer(ETimerType.TOTEM, plugin.getConfiguration().getTotemDuration()));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (item == null || !item.getType().equals(Material.FIREWORK_ROCKET)) {
            return;
        }

        if (!player.isGliding()) {
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            return;
        }

        if (factionPlayer.hasTimer(ETimerType.COMBAT)) {
            player.sendMessage(FMessage.ERROR + "You can not boost while combat-tagged");
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onShulkerBoxOpen(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Action action = event.getAction();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (event.useInteractedBlock().equals(Event.Result.DENY)) {
            return;
        }

        if (factionPlayer == null) {
            plugin.getAresLogger().error("failed to prevent shulker box open: faction player null");
            return;
        }

        if (block == null || !block.getType().name().endsWith("_SHULKER_BOX")) {
            return;
        }

        if (factionPlayer.hasTimer(ETimerType.COMBAT)) {
            player.sendMessage(FMessage.ERROR + "You can not use Shulker Boxes while combat-tagged");
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerChangeWorld(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final Location from = event.getFrom();
        final Location to = event.getTo();
        final PlayerTeleportEvent.TeleportCause cause = event.getCause();

        if (!cause.equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            return;
        }

        if (to == null || from == null || to.getWorld() == null || from.getWorld() == null) {
            return;
        }

        if (!to.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer != null && !factionPlayer.hasTimer(ETimerType.PROTECTION) && plugin.getConfiguration().getEnterEndProtectionDuration() > 0) {
            factionPlayer.addTimer(new FTimer(ETimerType.PROTECTION, plugin.getConfiguration().getEnterEndProtectionDuration()));
        }
    }
}

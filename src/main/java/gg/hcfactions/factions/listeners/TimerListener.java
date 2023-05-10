package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.ClassReadyEvent;
import gg.hcfactions.factions.listeners.events.player.ClassUnreadyEvent;
import gg.hcfactions.factions.listeners.events.player.PlayerDamageCombatLoggerEvent;
import gg.hcfactions.factions.models.classes.IClass;
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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

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
            if (factionPlayer.isPreferScoreboardDisplay()) {
                factionPlayer.getScoreboard().removeLine(ETimerType.CLASS.getScoreboardPosition());
            }

            factionPlayer.removeTimer(ETimerType.CLASS);
        }

        final int warmup = playerClass.getWarmup();

        player.sendMessage(ChatColor.GOLD + "Preparing Class" + ChatColor.YELLOW + ": " + ChatColor.GREEN + playerClass.getName());
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
            player.sendMessage(ChatColor.GOLD + "Class Canceled" + ChatColor.YELLOW + ": " + ChatColor.RED + currentClass.getName());
        }

        if (factionPlayer.hasTimer(ETimerType.CLASS)) {
            if (factionPlayer.isPreferScoreboardDisplay()) {
                factionPlayer.getScoreboard().removeLine(ETimerType.CLASS.getScoreboardPosition());
            }

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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCombatTagSplash(PlayerSplashPlayerEvent event) {
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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCombatTagSplash(PlayerLingeringSplashEvent event) {
        if (event.isCancelled()) {
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

            if (factionPlayer.isPreferScoreboardDisplay()) {
                factionPlayer.getScoreboard().removeLine(timer.getType().getScoreboardPosition());
            }

            FMessage.printTimerCancelled(player, ChatColor.stripColor(timer.getType().getDisplayName()), "moved");
        }
    }

    /**
     * Handles cancelling timers which require the player to not take damage
     *
     * @param event EntityDamageEvent
     */
    @EventHandler
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

            if (factionPlayer.isPreferScoreboardDisplay()) {
                factionPlayer.getScoreboard().removeLine(timer.getType().getScoreboardPosition());
            }

            FMessage.printTimerCancelled(player, ChatColor.stripColor(timer.getType().getDisplayName()), "took damage");
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
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
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
}

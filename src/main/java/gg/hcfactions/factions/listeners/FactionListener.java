package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.EventStartEvent;
import gg.hcfactions.factions.listeners.events.faction.FactionFocusEvent;
import gg.hcfactions.factions.listeners.events.faction.FactionMemberDeathEvent;
import gg.hcfactions.factions.listeners.events.faction.FactionUnfocusEvent;
import gg.hcfactions.factions.listeners.events.player.ClassActivateEvent;
import gg.hcfactions.factions.listeners.events.player.ClassReadyEvent;
import gg.hcfactions.factions.listeners.events.player.ConsumeClassItemEvent;
import gg.hcfactions.factions.models.classes.EConsumableApplicationType;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.impl.*;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.EScoreboardEntryType;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.bukkit.utils.Players;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public record FactionListener(@Getter Factions plugin) implements Listener {
    /**
     * Handles limiting the classes for Factions
     *
     * @param event       Cancellable
     * @param player      Player
     * @param playerClass Player Class
     */
    private void handleClassLimiter(Cancellable event, Player player, IClass playerClass) {
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (faction == null) {
            return;
        }

        final int count = plugin.getClassManager().getFactionClassCount(faction, playerClass);

        if (playerClass instanceof Archer) {
            if (count >= plugin.getConfiguration().getArcherClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }

            return;
        }

        if (playerClass instanceof Rogue) {
            if (count >= plugin.getConfiguration().getRogueClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }

            return;
        }

        if (playerClass instanceof Bard) {
            if (count >= plugin.getConfiguration().getBardClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }

            return;
        }

        if (playerClass instanceof Miner) {
            if (count >= plugin.getConfiguration().getMinerClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }
        }

        if (playerClass instanceof Diver) {
            if (count >= plugin.getConfiguration().getDiverClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }
        }

        if (playerClass instanceof Tank) {
            if (count >= plugin.getConfiguration().getGuardianClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles enforcing class limits
     * @param event ClassReadyEvent
     */
    @EventHandler(priority = EventPriority.HIGH) /* Applies class limit on ready */
    public void onClassReady(ClassReadyEvent event) {
        if (event.isCancelled()) {
            return;
        }

        handleClassLimiter(event, event.getPlayer(), event.getPlayerClass());
    }

    @EventHandler (priority = EventPriority.HIGH) /* Applies class limit on activate */
    public void onClassActivate(ClassActivateEvent event) {
        if (event.isCancelled()) {
            return;
        }

        handleClassLimiter(event, event.getPlayer(), event.getPlayerClass());
    }

    @EventHandler /* Enforces timers and effects for class consumption */
    public void onClassConsume(ConsumeClassItemEvent event) {
        final Player player = event.getPlayer();
        final IClass playerClass = event.getPlayerClass();
        final FactionPlayer profile = (FactionPlayer) getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile.hasTimer(ETimerType.PROTECTION)) {
            player.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_PERFORM_PVP_PROT.getErrorDescription());
            event.setCancelled(true);
            return;
        }

        // TODO: Eventually fix this but it's expected only bard will give effects for now
        if (!(playerClass instanceof final Bard bard)) {
            return;
        }

        if (event.getConsumable().getApplicationType().equals(EConsumableApplicationType.INDIVIDUAL)) {
            return;
        }

        if (event.getConsumable().getApplicationType().equals(EConsumableApplicationType.ALL)) {
            final List<Player> friendlies = FactionUtil.getNearbyFriendlies(plugin, player, bard.getBardRange());
            final List<Player> enemies = FactionUtil.getNearbyEnemies(plugin, player, bard.getBardRange());
            friendlies.forEach(friendly -> event.getAffectedPlayers().put(friendly.getUniqueId(), true));
            enemies.forEach(enemy -> event.getAffectedPlayers().put(enemy.getUniqueId(), false));
            return;
        }

        if (event.getConsumable().getApplicationType().equals(EConsumableApplicationType.FRIEND_ONLY)) {
            final List<Player> friendlies = FactionUtil.getNearbyFriendlies(plugin, player, bard.getBardRange());
            friendlies.forEach(friendly -> event.getAffectedPlayers().put(friendly.getUniqueId(), true));
            return;
        }

        if (event.getConsumable().getApplicationType().equals(EConsumableApplicationType.ENEMY_ONLY)) {
            final List<Player> enemies = FactionUtil.getNearbyEnemies(plugin, player, bard.getBardRange());
            enemies.forEach(enemy -> event.getAffectedPlayers().put(enemy.getUniqueId(), false));
        }
    }

    /**
     * Allows projectiles to phase through allies
     * @param event ProjectileHitEvent
     */
    @EventHandler
    public void onProjectileCollide(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof final Player player)) {
            return;
        }

        if (!(event.getHitEntity() instanceof final Player otherPlayer)) {
            return;
        }

        if (!(event.getEntity().getType().equals(EntityType.ARROW)
                || event.getEntity().getType().equals(EntityType.SPECTRAL_ARROW)
                || event.getEntity().getType().equals(EntityType.ENDER_PEARL)
                || event.getEntity().getType().equals(EntityType.TRIDENT))) {
            return;
        }

        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (faction == null || !faction.isMember(otherPlayer)) {
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Prints a faction raidable broadcast if a faction
     * loses enough DTR to go negative
     *
     * @param event FactionMemberDeathEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onFactionMemberDeath(FactionMemberDeathEvent event) {
        final PlayerFaction faction = event.getFaction();
        final double subtracted = event.getSubtractedDTR();

        if (faction.isRaidable()) {
            return;
        }

        if ((faction.getDtr() - subtracted) > 0.0) {
            return;
        }

        FMessage.broadcastFactionRaidable(faction);
    }

    /**
     * Play an audio queue for focused player
     * @param event FactionFocusEvent
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onFactionFocus(FactionFocusEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player toFocus = event.getFocusedPlayer();
        Players.playSound(toFocus, Sound.BLOCK_NOTE_BLOCK_BANJO);
    }

    /**
     * Prints unfocused message to focused player
     * @param event FactionUnfocusEvent
     */
    @EventHandler
    public void onFactionUnfocus(FactionUnfocusEvent event) {
        final PlayerFaction focusingFaction = event.getFaction();
        final Player focusedPlayer = event.getFocusedPlayer();

        FMessage.printNoLongerFocused(focusingFaction, focusedPlayer);
    }

    /**
     * Cleans up focused players quitting
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        plugin.getFactionManager().getPlayerFactions()
                .stream()
                .filter(pf -> pf.getFocusedPlayerId() != null && pf.getFocusedPlayerId().equals(player.getUniqueId()))
                .forEach(f -> {

                    f.getOnlineMembers().forEach(onlineMember -> {
                        final FactionPlayer onlineFactionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
                        onlineFactionPlayer.removeFromScoreboard(player, EScoreboardEntryType.FOCUS);
                    });
                });
    }

    /**
     * Updates nameplates to focus color
     * @param event FactionFocusEvent
     */
    @EventHandler (priority = EventPriority.HIGH)
    public void onFocusNameplateUpdate(FactionFocusEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player toFocus = event.getFocusedPlayer();
        final PlayerFaction faction = event.getFaction();

        faction.getOnlineMembers().forEach(onlineMember -> {
            final Player onlinePlayer = onlineMember.getBukkit();
            final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(onlinePlayer);

            if (factionPlayer != null) {
                factionPlayer.addToScoreboard(toFocus, EScoreboardEntryType.FOCUS);
            }
        });
    }

    /**
     * Removes focus color from nameplates
     * @param event FactionUnfocusEvent
     */
    @EventHandler
    public void onUnfocusUpdate(FactionUnfocusEvent event) {
        final Player focused = event.getFocusedPlayer();
        final PlayerFaction faction = event.getFaction();

        faction.getOnlineMembers().forEach(onlineMember -> {
            final Player onlinePlayer = onlineMember.getBukkit();
            final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(onlinePlayer);

            if (factionPlayer != null) {
                factionPlayer.removeFromScoreboard(focused, EScoreboardEntryType.FOCUS);
            }
        });
    }

    /**
     * Resets all reinvites when a Palace event starts
     *
     * @param event EventStartEvent
     */
    @EventHandler
    public void onEventStart(EventStartEvent event) {
        if (!(event.getEvent() instanceof PalaceEvent)) {
            return;
        }

        plugin.getFactionManager().getPlayerFactions().forEach(pf -> pf.setReinvites(plugin.getConfiguration().getDefaultFactionReinvites()));
    }
}

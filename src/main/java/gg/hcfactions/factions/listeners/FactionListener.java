package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.ClassActivateEvent;
import gg.hcfactions.factions.listeners.events.player.ClassReadyEvent;
import gg.hcfactions.factions.listeners.events.player.ConsumeClassItemEvent;
import gg.hcfactions.factions.models.classes.EConsumableApplicationType;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.impl.*;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.utils.FactionUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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
            if (count > plugin.getConfiguration().getArcherClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }

            return;
        }

        if (playerClass instanceof Rogue) {
            if (count > plugin.getConfiguration().getRogueClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }

            return;
        }

        if (playerClass instanceof Bard) {
            if (count > plugin.getConfiguration().getBardClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }

            return;
        }

        if (playerClass instanceof Miner) {
            if (count > plugin.getConfiguration().getMinerClassLimit()) {
                playerClass.deactivate(player, false);
                player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                event.setCancelled(true);
            }
        }

        if (playerClass instanceof Diver) {
            if (count > plugin.getConfiguration().getDiverClassLimit()) {
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
}

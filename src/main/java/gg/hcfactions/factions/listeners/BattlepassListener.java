package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.KOTHTickEvent;
import gg.hcfactions.factions.listeners.events.player.BattlepassCompleteEvent;
import gg.hcfactions.factions.listeners.events.player.BattlepassIncrementEvent;
import gg.hcfactions.factions.models.battlepass.EBPObjectiveType;
import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import gg.hcfactions.factions.models.battlepass.impl.BPTracker;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import gg.hcfactions.libs.bukkit.utils.Players;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public record BattlepassListener(@Getter Factions plugin) implements Listener {
    /**
     * Listens for block break events and tracks accordingly
     *
     * @param event BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        plugin.getBattlepassManager().getActiveObjectives().forEach(activeObj -> {
            if (activeObj.getObjectiveType().equals(EBPObjectiveType.BLOCK_BREAK) && activeObj.meetsRequirement(player, block.getLocation())) {
                final BPTracker tracker = plugin.getBattlepassManager().getTracker(player);

                if (!plugin.getBattlepassManager().isBeingTracked(player)) {
                    plugin.getBattlepassManager().getTrackerRepository().add(tracker);
                }

                tracker.addToObjective(activeObj, 1);
            }
        });
    }

    /**
     * Listens for entity deaths and tracks accordingly
     *
     * @param event EntityDeathEvent
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        final LivingEntity entity = event.getEntity();
        final Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        plugin.getBattlepassManager().getActiveObjectives().forEach(activeObj -> {
            if (activeObj.getObjectiveType().equals(EBPObjectiveType.ENTITY_SLAIN) && activeObj.meetsRequirement(killer, entity)) {
                final BPTracker tracker = plugin.getBattlepassManager().getTracker(killer);

                if (!plugin.getBattlepassManager().isBeingTracked(killer)) {
                    plugin.getBattlepassManager().getTrackerRepository().add(tracker);
                }

                tracker.addToObjective(activeObj, 1);
            }
        });
    }

    /**
     * Listens for player fish events and tracks accordingly
     *
     * @param event PlayerFishEvent
     */
    @EventHandler
    public void onEntityFish(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final Entity fish = event.getCaught();

        plugin.getBattlepassManager().getActiveObjectives().forEach(activeObj -> {
            if (activeObj.getObjectiveType().equals(EBPObjectiveType.FISH) && activeObj.meetsRequirement(player, fish)) {
                final BPTracker tracker = plugin.getBattlepassManager().getTracker(player);

                if (!plugin.getBattlepassManager().isBeingTracked(player)) {
                    plugin.getBattlepassManager().getTrackerRepository().add(tracker);
                }

                tracker.addToObjective(activeObj, 1);
            }
        });
    }

    /**
     * Listens for Battlepass progress with Capture KOTH Ticket events
     * @param event KOTHTickEvent
     */
    @EventHandler
    public void onKothTick(KOTHTickEvent event) {
        final KOTHEvent kothEvent = event.getEvent();
        final PlayerFaction capturingFaction = event.getCapturingFaction();

        capturingFaction.getOnlineMembers().forEach(onlineMember -> {
            final BPTracker tracker = plugin.getBattlepassManager().getTracker(onlineMember.getBukkit());

            plugin.getBattlepassManager().getActiveObjectives().stream().filter(obj -> obj.getObjectiveType().equals(EBPObjectiveType.CAPTURE_KOTH_TICKET)).forEach(captureObj -> {
                if (captureObj.meetsRequirement(onlineMember.getBukkit(), kothEvent.getCaptureRegion().getCornerA().getBukkitBlock().getLocation())) {
                    if (!plugin.getBattlepassManager().isBeingTracked(onlineMember.getBukkit())) {
                        plugin.getBattlepassManager().getTrackerRepository().add(tracker);
                    }

                    tracker.addToObjective(captureObj, 1);
                }
            });
        });
    }

    /**
     * Listens for player joins and stores their join time to apply
     * daily login bonus
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        plugin.getBattlepassManager().loadTracker(player).ifPresent(tracker -> plugin.getBattlepassManager().getTrackerRepository().add(tracker));
    }

    /**
     * Listens for player quits and decaches trackers
     *
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final BPTracker tracker = plugin.getBattlepassManager().getTracker(player);

        plugin.getBattlepassManager().getTrackerRepository().remove(tracker);

        if (tracker.isEmpty()) {
            plugin.getBattlepassManager().resetTracker(tracker);
            return;
        }

        plugin.getBattlepassManager().saveTracker(tracker);
    }

    /**
     * Listens for Battlepass progress updates and prints progress
     *
     * @param event BattlepassIncrementEvent
     */
    @EventHandler
    public void onBattlepassIncrement(BattlepassIncrementEvent event) {
        final Player player = event.getPlayer();
        final BPObjective obj = event.getObjective();
        final BPTracker tracker = plugin.getBattlepassManager().getTracker(player);
        final int progress = tracker.getProgress(obj);
        final int amount = event.getAmount();
        final int addedAmount = progress + amount;
        final int div = obj.getAmountRequirement() / 4;

        if (addedAmount % div == 0 && addedAmount > 1 && addedAmount < obj.getAmountRequirement()) {
            FMessage.printBattlepassProgress(player, obj, addedAmount);
            Players.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT);
        }
    }

    /**
     * Listens for Battlepass completion and grants EXP
     *
     * @param event BattlepassCompleteEvent
     */
    @EventHandler
    public void onBattlepassComplete(BattlepassCompleteEvent event) {
        final Player player = event.getPlayer();
        final BPObjective obj = event.getObjective();
        final RankService rankService = (RankService) plugin.getService(RankService.class);
        double expMultiplier = 0.0;

        if (rankService != null) {
            final AresRank rank = rankService.getHighestRank(player);
            expMultiplier = plugin.getBattlepassManager().getRankMultipliers().getOrDefault(rank, 0.0);
        }

        FMessage.printBattlepassComplete(player, obj, expMultiplier);
        Players.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP);
    }
}

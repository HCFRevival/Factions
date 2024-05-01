package gg.hcfactions.factions.listeners;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.KOTHTickEvent;
import gg.hcfactions.factions.events.tracker.EventTrackerManager;
import gg.hcfactions.factions.listeners.events.faction.FactionTicketLossEvent;
import gg.hcfactions.factions.listeners.events.player.*;
import gg.hcfactions.factions.events.event.EventTrackerPublishEvent;
import gg.hcfactions.factions.models.classes.impl.Bard;
import gg.hcfactions.factions.models.events.impl.tracking.entry.EventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.tracking.entry.types.DeathEventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.tracking.entry.types.KOTHTickEventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.tracking.entry.types.KOTHTicketLossEventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.tracking.entry.types.KillEventTrackerEntry;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.tracking.IEventTrackerPlayer;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import gg.hcfactions.libs.bukkit.location.ILocatable;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class EventTrackerListener implements Listener {
    @Getter public final Factions plugin;
    private final Set<BardAssistTracker> pendingBardAssists;

    public EventTrackerListener(Factions plugin) {
        this.plugin = plugin;
        this.pendingBardAssists = Sets.newConcurrentHashSet();
    }

    private record BardAssistTracker(@Getter UUID bardPlayerId, @Getter Set<UUID> buffedPlayerIds) {}

    // Creates a new entry in a player tracker object for the provided player and location
    private void createPlayerEntry(ILocatable origin, Player player, String key, Number value) {
        plugin.getEventManager().getTrackerManager().getTrackerByLocation(origin).ifPresent(tracker -> {
            final IEventTrackerPlayer trackedPlayer = plugin.getEventManager().getTrackerManager().getOrCreatePlayerTracker(player, tracker);
            trackedPlayer.add(key, value);
        });
    }

    // Creates a wrapper object to help determine bard kills
    private void createBardAssist(Player bardPlayer, Set<UUID> buffedPlayerIds, int effectDuration) {
        final UUID bardPlayerId = bardPlayer.getUniqueId();
        pendingBardAssists.add(new BardAssistTracker(bardPlayer.getUniqueId(), buffedPlayerIds));
        new Scheduler(plugin).sync(() -> pendingBardAssists.removeIf(pba -> pba.getBardPlayerId().equals(bardPlayerId))).delay(effectDuration * 20L).run();
    }

    // Returns any bard kill assist trackers this player is a part of
    private ImmutableList<BardAssistTracker> getAssistTrackers(Player buffedPlayer) {
        return ImmutableList.copyOf(pendingBardAssists.stream().filter(pba -> pba.getBuffedPlayerIds().contains(buffedPlayer.getUniqueId())).collect(Collectors.toList()));
    }

    // Helper event to create death entries
    private EventTrackerEntry createDeathEvent(UUID slainUniqueId, UUID killerUniqueId, String slainUsername, String killerUsername, BLocatable deathLocation) {
        if (killerUniqueId != null && killerUsername != null) {
            return new KillEventTrackerEntry(
                    plugin,
                    killerUniqueId,
                    killerUsername,
                    slainUniqueId,
                    slainUsername,
                    deathLocation
            );
        }

        return new DeathEventTrackerEntry(
                plugin,
                slainUniqueId,
                slainUsername,
                deathLocation
        );
    }

    @EventHandler
    public void onEventTrackerPublish(EventTrackerPublishEvent event) {
        final String url = event.getUrl();

        // slight delay to bypass any shit being spammed in chat after the event
        new Scheduler(plugin).sync(() -> FMessage.broadcastEventTrackerPublish(url)).delay(5*20L).run();
    }

    /**
     * Handles updating existing player tracker if
     * the player changed their username
     * @param event Bukkit PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        plugin.getEventManager().getTrackerManager()
                .getActiveTrackers(player.getUniqueId())
                .stream()
                .filter(tracker -> !tracker.getUsername().equals(player.getName()))
                .forEach(trackerNeedUpdate -> trackerNeedUpdate.setUsername(player.getName()));
    }

    /**
     * Handles creating a player death event
     * for a tracked event
     * @param event Bukkit PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player slainPlayer = event.getEntity();
        final Player killerPlayer = slainPlayer.getKiller();

        if (killerPlayer != null) {
            final BLocatable location = new BLocatable(killerPlayer.getLocation().getBlock());

            plugin.getEventManager().getTrackerManager().getTrackerByLocation(location).ifPresent(tracker -> tracker.addEntry(createDeathEvent(
                    slainPlayer.getUniqueId(),
                    killerPlayer.getUniqueId(),
                    slainPlayer.getName(),
                    killerPlayer.getName(),
                    location
            )));

            createPlayerEntry(location, killerPlayer, EventTrackerManager.P_KILLS, 1);
            return;
        }

        final BLocatable location = new BLocatable(slainPlayer.getLocation().getBlock());

        plugin.getEventManager().getTrackerManager().getTrackerByLocation(location).ifPresent(tracker -> tracker.addEntry(createDeathEvent(
                slainPlayer.getUniqueId(),
                null,
                slainPlayer.getName(),
                null,
                location
        )));

        createPlayerEntry(location, slainPlayer, EventTrackerManager.P_DEATHS, 1);
    }

    /**
     * Handles creating a player death event
     * for a tracked event
     * @param event HCFR CombatLoggerDeathEvent
     */
    @EventHandler
    public void onCombatLoggerDeath(CombatLoggerDeathEvent event) {
        final UUID slainUniqueId = event.getLogger().getOwnerId();
        final String slainUsername = event.getLogger().getOwnerUsername();
        final Player killerPlayer = event.getKiller();

        if (killerPlayer != null) {
            final BLocatable location = new BLocatable(killerPlayer.getLocation().getBlock());

            plugin.getEventManager().getTrackerManager().getTrackerByLocation(location).ifPresent(tracker -> tracker.addEntry(createDeathEvent(
                    slainUniqueId,
                    killerPlayer.getUniqueId(),
                    slainUsername,
                    killerPlayer.getName(),
                    location
            )));

            return;
        }

        final BLocatable location = new BLocatable(event.getLogger().getBukkitEntity().getLocation().getBlock());

        plugin.getEventManager().getTrackerManager().getTrackerByLocation(location).ifPresent(tracker -> tracker.addEntry(createDeathEvent(
                slainUniqueId,
                null,
                slainUsername,
                null,
                location
        )));
    }

    /**
     * Handles creating a koth tick entry
     * @param event HCFR KOTHTickEvent
     */
    @EventHandler
    public void onTicketCaptured(KOTHTickEvent event) {
        final KOTHEvent kothEvent = event.getEvent();
        final PlayerFaction faction = event.getCapturingFaction();
        kothEvent.getSession().getTracker().addEntry(new KOTHTickEventTrackerEntry(plugin, faction.getUniqueId(), event.getNewTicketCount()));
    }

    /**
     * Handles creating a koth ticket loss entry
     * @param event HCFR FactionTicketLossEvent
     */
    @EventHandler
    public void onTicketLoss(FactionTicketLossEvent event) {
        if (!(event.getEvent() instanceof final KOTHEvent kothEvent)) {
            return;
        }

        kothEvent.getSession().getTracker().addEntry(new KOTHTicketLossEventTrackerEntry(
                plugin,
                event.getFaction().getUniqueId(),
                (event.getNewTicketCount() - event.getOldTicketCount()))
        );
    }

    /**
     * Handles increasing damage for a player being
     * tracked in an event
     * @param event Ares PlayerDamagePlayerEvent
     */
    @EventHandler
    public void onPlayerAttackPlayer(PlayerDamagePlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final double damage = event.getDamage();

        createPlayerEntry(new PLocatable(attacker), attacker, EventTrackerManager.P_DAMAGE_DEALT, damage);
        createPlayerEntry(new PLocatable(attacked), attacker, EventTrackerManager.P_DAMAGE_TAKEN, damage);
    }

    /**
     * Handles increasing archer damage values
     * @param event HCFR ArcherTagEvent
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onArcherTag(ArcherTagEvent event) {
        if (event.isCancelled() || !(event.getAttacked() instanceof Player)) {
            return;
        }

        final Player player = event.getPlayer();
        createPlayerEntry(new PLocatable(player), player, EventTrackerManager.P_ARCHER_RANGE_DMG, event.getDamage());
    }

    /**
     * Handles tracking archer marked players
     * @param event HCFR ArcherMarkEvent
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onArcherMarkEvent(ArcherMarkEvent event) {
        if (event.isCancelled() || !(event.getAttacked() instanceof Player)) {
            return;
        }

        final Player player = event.getPlayer();
        createPlayerEntry(new PLocatable(player), player, EventTrackerManager.P_ARCHER_TAG_HIT, 1);
    }

    /**
     * Handles tracking Rogue backstabs
     * @param event HCFR RogueBackstabEvent
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onRogueBackstab(RogueBackstabEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        createPlayerEntry(new PLocatable(player), player, EventTrackerManager.P_ROGUE_BACKSTAB, 1);
    }

    /**
     * Handles tracking diver damage dealt using Tridents
     * @param event HCFR DiverPierceEvent
     */
    @EventHandler
    public void onDiverPierce(DiverPierceEvent event) {
        if (event.isCancelled() || !(event.getAttacked() instanceof Player)) {
            return;
        }

        final Player player = event.getPlayer();
        createPlayerEntry(new PLocatable(player), player, EventTrackerManager.P_DIVER_DMG, event.getDamage());
    }

    /**
     * Handles tracking bard item consumption analytics
     * @param event HCFR PostConsumeClassItemEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBardItemConsume(PostConsumeClassItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getPlayerClass() instanceof Bard)) {
            return;
        }

        final Player player = event.getPlayer();

        if (event.getConsumable().getEffectType().equals(PotionEffectType.STRENGTH)) {
            pendingBardAssists.removeIf(pba -> pba.getBardPlayerId().equals(player.getUniqueId()));
            createBardAssist(player, event.getAffectedPlayers(), event.getConsumable().getDuration());
        }

        createPlayerEntry(new PLocatable(player), player, EventTrackerManager.P_BARD_EFFECT_GIVEN, event.getAffectedPlayers().size());
    }

    /**
     * Handles tracking assists on kills that were
     * enabled by a bard giving a player an effect
     * @param event Bukkit PlayerDeathEvent
     */
    @EventHandler
    public void onBardKillAssist(PlayerDeathEvent event) {
        final Player slainPlayer = event.getEntity();

        if (slainPlayer.getKiller() == null) {
            return;
        }

        final Player killerPlayer = slainPlayer.getKiller();

        getAssistTrackers(killerPlayer).forEach(assistTracker -> {
            final Player bardPlayer = Bukkit.getPlayer(assistTracker.getBardPlayerId());

            if (bardPlayer != null && bardPlayer.isOnline()) {
                createPlayerEntry(new PLocatable(killerPlayer), bardPlayer, EventTrackerManager.P_BARD_ASSISTS, 1);
            }
        });
    }

    /**
     * Handles tracking item consumption analytics
     * @param event Bukkit PlayerItemConsumeEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (item.getType().equals(Material.ENCHANTED_GOLDEN_APPLE)) {
            createPlayerEntry(new PLocatable(player), player, EventTrackerManager.P_GAPPLES_USED, 1);
        }
    }

    /**
     * Handles tracking potion splash analytics
     * @param event Bukkit PotionSplashEvent
     */
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getPotion().getShooter() instanceof final Player player)) {
            return;
        }

        final ThrownPotion potion = event.getPotion();
        final ItemStack item = potion.getItem();
        final PotionMeta meta = (PotionMeta) item.getItemMeta();

        if (meta == null || meta.getBasePotionType() == null) {
            return;
        }

        if (meta.getBasePotionType().getPotionEffects().stream().anyMatch(eff -> eff.getType().equals(PotionEffectType.INSTANT_HEALTH))) {
            createPlayerEntry(new PLocatable(player), player, EventTrackerManager.P_HEALTH_POTIONS_USED, 1);
        }
    }

    /**
     * Handles tracking totem consumption
     * @param event Bukkit EntityResurrectEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityRevive(EntityResurrectEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }

        createPlayerEntry(new PLocatable(player), player, EventTrackerManager.P_TOTEMS_USED, 1);
    }

    /**
     * Handles tracking tank guard effect application
     * @param event HCFR TankGuardApplyEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onTankGuardApply(TankGuardApplyEvent event) {
        final Player player = event.getPlayer();

        if (event.isCancelled()) {
            return;
        }

        createPlayerEntry(new PLocatable(player), player, EventTrackerManager.P_TANK_GUARD, 1);
    }
}

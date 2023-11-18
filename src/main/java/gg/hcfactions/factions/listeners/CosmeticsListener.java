package gg.hcfactions.factions.listeners;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.DPSCaptureEvent;
import gg.hcfactions.factions.events.event.KOTHCaptureEvent;
import gg.hcfactions.factions.listeners.events.player.CombatLoggerDeathEvent;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.utils.Worlds;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public final class CosmeticsListener implements Listener {
    @Getter Factions plugin;
    @Getter public Random random;
    @Getter public Set<UUID> recentlyPerformedHeadLookup;

    public CosmeticsListener(Factions plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.recentlyPerformedHeadLookup = Sets.newConcurrentHashSet();
    }

    private ItemStack getPlayerHead(UUID uniqueId) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) item.getItemMeta();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);

        if (meta != null) {
            meta.setOwningPlayer(offlinePlayer);
            item.setItemMeta(meta);
        }

        return item;
    }

    private void playEventCelebration(IEvent event, PlayerFaction winner) {
        final List<Claim> eventClaims = plugin.getClaimManager().getClaimsByOwner(event.getOwner());

        if (eventClaims.isEmpty()) {
            return;
        }

        final List<FireworkEffect.Type> effectTypes = List.of(FireworkEffect.Type.values());
        final List<Color> colors = List.of(Color.AQUA, Color.RED, Color.GREEN, Color.YELLOW, Color.PURPLE, Color.FUCHSIA);

        long fireworkDelay = 0;

        for (PlayerFaction.Member onlineMember : winner.getOnlineMembers()) {
            final Player onlinePlayer = onlineMember.getBukkit();

            for (Claim eventClaim : eventClaims) {
                if (onlinePlayer == null || !eventClaim.isInside(new PLocatable(onlinePlayer), false)) {
                    continue;
                }

                final FireworkEffect.Type fireworkType = effectTypes.get(Math.abs(random.nextInt(effectTypes.size())));
                final Color fireworkColor = colors.get(Math.abs(random.nextInt(colors.size())));
                final FireworkEffect effect = FireworkEffect.builder()
                        .with(fireworkType)
                        .withColor(fireworkColor)
                        .withFade(Color.SILVER)
                        .withTrail()
                        .withFlicker()
                        .build();

                new Scheduler(plugin).sync(() -> Worlds.spawnFirework(plugin, onlinePlayer.getLocation(), 2, 40, effect)).delay(20L * fireworkDelay).run();
                fireworkDelay += 2;
            }
        }
    }

    @EventHandler
    public void onDropPlayerHead(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final ItemStack head = getPlayerHead(player.getUniqueId());
        event.getDrops().add(head);
    }

    @EventHandler
    public void onDropCombatLoggerHead(CombatLoggerDeathEvent event) {
        final UUID ownerUniqueId = event.getLogger().getOwnerId();
        final ItemStack head = getPlayerHead(ownerUniqueId);
        event.getLogger().getLoggerInventory().add(head);
    }

    @EventHandler
    public void onCaptureEvent(KOTHCaptureEvent event) {
        final KOTHEvent koth = event.getEvent();
        final PlayerFaction capturingFaction = event.getCapturingFaction();
        playEventCelebration(koth, capturingFaction);
    }

    @EventHandler
    public void onCaptureDPS(DPSCaptureEvent event) {
        final DPSEvent dps = event.getEvent();
        final PlayerFaction capturingFaction = event.getCapturingFaction();
        playEventCelebration(dps, capturingFaction);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final Block block = event.getClickedBlock();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (block == null || !(block.getState() instanceof final Skull skull)) {
            return;
        }

        if (recentlyPerformedHeadLookup.contains(player.getUniqueId())) {
            return;
        }

        if (skull.getOwningPlayer() == null) {
            return;
        }

        recentlyPerformedHeadLookup.add(uniqueId);
        new Scheduler(plugin).sync(() -> recentlyPerformedHeadLookup.remove(uniqueId)).delay(20L).run();

        player.sendMessage(ChatColor.YELLOW + skull.getOwningPlayer().getName() + "'s Head");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!event.getEntity().getType().equals(EntityType.ENDER_DRAGON)) {
            return;
        }

        final LivingEntity dragon = event.getEntity();
        final Player killer = dragon.getKiller();

        if (killer != null) {
            final RankService rankService = (RankService) plugin.getService(RankService.class);
            final PlayerFaction pf = plugin.getFactionManager().getPlayerFactionByPlayer(killer);

            for (Player player : Bukkit.getOnlinePlayers()) {
                final StringBuilder displayName = new StringBuilder(rankService.getFormattedName(killer));

                if (pf != null) {
                    displayName.insert(0, (pf.isMember(player)
                            ? ChatColor.DARK_GREEN + "[" + pf.getName() + "] "
                            : FMessage.LAYER_2 + "[" + FMessage.LAYER_1 + pf.getName() + FMessage.LAYER_2 + "] "));
                }

                player.sendMessage(" ");
                player.sendMessage(ChatColor.DARK_PURPLE + "The " + ChatColor.LIGHT_PURPLE + "Ender Dragon" + ChatColor.DARK_PURPLE + " has been slain by " + displayName);
                player.sendMessage(" ");
            }
        }
    }

    /**
     * Adds rank display to tablist
     *
     * TODO: Should we move this to CommandX?
     *
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final RankService rankService = (RankService) plugin.getService(RankService.class);

        if (rankService == null) {
            return;
        }

        player.setPlayerListName(rankService.getFormattedName(player));
    }
}

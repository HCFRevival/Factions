package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.utils.Worlds;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Rogue implements IClass {
    @Getter public ClassManager manager;
    @Getter public final String name = "Rogue";
    @Getter public final String description = "Grants you the ability to backstab players with Golden Swords";
    @Getter public final int warmup;
    @Getter public final boolean emptyArmorEnforced = true;
    @Getter public final Material helmet = Material.CHAINMAIL_HELMET;
    @Getter public final Material chestplate = Material.CHAINMAIL_CHESTPLATE;
    @Getter public final Material leggings = Material.CHAINMAIL_LEGGINGS;
    @Getter public final Material boots = Material.CHAINMAIL_BOOTS;
    @Getter public final Material offhand = null;
    @Getter public Set<UUID> activePlayers;
    @Getter public Map<PotionEffectType, Integer> passiveEffects;
    @Getter public List<IConsumeable> consumables;
    @Getter public final Map<UUID, Long> backstabCooldowns;
    @Getter public final Map<UUID, Long> invisCooldowns;
    @Getter public final Map<UUID, InvisibilityState> invisibilityStates;
    @Getter @Setter public double backstabDamage;
    @Getter @Setter public int backstabTickrate;
    @Getter @Setter public int backstabCooldown;
    @Getter @Setter public int grappleCooldown;
    @Getter @Setter public int invisibilityCooldown;
    @Getter @Setter public boolean invisibilityEnabled;
    @Getter @Setter public double fullInvisibilityMinRadius;
    @Getter @Setter public double partialInvisibilityMinRadius;
    @Getter @Setter public double grappleHorizontalSpeed;
    @Getter @Setter public double grappleVerticalSpeed;
    private BukkitTask invisibilityStateUpdateTask;

    public Rogue(ClassManager manager) {
        this.manager = manager;
        this.warmup = 30;
        this.backstabDamage = 1.0;
        this.backstabTickrate = 10;
        this.backstabCooldown = 3;
        this.fullInvisibilityMinRadius = 16.0;
        this.partialInvisibilityMinRadius = 8.0;
        this.invisibilityEnabled = true;
        this.grappleHorizontalSpeed = 1.25;
        this.grappleVerticalSpeed = 0.25;
        this.grappleCooldown = 5;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.backstabCooldowns = Maps.newConcurrentMap();
        this.invisCooldowns = Maps.newConcurrentMap();
        this.invisibilityStates = Maps.newConcurrentMap();

        initInvisibilityTask();
    }

    public Rogue(ClassManager manager,
                 int warmup,
                 double backstabDamage,
                 int backstabTickrate,
                 int backstabCooldown,
                 int invisCooldown,
                 int grappleCooldown,
                 double grappleHorizontalSpeed,
                 double grappleVerticalSpeed,
                 boolean invisEnabled,
                 double fullInvisMinRadius,
                 double partialInvisMinRadius
    ) {
        this.manager = manager;
        this.warmup = warmup;
        this.activePlayers = Sets.newConcurrentHashSet();
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.backstabCooldowns = Maps.newConcurrentMap();
        this.invisCooldowns = Maps.newConcurrentMap();
        this.backstabDamage = backstabDamage;
        this.backstabTickrate = backstabTickrate;
        this.backstabCooldown = backstabCooldown;
        this.grappleCooldown = grappleCooldown;
        this.invisibilityCooldown = invisCooldown;
        this.invisibilityEnabled = invisEnabled;
        this.grappleHorizontalSpeed = grappleHorizontalSpeed;
        this.grappleVerticalSpeed = grappleVerticalSpeed;
        this.fullInvisibilityMinRadius = fullInvisMinRadius;
        this.partialInvisibilityMinRadius = partialInvisMinRadius;
        this.invisibilityStates = Maps.newConcurrentMap();

        initInvisibilityTask();
    }

    public boolean hasBackstabCooldown(Player player) {
        return backstabCooldowns.containsKey(player.getUniqueId());
    }

    public boolean hasInvisCooldown(Player player) {
        return invisCooldowns.containsKey(player.getUniqueId());
    }

    public boolean isInvisible(Player player) {
        return invisibilityStates.containsKey(player.getUniqueId()) && !invisibilityStates.get(player.getUniqueId()).equals(InvisibilityState.NONE);
    }

    public Map<UUID, InvisibilityState> getInvisiblePlayers() {
        final Map<UUID, InvisibilityState> res = Maps.newHashMap();

        invisibilityStates.forEach((id, state) -> {
            if (!state.equals(InvisibilityState.NONE)) {
                res.put(id, state);
            }
        });

        return res;
    }

    public InvisibilityState getExpectedInvisibilityState(Player player) {
        final List<Player> inFullRadius = FactionUtil.getNearbyEnemies(manager.getPlugin(), player, fullInvisibilityMinRadius);
        final List<Player> inPartialRadius = FactionUtil.getNearbyEnemies(manager.getPlugin(), player, partialInvisibilityMinRadius);

        if (!inPartialRadius.isEmpty()) {
            return InvisibilityState.NONE;
        }

        if (!inFullRadius.isEmpty()) {
            return InvisibilityState.PARTIAL;
        }

        return InvisibilityState.FULL;
    }

    public void unvanishPlayer(Player player, String reason) {
        unvanishPlayer(player);
        FMessage.printRogueUncloak(player, reason);
    }

    public void unvanishPlayer(Player player) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.showPlayer(manager.getPlugin(), player));
        invisibilityStates.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    public void vanishPlayer(Player player) {
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(player);

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (!onlinePlayer.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                if (faction != null && faction.isMember(onlinePlayer)) {
                    return;
                }

                onlinePlayer.hidePlayer(manager.getPlugin(), player);
            }
        });
    }

    public void renderVanishState(Player player, InvisibilityState state) {
        final String result;

        if (state.equals(InvisibilityState.FULL)) {
            result = ChatColor.DARK_PURPLE + "Fully Cloaked";
        } else if (state.equals(InvisibilityState.PARTIAL)) {
            result = ChatColor.GOLD + "Partially Cloaked";
        } else {
            result = ChatColor.RED + "Cloak Disabled";
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(result));
    }

    public void updateVisibility(Player player, InvisibilityState expectedState) {
        final InvisibilityState currentState = invisibilityStates.get(player.getUniqueId());

        if (expectedState == currentState) {
            return;
        }

        invisibilityStates.put(player.getUniqueId(), expectedState);

        player.getWorld().spawnParticle(
                Particle.WITCH,
                player.getLocation().getX(),
                player.getLocation().getY() + 1.5,
                player.getLocation().getZ(),
                32,
                0.35, 0.35, 0.35,
                0.01
        );

        Worlds.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT);

        if (expectedState.equals(InvisibilityState.FULL)) {
            vanishPlayer(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0));
            return;
        }

        if (expectedState.equals(InvisibilityState.PARTIAL)) {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.showPlayer(manager.getPlugin(), player));
            return;
        }

        final PotionEffect existingInvis = player.getPotionEffect(PotionEffectType.INVISIBILITY);

        if (existingInvis != null && existingInvis.isInfinite()) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    private void initInvisibilityTask() {
        if (invisibilityStateUpdateTask != null) {
            return;
        }

        invisibilityStateUpdateTask = new Scheduler(getManager().getPlugin()).sync(() -> getInvisiblePlayers().forEach((id, currentState) -> {
            final Player player = Bukkit.getPlayer(id);

            if (player == null) {
                return;
            }

            final InvisibilityState expectedState = getExpectedInvisibilityState(player);
            renderVanishState(player, expectedState);

            if (expectedState == currentState) {
                return;
            }

            updateVisibility(player, expectedState);
        })).repeat(0L, 10L).run();
    }

    public void disableInvisibilityTask() {
        if (invisibilityStateUpdateTask == null) {
            return;
        }

        manager.getPlugin().getAresLogger().warn("Disabling invisibility task for Rogue");

        invisibilityStateUpdateTask.cancel();
        invisibilityStateUpdateTask = null;
    }

    public enum InvisibilityState {
        FULL, PARTIAL, NONE
    }
}

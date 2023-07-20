package gg.hcfactions.factions.listeners;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.Sugarcube;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * TODO: Migrate this to CommandX
 */
public final class HorseListener implements Listener {
    @Getter public final Factions plugin;
    private final Map<UUID, Long> sugarcubeCooldowns;

    public HorseListener(Factions plugin) {
        this.plugin = plugin;
        this.sugarcubeCooldowns = Maps.newConcurrentMap();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onMeasure(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();

        if (event.isCancelled()) {
            return;
        }

        if (!(event.getRightClicked() instanceof final Horse horse)) {
            return;
        }

        if (!hand.getType().equals(Material.CLOCK)) {
            return;
        }

        event.setCancelled(true);

        final AttributeInstance healthAttr = horse.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        final AttributeInstance moveSpeedAttr = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

        player.sendMessage(ChatColor.GOLD + "Horse Details");

        if (healthAttr != null) {
            player.sendMessage(ChatColor.RED + "Health" + ChatColor.RESET + ": " + String.format("%.2f",  healthAttr.getBaseValue()));
        }

        if (moveSpeedAttr != null) {
            player.sendMessage(ChatColor.AQUA + "Movement Speed" + ChatColor.RESET + ": " + String.format("%.2f", moveSpeedAttr.getBaseValue()));
        }

        player.sendMessage(ChatColor.GREEN + "Jump Strength" + ChatColor.RESET + ": " + String.format("%.2f", horse.getJumpStrength()));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onSugarCube(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();

        if (event.isCancelled()) {
            return;
        }

        if (!(event.getRightClicked() instanceof final Horse horse)) {
            return;
        }

        final CustomItemService cis = (CustomItemService)plugin.getService(CustomItemService.class);

        if (cis == null) {
            return;
        }

        final Optional<ICustomItem> customItemQuery = cis.getItem(hand);

        if (customItemQuery.isEmpty()) {
            return;
        }

        final ICustomItem customItem = customItemQuery.get();

        if (!(customItem instanceof Sugarcube)) {
            return;
        }

        event.setCancelled(true);

        if (sugarcubeCooldowns.containsKey(player.getUniqueId())) {
            final long expire = sugarcubeCooldowns.get(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Please wait " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(expire - Time.now()) + ChatColor.RED + "s before performing this action again");
            return;
        }

        final AttributeInstance moveAttr = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        final AttributeInstance healthAttr = horse.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (healthAttr == null || moveAttr == null) {
            player.sendMessage(ChatColor.RED + "Invalid entity attributes");
            return;
        }

        if (healthAttr.getBaseValue() >= 60.0 && moveAttr.getBaseValue() >= 0.3375 && horse.getJumpStrength() >= 1.0) {
            player.sendMessage(ChatColor.RED + "Horse has max attributes");
            return;
        }

        if (hand.getAmount() <= 0) {
            player.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
            player.getInventory().setItemInMainHand(hand);
        }

        if (moveAttr.getBaseValue() < 0.3375) {
            moveAttr.setBaseValue(Math.min(moveAttr.getBaseValue() + 0.01, 0.3375));
            player.sendMessage(ChatColor.AQUA + "Movement Speed Upgraded" + ChatColor.RESET + ": " + String.format("%.2f", moveAttr.getBaseValue()));
        }

        if (healthAttr.getBaseValue() < 60.0) {
            healthAttr.setBaseValue(Math.min(healthAttr.getBaseValue() + 1.0, 60.0));
            player.sendMessage(ChatColor.RED + "Health Upgrade" + ChatColor.RESET + ": " + String.format("%.2f", healthAttr.getBaseValue()));
        }

        if (horse.getJumpStrength() < 1.0) {
            horse.setJumpStrength(Math.min(horse.getJumpStrength() + 0.1, 1.0));
            player.sendMessage(ChatColor.GREEN + "Jump Strength Upgrade" + ChatColor.RESET + ": " + String.format("%.2f", horse.getJumpStrength()));
        }

        horse.getWorld().spawnParticle(Particle.HEART, horse.getLocation().add(0, 1.0, 0), 4);

        sugarcubeCooldowns.put(uniqueId, Time.now() + (60 * 1000L));
        new Scheduler(plugin).sync(() -> sugarcubeCooldowns.remove(uniqueId)).delay(60 * 20L).run();
    }
}

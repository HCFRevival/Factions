package gg.hcfactions.factions.listeners;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.FoundOreEvent;
import gg.hcfactions.libs.base.util.Strings;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Set;

@Getter
public final class FoundOreListener implements Listener {
    private static final ImmutableList<Material> TRACKED_ORES = ImmutableList.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.ANCIENT_DEBRIS);
    private static final Component FD_PREFIX = Component.text("[FO]", NamedTextColor.WHITE).appendSpace();
    private static final TextColor DIAMOND_COLOR = TextColor.color(0xB9F2FF);
    private static final TextColor NETHERITE_COLOR = TextColor.color(0x723232);

    public Factions plugin;
    public Set<Block> foundOres;

    public FoundOreListener(Factions plugin) {
        this.plugin = plugin;
        this.foundOres = Sets.newConcurrentHashSet();
    }

    private int countOres(List<Block> tracked, Block origin, int iter) {
        tracked.add(origin);
        foundOres.add(origin);

        iter += 1;

        if (iter > 32) {
            return tracked.size();
        }

        for (BlockFace face : BlockFace.values()) {
            if (face.equals(BlockFace.SELF)) {
                continue;
            }

            final Block otherBlock = origin.getRelative(face);

            if (!otherBlock.getType().equals(origin.getType())) {
                continue;
            }

            if (tracked.contains(otherBlock)) {
                continue;
            }

            if (foundOres.contains(otherBlock)) {
                continue;
            }

            countOres(tracked, otherBlock, iter);
        }

        return tracked.size();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (event.isCancelled()) {
            return;
        }

        if (!TRACKED_ORES.contains(block.getType())) {
            return;
        }

        if (block.hasMetadata("player_placed")) {
            return;
        }

        if (foundOres.contains(block)) {
            foundOres.remove(block);
            return;
        }

        final int found = countOres(Lists.newArrayList(), block, 0);

        if (block.getType().equals(Material.DIAMOND_ORE) || block.getType().equals(Material.DEEPSLATE_DIAMOND_ORE)) {
            final FoundOreEvent foundOreEvent = new FoundOreEvent(player, Material.DIAMOND_ORE, found);
            Bukkit.getPluginManager().callEvent(foundOreEvent);
        } else if (block.getType().equals(Material.ANCIENT_DEBRIS)) {
            final FoundOreEvent foundOreEvent = new FoundOreEvent(player, Material.ANCIENT_DEBRIS, found);
            Bukkit.getPluginManager().callEvent(foundOreEvent);
        }

        // cleanup method to prevent a large heap
        if (foundOres.size() > 500) {
            plugin.getAresLogger().warn("cleared found ores cache (exceeds 500 entries)");
            foundOres.clear();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (!TRACKED_ORES.contains(block.getType())) {
            return;
        }

        block.setMetadata("player_placed", new FixedMetadataValue(plugin, player.getUniqueId()));
    }

    @EventHandler
    public void onFoundOre(FoundOreEvent event) {
        AccountService acs = (AccountService) plugin.getService(AccountService.class);
        Player player = event.getPlayer();
        Material material = event.getMaterial();
        int amount = event.getAmount();
        TextColor color = NamedTextColor.WHITE;
        String materialName = Strings.capitalize(material.getKey().getKey().toLowerCase().replaceAll("_", " "));

        if (material.equals(Material.DIAMOND_ORE)) {
            color = DIAMOND_COLOR;
        } else if (material.equals(Material.ANCIENT_DEBRIS)) {
            color = NETHERITE_COLOR;
        }

        Component component = FD_PREFIX.append(Component.text(player.getName() + " found " + amount + " " + materialName, color));

        if (acs == null) {
            Bukkit.broadcast(component);
            return;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            AresAccount account = acs.getCachedAccount(onlinePlayer.getUniqueId());

            if (account == null) {
                continue;
            }

            if (!account.getSettings().isEnabled(AresAccount.Settings.SettingValue.FO_NOTIFICATIONS_ENABLED)) {
                continue;
            }

            if (player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                Component staffComponent = component.hoverEvent(Component.text("Click to teleport").clickEvent(ClickEvent.runCommand("/tp " + player.getName())));
                onlinePlayer.sendMessage(staffComponent);
                continue;
            }

            onlinePlayer.sendMessage(component);
        }
    }
}

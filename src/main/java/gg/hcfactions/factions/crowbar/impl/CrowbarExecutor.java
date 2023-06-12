package gg.hcfactions.factions.crowbar.impl;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.crowbar.CrowbarManager;
import gg.hcfactions.factions.crowbar.ICrowbarExecutor;
import gg.hcfactions.factions.items.Crowbar;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.crowbar.ECrowbarUseType;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.utils.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public final class CrowbarExecutor implements ICrowbarExecutor {
    @Getter public CrowbarManager manager;

    @Override
    public void useCrowbar(Player player, ItemStack item, Block block, Promise promise) {
        ECrowbarUseType useType = null;

        if (block.getType().equals(Material.SPAWNER)) {
            useType = ECrowbarUseType.SPAWNER;
        }

        if (block.getType().equals(Material.END_PORTAL_FRAME)) {
            useType = ECrowbarUseType.END_PORTAL;
        }

        if (useType == null) {
            promise.reject("You can not crowbar this block");
            return;
        }

        final int remainingUses = manager.getRemainingUses(item, useType);

        if (remainingUses == 0) {
            promise.reject("Crowbar is too weak");
            return;
        }

        final ItemMeta meta = item.getItemMeta();
        final List<String> lore = Objects.requireNonNull(meta).getLore();

        if (lore == null) {
            promise.reject("Encountered an error while parsing crowbar item lore");
            return;
        }

        final Claim insideClaim = manager.getPlugin().getClaimManager().getClaimAt(new BLocatable(block));

        if (insideClaim != null) {
            final IFaction insideFaction = manager.getPlugin().getFactionManager().getFactionById(insideClaim.getOwner());

            if (insideFaction != null) {
                if (insideFaction instanceof final PlayerFaction pf) {
                    if (!pf.isMember(player) && !pf.isRaidable() && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                        promise.reject(FError.F_CLAIM_NO_ACCESS.getErrorDescription());
                        return;
                    }
                }

                else if (insideFaction instanceof final ServerFaction sf && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                    promise.reject(FError.F_CLAIM_NO_ACCESS.getErrorDescription());
                    return;
                }
            }
        }

        ItemStack toDrop = new ItemStack(block.getType(), 1);

        // give spawner name on drop
        if (useType.equals(ECrowbarUseType.SPAWNER)) {
            final CreatureSpawner spawner = ((CreatureSpawner) block.getState());
            final ItemMeta dropMeta = toDrop.getItemMeta();

            if (dropMeta == null) {
                promise.reject("Failed to build dropped item meta");
                return;
            }

            dropMeta.setDisplayName(Colors.DARK_BLUE.toBukkit() + StringUtils.capitalize(spawner.getSpawnedType().name().toLowerCase().replaceAll("_", " ")) + " Spawner");
            toDrop.setItemMeta(dropMeta);
        }

        lore.set(useType.getLorePosition(), ((useType.equals(ECrowbarUseType.SPAWNER) ? Crowbar.MONSTER_SPAWNER_PREFIX : Crowbar.END_PORTAL_PREFIX)) + (remainingUses - 1));
        meta.setLore(lore);
        item.setItemMeta(meta);

        block.getWorld().dropItem(block.getLocation(), toDrop);
        block.breakNaturally();

        promise.resolve();
    }
}

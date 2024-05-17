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
import gg.hcfactions.libs.base.util.Strings;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
public final class CrowbarExecutor implements ICrowbarExecutor {
    @Getter public CrowbarManager manager;

    @Override
    public void useCrowbar(Player player, ItemStack item, Block block, Promise promise) {
        CustomItemService cis = (CustomItemService) manager.getPlugin().getService(CustomItemService.class);

        if (cis == null) {
            promise.reject("Failed to query Custom Item Service");
            return;
        }

        Optional<ICustomItem> itemQuery = cis.getItem(item);
        if (itemQuery.isEmpty()) {
            promise.reject("Failed to query Crowbar Item");
            return;
        }

        ICustomItem customItem = itemQuery.get();
        if (!(customItem instanceof final Crowbar crowbarItem)) {
            promise.reject("You are not holding a Crowbar");
            return;
        }

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

        int cost = (useType.equals(ECrowbarUseType.SPAWNER) ? Crowbar.MONSTER_SPAWNER_COST : Crowbar.END_PORTAL_COST);
        if (!crowbarItem.canAfford(item, cost)) {
            promise.reject("Crowbar is too weak");
            return;
        }

        if (useType.equals(ECrowbarUseType.SPAWNER) && !block.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            promise.reject("Crowbars can only be used in the Overworld");
            return;
        }

        Claim insideClaim = manager.getPlugin().getClaimManager().getClaimAt(new BLocatable(block));
        if (insideClaim != null) {
            IFaction insideFaction = manager.getPlugin().getFactionManager().getFactionById(insideClaim.getOwner());

            if (insideFaction != null) {
                if (insideFaction instanceof final PlayerFaction pf) {
                    if (!pf.isMember(player) && !pf.isRaidable() && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                        promise.reject(FError.F_CLAIM_NO_ACCESS.getErrorDescription());
                        return;
                    }
                }

                else if (insideFaction instanceof ServerFaction && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
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

            dropMeta.displayName(Component
                    .text(Strings.capitalize(Objects.requireNonNull(spawner.getSpawnedType()).name().toLowerCase()) + " Spawner")
                    .color(NamedTextColor.DARK_RED)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

            toDrop.setItemMeta(dropMeta);
        }

        // drop block
        block.getWorld().dropItem(block.getLocation(), toDrop);
        block.breakNaturally();

        // subtract dura & update item
        crowbarItem.subtractDurability(item, cost);

        promise.resolve();
    }
}

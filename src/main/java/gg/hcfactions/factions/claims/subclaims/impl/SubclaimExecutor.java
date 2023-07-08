package gg.hcfactions.factions.claims.subclaims.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.claims.subclaims.ISubclaimExecutor;
import gg.hcfactions.factions.claims.subclaims.SubclaimManager;
import gg.hcfactions.factions.items.FactionSubclaimAxe;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.subclaim.ChestSubclaim;
import gg.hcfactions.factions.models.subclaim.Subclaim;
import gg.hcfactions.factions.models.subclaim.SubclaimBuilder;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class SubclaimExecutor implements ISubclaimExecutor {
    @Getter public SubclaimManager manager;

    @Override
    public void addToSubclaim(Player player, String subclaimName, String username, Promise promise) {
        final AccountService acs = (AccountService)manager.getPlugin().getService(AccountService.class);
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(player.getUniqueId());
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (acs == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = faction.getMember(player.getUniqueId());

        if (member == null && !bypass) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        final Subclaim subclaim = manager.getSubclaimByName(faction, subclaimName);

        if (subclaim == null) {
            promise.reject(FError.F_SUBCLAIM_NOT_FOUND.getErrorDescription());
            return;
        }

        if (!subclaim.canAccess(player)) {
            promise.reject(FError.F_SUBCLAIM_NO_ACCESS.getErrorDescription());
            return;
        }

        acs.getAccount(username, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount aresAccount) {
                if (aresAccount == null) {
                    promise.reject(FError.P_NOT_FOUND.getErrorDescription());
                    return;
                }

                if (!faction.isMember(aresAccount.getUniqueId())) {
                    promise.reject(aresAccount.getUsername() + " is not a member of the faction");
                    return;
                }

                if (subclaim.isMember(aresAccount.getUniqueId()) || !faction.getMember(aresAccount.getUniqueId()).getRank().equals(PlayerFaction.Rank.MEMBER)) {
                    promise.reject(aresAccount.getUsername() + " already has access to this subclaim");
                    return;
                }

                subclaim.addMember(aresAccount.getUniqueId());
                FMessage.printSubclaimAdded(subclaim, player.getName(), aresAccount.getUsername());

                manager.getPlugin().getAresLogger().info(player.getName() + " added " + aresAccount.getUsername() + " to the subclaim '" + subclaim.getName() + "'");
                promise.resolve();
            }

            @Override
            public void reject(String s) {
                promise.reject(s);
            }
        });
    }

    @Override
    public void removeFromSubclaim(Player player, String subclaimName, String username, Promise promise) {
        final AccountService acs = (AccountService)manager.getPlugin().getService(AccountService.class);
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(player.getUniqueId());
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (acs == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = faction.getMember(player.getUniqueId());

        if (member == null && !bypass) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        final Subclaim subclaim = manager.getSubclaimByName(faction, subclaimName);

        if (subclaim == null) {
            promise.reject(FError.F_SUBCLAIM_NOT_FOUND.getErrorDescription());
            return;
        }

        if (!subclaim.canAccess(player)) {
            promise.reject(FError.F_SUBCLAIM_NO_ACCESS.getErrorDescription());
            return;
        }

        acs.getAccount(username, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount aresAccount) {
                if (aresAccount == null) {
                    promise.reject(FError.P_NOT_FOUND.getErrorDescription());
                    return;
                }

                if (!faction.isMember(aresAccount.getUniqueId())) {
                    promise.reject(FError.P_NOT_IN_OWN_F.getErrorDescription());
                    return;
                }

                if (!faction.getMember(aresAccount.getUniqueId()).getRank().equals(PlayerFaction.Rank.MEMBER)) {
                    promise.reject(FError.F_SUBCLAIM_ELEVATED_PRIVS.getErrorDescription());
                    return;
                }

                if (!subclaim.isMember(aresAccount.getUniqueId())) {
                    promise.reject(aresAccount.getUsername() + " does not have access to this subclaim");
                    return;
                }

                subclaim.removeMember(aresAccount.getUniqueId());
                FMessage.printSubclaimRemoved(subclaim, player.getName(), aresAccount.getUsername());

                manager.getPlugin().getAresLogger().info(player.getName() + " removed " + aresAccount.getUsername() + " from the subclaim '" + subclaim.getName() + "'");
                promise.resolve();
            }

            @Override
            public void reject(String s) {
                promise.reject(s);
            }
        });
    }

    @Override
    public void startSubclaiming(Player player, String subclaimName, Promise promise) {
        final CustomItemService cis = (CustomItemService)manager.getPlugin().getService(CustomItemService.class);
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(player);
        final Subclaim existing = manager.getSubclaimByName(faction, subclaimName);
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (cis == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = faction.getMember(player.getUniqueId());

        if (member == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        if (member.getRank().equals(PlayerFaction.Rank.MEMBER) && !bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        if (existing != null) {
            promise.reject("Your faction already has a subclaim named " + existing.getName());
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            promise.reject(FError.P_INV_FULL.getErrorDescription());
            return;
        }

        final SubclaimBuilder subclaimBuilder = new SubclaimBuilder(manager, faction, player, subclaimName);
        final Optional<ICustomItem> claimingAxe = cis.getItem(FactionSubclaimAxe.class);

        if (claimingAxe.isEmpty()) {
            promise.reject("Failed to obtain a subclaim creator axe");
            return;
        }

        manager.getBuilderManager().getBuilderRepository().add(subclaimBuilder);
        player.getInventory().addItem(claimingAxe.get().getItem());
        promise.resolve();
    }

    @Override
    public void startSubclaiming(Player player, String factionName, String subclaimName, Promise promise) {
        final CustomItemService cis = (CustomItemService)manager.getPlugin().getService(CustomItemService.class);
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByName(factionName);
        final Subclaim existing = manager.getSubclaimByName(faction, subclaimName);

        if (cis == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        if (manager.getBuilderManager().getSubclaimBuilder(player) != null) {
            promise.reject("You are already claiming for " + manager.getBuilderManager().getSubclaimBuilder(player).getOwner().getName());
            return;
        }

        if (existing != null) {
            promise.reject("Your faction already has a subclaim named " + existing.getName());
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            promise.reject(FError.P_INV_FULL.getErrorDescription());
            return;
        }

        final SubclaimBuilder subclaimBuilder = new SubclaimBuilder(manager, faction, player, subclaimName);
        final Optional<ICustomItem> claimingAxe = cis.getItem(FactionSubclaimAxe.class);

        if (claimingAxe.isEmpty()) {
            promise.reject("Failed to obtain a subclaim creator axe");
            return;
        }

        manager.getBuilderManager().getBuilderRepository().add(subclaimBuilder);
        player.getInventory().addItem(claimingAxe.get().getItem());
        promise.resolve();
    }

    @Override
    public void createChestSubclaim(Player player, Block block, Promise promise) {
        final PlayerFaction playerFaction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(player);
        final WallSign sign = (WallSign)block.getState().getBlockData();
        final Block chest = block.getRelative(sign.getFacing().getOppositeFace());

        if (playerFaction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        if (!(chest.getType().equals(Material.CHEST) || chest.getType().equals(Material.TRAPPED_CHEST))) {
            promise.reject("Sign was not placed against a chest");
            return;
        }

        if (findChestSubclaimAt(block) != null) {
            promise.reject("This chest is already subclaimed");
            return;
        }

        final BLocatable locatable = new BLocatable(chest);
        final Claim insideClaim = manager.getPlugin().getClaimManager().getClaimAt(locatable);
        final Subclaim insideSubclaim = manager.getSubclaimAt(locatable);

        if (insideClaim == null) {
            promise.reject("This chest is not placed inside your faction's claim");
            return;
        }

        if (!insideClaim.getOwner().equals(playerFaction.getUniqueId())) {
            promise.reject("This chest is not placed inside your faction's claim");
            return;
        }

        if (insideSubclaim != null && !insideSubclaim.canAccess(player)) {
            promise.reject("This chest is placed inside a subclaim you do not have access to");
            return;
        }

        final Sign signBlock = (Sign) block.getState();

        signBlock.setLine(0, ChatColor.AQUA + "[Subclaim]");
        promise.resolve();
    }

    @Override
    public void unsubclaim(Player player, String subclaimName, Promise promise) {
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(player);
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = faction.getMember(player.getUniqueId());

        if (member == null && !bypass) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        final Subclaim subclaim = manager.getSubclaimByName(faction, subclaimName);

        if (subclaim == null) {
            promise.reject(FError.F_SUBCLAIM_NOT_FOUND.getErrorDescription());
            return;
        }

        if (!subclaim.canAccess(player) && !bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        manager.getSubclaimRepository().remove(subclaim);
        FMessage.printSubclaimDeleted(subclaim, player.getName());

        new Scheduler(manager.getPlugin()).async(() -> {
            manager.deleteSubclaim(subclaim);

            new Scheduler(manager.getPlugin()).sync(() -> {
                manager.getPlugin().getAresLogger().info(player.getName() + " deleted the subclaim '" + subclaim.getName() + "' for " + faction.getName());
                promise.resolve();
            }).run();
        }).run();
    }

    @Override
    public void listSubclaims(Player player, Promise promise) {
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getPlayerFactionByPlayer(player);
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member account = faction.getMember(player.getUniqueId());

        if (account == null && !bypass) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        final List<Subclaim> accessible = manager.getSubclaimRepository().stream().filter(subclaim ->
                subclaim.getOwner().equals(faction.getUniqueId()) &&
                        subclaim.canAccess(player)).collect(Collectors.toList());

        if (accessible.isEmpty()) {
            promise.reject("No accessible subclaims found");
            return;
        }

        FMessage.listSubclaims(player, accessible);
        promise.resolve();
    }

    @Override
    public void listSubclaims(Player player, PlayerFaction faction, Promise promise) {
        if (!player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        final List<Subclaim> accessible = manager.getSubclaimRepository().stream().filter(subclaim ->
                subclaim.getOwner().equals(faction.getUniqueId()) &&
                        subclaim.canAccess(player)).collect(Collectors.toList());

        if (accessible.isEmpty()) {
            promise.reject("No accessible subclaims found");
            return;
        }

        FMessage.listSubclaims(player, accessible);
        promise.resolve();
    }

    /**
     * Returns a ChestSubclaim instance at the provided Block
     * @param block Bukkit Block
     * @return ChestSubclaim
     */
    public ChestSubclaim findChestSubclaimAt(Block block) {
        final BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
        final List<Block> chests = Lists.newArrayList();

        Block chest = null;

        if (block.getType().name().endsWith("_WALL_SIGN")) {
            final WallSign sign = (WallSign)block.getState().getBlockData();
            final Block relative = block.getRelative(sign.getFacing().getOppositeFace());

            if (relative.getType().equals(Material.CHEST) || relative.getType().equals(Material.TRAPPED_CHEST)) {
                chest = block.getRelative(sign.getFacing().getOppositeFace());
                chests.add(chest);
            }
        } else if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
            chest = block;
            chests.add(chest);
        }

        if (chest == null) {
            return null;
        }

        if (chest.getState() instanceof final Chest chestBlock) {
            if (chestBlock.getInventory() instanceof final DoubleChestInventory doubleChestInventory) {
                final Location leftSide = doubleChestInventory.getLeftSide().getLocation();
                final Location rightSide = doubleChestInventory.getRightSide().getLocation();

                if (leftSide != null && rightSide != null) {
                    final Block otherChest = (leftSide.getBlock().equals(block) ? rightSide.getBlock() : leftSide.getBlock());
                    chests.add(otherChest);
                }
            }
        }

        for (Block chestBlock : chests) {
            for (BlockFace face : faces) {
                final Block relative = chestBlock.getRelative(face);

                if (relative.getType().name().endsWith("_WALL_SIGN")) {
                    final Sign sign = (Sign)relative.getState();

                    if (sign.getLine(0).equals(ChatColor.AQUA + "[Subclaim]")) {
                        return new ChestSubclaim(chests, sign);
                    }
                }
            }
        }

        return null;
    }
}

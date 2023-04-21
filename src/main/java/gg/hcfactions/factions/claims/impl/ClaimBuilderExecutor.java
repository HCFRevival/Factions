package gg.hcfactions.factions.claims.impl;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.claims.ClaimBuilderManager;
import gg.hcfactions.factions.claims.IClaimBuilderExecutor;
import gg.hcfactions.factions.items.FactionClaimingStick;
import gg.hcfactions.factions.models.claim.impl.ClaimBuilder;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Optional;

public record ClaimBuilderExecutor(@Getter ClaimBuilderManager builderManager) implements IClaimBuilderExecutor {
    @Override
    public void startClaiming(Player player, Promise promise) {
        final CustomItemService cis = (CustomItemService) builderManager.getClaimManager().getPlugin().getService(CustomItemService.class);

        if (cis == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        final PlayerFaction faction = builderManager.getClaimManager().getPlugin().getFactionManager().getPlayerFactionByPlayer(player);

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = faction.getMember(player.getUniqueId());

        if (member == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        final boolean hasBypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);
        if (member.getRank().equals(PlayerFaction.Rank.MEMBER) && !hasBypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        if (builderManager.getClaimBuilder(player) != null) {
            promise.reject(FError.P_ALREADY_CLAIMING.getErrorDescription());
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            promise.reject(FError.P_INV_FULL.getErrorDescription());
            return;
        }

        final ClaimBuilder builder = new ClaimBuilder(builderManager.getClaimManager(), faction, player);
        final Optional<ICustomItem> item = cis.getItem(FactionClaimingStick.class);

        if (item.isEmpty()) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        player.getInventory().addItem(item.get().getItem());
        builderManager.getBuilderRepository().add(builder);
        promise.resolve();
    }

    @Override
    public void startClaiming(Player player, IFaction faction, Promise promise) {
        final CustomItemService cis = (CustomItemService) builderManager.getClaimManager().getPlugin().getService(CustomItemService.class);

        if (cis == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        if (builderManager.getClaimBuilder(player) != null) {
            promise.reject(FError.P_ALREADY_CLAIMING.getErrorDescription());
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            promise.reject(FError.P_INV_FULL.getErrorDescription());
            return;
        }

        final ClaimBuilder builder = new ClaimBuilder(builderManager.getClaimManager(), faction, player);
        final Optional<ICustomItem> item = cis.getItem(FactionClaimingStick.class);

        if (item.isEmpty()) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        builderManager.getBuilderRepository().add(builder);
        promise.resolve();
    }
}

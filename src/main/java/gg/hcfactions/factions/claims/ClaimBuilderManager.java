package gg.hcfactions.factions.claims;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.claims.impl.ClaimBuilderExecutor;
import gg.hcfactions.factions.items.FactionClaimingStick;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.claim.impl.ClaimBuilder;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;

@Getter
public final class ClaimBuilderManager implements IManager {
    public final ClaimManager claimManager;
    public final ClaimBuilderExecutor executor;
    public final Set<ClaimBuilder> builderRepository;

    public ClaimBuilderManager(ClaimManager claimManager) {
        this.claimManager = claimManager;
        this.executor = new ClaimBuilderExecutor(this);
        this.builderRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public void onEnable() {
        final CustomItemService cis = (CustomItemService) claimManager.getPlugin().getService(CustomItemService.class);

        if (cis == null) {
            claimManager.getPlugin().getAresLogger().error("failed to get custom item service");
            return;
        }

        cis.registerNewItem(new FactionClaimingStick(this));
    }

    @Override
    public void onDisable() {
        builderRepository.clear();
    }

    public ClaimBuilder getClaimBuilder(Player player) {
        return builderRepository
                .stream()
                .filter(b -> b.getPlayer().getUniqueId().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }
}

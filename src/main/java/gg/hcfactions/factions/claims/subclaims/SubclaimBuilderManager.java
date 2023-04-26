package gg.hcfactions.factions.claims.subclaims;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.items.FactionSubclaimAxe;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.subclaim.SubclaimBuilder;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;

public final class SubclaimBuilderManager implements IManager {
    @Getter public final SubclaimManager subclaimManager;
    @Getter public Set<SubclaimBuilder> builderRepository;

    public SubclaimBuilderManager(SubclaimManager subclaimManager) {
        this.subclaimManager = subclaimManager;
    }

    @Override
    public void onEnable() {
        this.builderRepository = Sets.newConcurrentHashSet();

        final CustomItemService cis = (CustomItemService) subclaimManager.getPlugin().getService(CustomItemService.class);
        if (cis == null) {
            subclaimManager.getPlugin().getAresLogger().error("failed to get custom item service");
            return;
        }

        cis.registerNewItem(new FactionSubclaimAxe());
    }

    @Override
    public void onDisable() {
        this.builderRepository = null;
    }

    /**
     * Returns a player's Subclaim Builder
     * @param player Player
     * @return Subclaim Builder
     */
    public SubclaimBuilder getSubclaimBuilder(Player player) {
        return builderRepository.stream().filter(builder -> builder.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }
}

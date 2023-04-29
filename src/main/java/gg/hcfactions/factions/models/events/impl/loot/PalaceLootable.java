package gg.hcfactions.factions.models.events.impl.loot;

import gg.hcfactions.factions.models.events.EPalaceLootTier;
import gg.hcfactions.libs.bukkit.loot.ILootable;
import gg.hcfactions.libs.bukkit.loot.impl.GenericLootable;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class PalaceLootable extends GenericLootable implements ILootable {
    @Getter public final EPalaceLootTier lootTier;

    public PalaceLootable(GenericLootable generic, EPalaceLootTier lootTier) {
        super(
                generic.getId(),
                generic.getDisplayName(),
                generic.getMaterial(),
                generic.getLore(),
                generic.getEnchantments(),
                generic.getMinDropAmount(),
                generic.getMaxDropAmount(),
                generic.getProbability()
        );

        this.lootTier = lootTier;
    }

    public PalaceLootable(
            String id,
            String displayName,
            Material material,
            List<String> lore,
            Map<Enchantment, Integer> enchantments,
            int minDropAmount,
            int maxDropAmount,
            int probability,
            EPalaceLootTier lootTier
    ) {
        super(id, displayName, material, lore, enchantments, minDropAmount, maxDropAmount, probability);
        this.lootTier = lootTier;
    }
}

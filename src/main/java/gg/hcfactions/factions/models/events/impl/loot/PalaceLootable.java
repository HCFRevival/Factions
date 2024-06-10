package gg.hcfactions.factions.models.events.impl.loot;

import gg.hcfactions.factions.models.events.EPalaceLootTier;
import gg.hcfactions.libs.bukkit.loot.ILootable;
import gg.hcfactions.libs.bukkit.loot.impl.GenericLootable;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
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
                generic.getProbability(),
                generic.getCustomItemClass()
        );

        this.lootTier = lootTier;
    }

    public PalaceLootable(
            String id,
            Component displayName,
            Material material,
            List<Component> lore,
            Map<Enchantment, Integer> enchantments,
            int minDropAmount,
            int maxDropAmount,
            int probability,
            EPalaceLootTier lootTier,
            ICustomItem customItemClass
    ) {
        super(id, displayName, material, lore, enchantments, minDropAmount, maxDropAmount, probability, customItemClass);
        this.lootTier = lootTier;
    }
}

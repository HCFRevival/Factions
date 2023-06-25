package gg.hcfactions.factions.models.events.impl.loot;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.models.events.EPalaceLootTier;
import gg.hcfactions.factions.models.events.IPalaceLootChest;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.loot.LootManager;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record PalaceLootChest(@Getter EventManager manager,
                              @Getter BLocatable location,
                              @Getter EPalaceLootTier lootTier) implements IPalaceLootChest {

    @Override
    public void restock() {
        final int amount = LootManager.RANDOM.nextInt(3);

        getChest().getBlockInventory().clear();

        new Scheduler(manager.getPlugin()).async(() -> {
            final List<ItemStack> items = manager.getPalaceLootManager().getItems(lootTier, amount);
            final List<Integer> positions = Lists.newArrayList();

            while (positions.size() < amount) {
                final int pos = LootManager.RANDOM.nextInt(26);

                if (positions.contains(pos)) {
                    continue;
                }

                positions.add(pos);
            }

            new Scheduler(manager.getPlugin()).sync(() -> {
                for (int i = 0; i < items.size(); i++) {
                    final ItemStack item = items.get(i);
                    final int pos = positions.get(i);

                    getChest().getBlockInventory().setItem(pos, item);
                }
            }).run();
        }).run();
    }
}

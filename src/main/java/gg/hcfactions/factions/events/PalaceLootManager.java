package gg.hcfactions.factions.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.events.EPalaceLootTier;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootable;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.loot.ILootable;
import gg.hcfactions.libs.bukkit.loot.LootManager;
import gg.hcfactions.libs.bukkit.loot.impl.GenericLootable;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class PalaceLootManager extends LootManager implements IManager {
    @Getter public final Factions plugin;
    @Getter private final List<GenericLootable> lootRepository;

    public PalaceLootManager(Factions plugin) {
        super(plugin, "Palace");
        this.plugin = plugin;
        this.lootRepository = Lists.newArrayList();
    }

    @Override
    public void onEnable() {
        loadTiers();
    }

    @Override
    public void onDisable() {
        lootRepository.clear();
    }

    public void loadTiers() {
        if (!lootRepository.isEmpty()) {
            lootRepository.clear();
        }

        for (EPalaceLootTier tier : EPalaceLootTier.values()) {
            loadTier(tier);
        }

        plugin.getAresLogger().info("loaded " + lootRepository.size() + " palace lootables");
    }

    public void loadTier(EPalaceLootTier tier) {
        final YamlConfiguration conf = plugin.loadConfiguration("event-loot");
        final List<GenericLootable> res = load(conf, "palace." + tier.name);
        res.forEach(gl -> lootRepository.add(new PalaceLootable(gl, tier)));
        plugin.getAresLogger().info("loaded " + res.size() + " palace lootables for " + tier.name);
    }

    public ImmutableList<ItemStack> getItems(EPalaceLootTier tier, int amount) {
        final Random random = new Random();
        final List<PalaceLootable> eligibleLootables = lootRepository.stream()
                .filter(lootable -> lootable instanceof PalaceLootable)
                .map(lootable -> (PalaceLootable) lootable)
                .filter(palaceLootable -> palaceLootable.getLootTier().equals(tier))
                .toList();

        if (eligibleLootables.isEmpty()) {
            return ImmutableList.of();
        }

        final ImmutableList.Builder<ItemStack> builder = new ImmutableList.Builder<>();
        int totalAdded = 0;

        while (totalAdded < amount) {
            for (PalaceLootable palaceLootable : eligibleLootables) {
                if (totalAdded >= amount) {
                    break;
                }

                final int roll = random.nextInt(100) + 1;

                if (roll <= palaceLootable.getProbability()) {
                    builder.add(palaceLootable.getItem(false));
                    totalAdded += 1;
                }
            }
        }

        return builder.build();
    }

    public void saveItem(PalaceLootable lootable) {
        saveItem("event-loot",
                "palace." + lootable.getLootTier().name + ".",
                lootable.getItem(false),
                lootable.getMinDropAmount(),
                lootable.getMaxDropAmount(),
                lootable.getProbability()
        );
    }

    @Override
    public void removeItem(String fileName, ILootable lootable) {
        if (!(lootable instanceof final PalaceLootable palaceLootable)) {
            return;
        }

        final YamlConfiguration conf = plugin.loadConfiguration("event-loot");
        final String key = "palace." + palaceLootable.getLootTier().name + "." + palaceLootable.getId();

        conf.set(key, null);
        plugin.saveConfiguration(fileName, conf);
    }

    public Optional<PalaceLootChest> getLootChestAt(Block block) {
        final BLocatable loc = new BLocatable(block);
        final Claim insideClaim = plugin.getClaimManager().getClaimAt(loc);

        if (insideClaim == null) {
            return Optional.empty();
        }

        final Optional<PalaceEvent> palaceQuery = plugin.getEventManager().getPalaceEvents().stream().filter(event -> event.getOwner().equals(insideClaim.getOwner())).findFirst();

        if (palaceQuery.isEmpty()) {
            return Optional.empty();
        }

        final PalaceEvent palaceEvent = palaceQuery.get();

        for (PalaceLootChest lootChest : palaceEvent.getLootChests()) {
            if (lootChest.getLocation().getX() == loc.getX() && lootChest.getLocation().getY() == loc.getY() && lootChest.getLocation().getZ() == loc.getZ()) {
                return Optional.of(lootChest);
            }
        }

        return Optional.empty();
    }
}

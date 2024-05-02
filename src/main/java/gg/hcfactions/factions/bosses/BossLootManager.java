package gg.hcfactions.factions.bosses;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.libs.bukkit.loot.ILootable;
import gg.hcfactions.libs.bukkit.loot.LootManager;
import gg.hcfactions.libs.bukkit.loot.impl.GenericLootable;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class BossLootManager extends LootManager implements IManager {
    public static final String TABLE_NAME = "Boss Loot Table";
    public static final String FILE_NAME = "boss-loot";
    public static final String FILE_KEY = "data.";

    @Getter public final BossManager manager;
    @Getter public final List<GenericLootable> lootRepository;

    public BossLootManager(BossManager manager) {
        super(manager.getPlugin(), TABLE_NAME);
        this.manager = manager;
        this.lootRepository = Lists.newArrayList();
    }

    @Override
    public void onEnable() {
        lootRepository.addAll(load(plugin.loadConfiguration(FILE_NAME), FILE_KEY));
        plugin.getAresLogger().info("Loaded {} Boss Lootable Items", lootRepository.size());
    }

    @Override
    public void onDisable() {
        lootRepository.clear();
    }

    @Override
    public void onReload() {
        lootRepository.clear();
        onEnable();
    }

    @Override
    public void removeItem(String fileName, ILootable loot) {
        final YamlConfiguration conf = plugin.loadConfiguration(fileName);
        final String key = "data." + loot.getId();

        conf.set(key, null);
        plugin.saveConfiguration(fileName, conf);
    }

    public ImmutableList<ItemStack> getItems(int amount) {
        return getItems(lootRepository, amount);
    }
}

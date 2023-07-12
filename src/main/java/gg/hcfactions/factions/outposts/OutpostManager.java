package gg.hcfactions.factions.outposts;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.outpost.IOutpostBlock;
import gg.hcfactions.factions.models.outpost.impl.OutpostBlock;
import gg.hcfactions.factions.outposts.impl.OutpostExecutor;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;

public final class OutpostManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public final OutpostExecutor executor;
    @Getter public final List<OutpostBlock> blockRepository;

    @Getter @Setter public long nextRestockTime;

    private BukkitTask resetTask;

    public OutpostManager(Factions plugin) {
        this.plugin = plugin;
        this.executor = new OutpostExecutor(this);
        this.blockRepository = Lists.newArrayList();
    }

    @Override
    public void onEnable() {
        nextRestockTime = Time.now() + (plugin.getConfiguration().getOutpostRestockDuration() * 1000L);

        loadBlocks();

        resetTask = new Scheduler(plugin).async(() -> {
            if (nextRestockTime <= Time.now()) {
                new Scheduler(plugin).sync(() -> {
                    blockRepository.forEach(IOutpostBlock::reset);
                    setNextRestockTime(Time.now() + (plugin.getConfiguration().getOutpostRestockDuration() * 1000L));

                    Bukkit.broadcastMessage(" ");
                    Bukkit.broadcastMessage(FMessage.OUTPOST_PREFIX + "Outposts have been reset");
                    Bukkit.broadcastMessage(" ");
                }).run();
            }
        }).repeat(0L, 30*20L).run();
    }

    @Override
    public void onDisable() {
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }

        blockRepository.forEach(OutpostBlock::reset);
    }

    public void loadBlocks() {
        final YamlConfiguration conf = plugin.loadConfiguration("outposts");

        if (conf.get("blocks") == null) {
            plugin.getAresLogger().warn("could not find any blocks in outposts.yml... skipping.");
            return;
        }

        for (String materialName : conf.getStringList("blocks")) {
            final Material material;

            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException e) {
                plugin.getAresLogger().error("could not load outpost block, bad material type: " + materialName);
                continue;
            }

            final OutpostBlock block = new OutpostBlock(plugin, material);

            blockRepository.add(block);
        }

        plugin.getAresLogger().info("loaded " + blockRepository.size() + " outpost blocks");
    }

    public void saveBlock(OutpostBlock block) {
        final YamlConfiguration conf = plugin.loadConfiguration("outposts");
        final List<String> post = Lists.newArrayList();

        if (conf.get("blocks") != null) {
            final List<String> pre = conf.getStringList("blocks");
            post.addAll(pre);
        }

        post.add(block.getMaterial().name());

        conf.set("blocks", post);
    }

    public void deleteBlock(OutpostBlock block) {
        final YamlConfiguration conf = plugin.loadConfiguration("outposts");

        if (conf.get("blocks") == null) {
            return;
        }

        final List<String> existing = conf.getStringList("blocks");
        existing.remove(block.getMaterial().name());

        conf.set("blocks", existing);
    }

    public Optional<OutpostBlock> getOutpostBlock(Material mat) {
        return blockRepository.stream().filter(b -> b.getMaterial().equals(mat)).findFirst();
    }
}

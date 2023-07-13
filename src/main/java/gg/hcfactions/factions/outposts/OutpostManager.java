package gg.hcfactions.factions.outposts;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.outpost.IOutpostBlock;
import gg.hcfactions.factions.models.outpost.impl.OutpostBlock;
import gg.hcfactions.factions.outposts.impl.OutpostExecutor;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class OutpostManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public final OutpostExecutor executor;
    @Getter public final List<OutpostBlock> blockRepository;

    @Getter @Setter public long nextRestockTime;
    @Getter public final Random random;

    private BukkitTask resetTask;

    private final ImmutableList<Material> HELMET_MAT = ImmutableList.of(
            Material.LEATHER_HELMET,
            Material.CHAINMAIL_HELMET,
            Material.IRON_HELMET,
            Material.GOLDEN_HELMET,
            Material.DIAMOND_HELMET,
            Material.NETHERITE_HELMET);

    private final ImmutableList<Material> CHESTPLATE_MAT = ImmutableList.of(
            Material.LEATHER_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.GOLDEN_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE,
            Material.NETHERITE_CHESTPLATE
    );

    private final ImmutableList<Material> LEGGING_MAT = ImmutableList.of(
            Material.LEATHER_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.GOLDEN_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.NETHERITE_LEGGINGS
    );

    private final ImmutableList<Material> BOOT_MAT = ImmutableList.of(
            Material.LEATHER_BOOTS,
            Material.CHAINMAIL_BOOTS,
            Material.IRON_BOOTS,
            Material.GOLDEN_BOOTS,
            Material.CHAINMAIL_BOOTS,
            Material.DIAMOND_BOOTS,
            Material.NETHERITE_BOOTS
    );

    private final ImmutableList<Material> WEAPON_MAT = ImmutableList.of(
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE,
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.IRON_SWORD,
            Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD
    );

    public OutpostManager(Factions plugin) {
        this.plugin = plugin;
        this.random = new Random();
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
        }).repeat(0L, 60*20L).run();
    }

    @Override
    public void onDisable() {
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }

        blockRepository.forEach(OutpostBlock::reset);
    }

    /**
     * Returns an Outpost Block matching the provided Material
     * @param mat Bukkit Material
     * @return Optional of Outpost Block
     */
    public Optional<OutpostBlock> getOutpostBlock(Material mat) {
        return blockRepository.stream().filter(b -> b.getMaterial().equals(mat)).findFirst();
    }

    public ItemStack getRandomArmor(final List<Material> mats) {
        final Material mat = mats.get(Math.abs(random.nextInt(HELMET_MAT.size())));
        final ItemBuilder builder = new ItemBuilder().setMaterial(mat);
        final int protLevel = Math.abs(random.nextInt(4));

        builder.addEnchant(Enchantment.VANISHING_CURSE, 1);

        if (protLevel > 0) {
            builder.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, protLevel);
        }

        return builder.build();
    }

    public ItemStack getRandomWeapon() {
        final Material mat = WEAPON_MAT.get(Math.abs(random.nextInt(WEAPON_MAT.size())));
        final ItemBuilder builder = new ItemBuilder().setMaterial(mat);
        final int sharpLevel = Math.abs(random.nextInt(3));
        final int knockbackLevel = Math.abs(random.nextInt(2));

        builder.addEnchant(Enchantment.VANISHING_CURSE, 1);

        if (sharpLevel > 0) {
            builder.addEnchant(Enchantment.DAMAGE_ALL, sharpLevel);
        }

        if (knockbackLevel > 0) {
            builder.addEnchant(Enchantment.KNOCKBACK, knockbackLevel);
        }

        return builder.build();
    }

    public ItemStack getEnhancedBow(boolean crossbow) {
        if (crossbow) {
            final ItemBuilder builder = new ItemBuilder().setMaterial(Material.CROSSBOW);
            final int piercingLevel = Math.abs(random.nextInt(3));
            final int quickChargeLevel = Math.abs(random.nextInt(3));
            final boolean multishot = random.nextBoolean();

            builder.addEnchant(Enchantment.VANISHING_CURSE, 1);

            if (piercingLevel > 0) {
                builder.addEnchant(Enchantment.PIERCING, piercingLevel);
            }

            if (quickChargeLevel > 0) {
                builder.addEnchant(Enchantment.QUICK_CHARGE, quickChargeLevel);
            }

            if (multishot) {
                builder.addEnchant(Enchantment.MULTISHOT, 1);
            }

            return builder.build();
        }

        final ItemBuilder builder = new ItemBuilder().setMaterial(Material.BOW);
        final int powerLevel = Math.abs(random.nextInt(5));
        final int flameLevel = Math.abs(random.nextInt(2));
        final int punchLevel = Math.abs(random.nextInt(2));

        builder.addEnchant(Enchantment.VANISHING_CURSE, 1);

        if (powerLevel > 0) {
            builder.addEnchant(Enchantment.ARROW_DAMAGE, powerLevel);
        }

        if (flameLevel > 0) {
            builder.addEnchant(Enchantment.ARROW_FIRE, flameLevel);
        }

        if (punchLevel > 0) {
            builder.addEnchant(Enchantment.ARROW_KNOCKBACK, punchLevel);
        }

        return builder.build();
    }

    public ItemStack getRandomHelmet() {
        return getRandomArmor(HELMET_MAT);
    }

    public ItemStack getRandomChestplate() {
        return getRandomArmor(CHESTPLATE_MAT);
    }

    public ItemStack getRandomLeggings() {
        return getRandomArmor(LEGGING_MAT);
    }

    public ItemStack getRandomBoots() {
        return getRandomArmor(BOOT_MAT);
    }

    /**
     * Loads all Outpost Blocks in to memory
     */
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

    /**
     * Save an Outpost Block to file
     * @param block Outpost Block
     */
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

    /**
     * Delete an existing Outpost Block from file
     * @param block Outpost Block
     */
    public void deleteBlock(OutpostBlock block) {
        final YamlConfiguration conf = plugin.loadConfiguration("outposts");

        if (conf.get("blocks") == null) {
            return;
        }

        final List<String> existing = conf.getStringList("blocks");
        existing.remove(block.getMaterial().name());

        conf.set("blocks", existing);
    }
}

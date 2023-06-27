package gg.hcfactions.factions.utils;

import gg.hcfactions.factions.Factions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public final class FRecipes {
    @Getter public final Factions plugin;
    @Getter @Setter public Config config;

    public FRecipes(Factions plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void register() {
        final NamespacedKey saddleKey = new NamespacedKey(plugin, "craftable_saddle");
        final NamespacedKey hosKey = new NamespacedKey(plugin, "craftable_heart_of_the_sea");
        final NamespacedKey tridentKey = new NamespacedKey(plugin, "craftable_trident");
        final NamespacedKey chainmailHelmetKey = new NamespacedKey(plugin, "craftable_chainmail_helmet");
        final NamespacedKey chainmailChestKey = new NamespacedKey(plugin, "craftable_chainmail_chestplate");
        final NamespacedKey chainmailLegKey = new NamespacedKey(plugin, "craftable_chainmail_leggings");
        final NamespacedKey chainmailBootKey = new NamespacedKey(plugin, "craftable_chainmail_boots");
        final NamespacedKey totemKey = new NamespacedKey(plugin, "craftable_totem");
        final NamespacedKey gappleKey = new NamespacedKey(plugin, "craftable_gapple");
        final NamespacedKey nametagKey = new NamespacedKey(plugin, "craftable_nametag");
        final NamespacedKey smithingUpgradeKey = new NamespacedKey(plugin, "craftable_smithing_upgrade");
        final NamespacedKey simpleGlisteningMelonKey = new NamespacedKey(plugin, "craftable_simple_glistening_melon");

        if (config.isSaddlesEnabled() && Bukkit.getRecipe(saddleKey) == null) {
            final ShapedRecipe recipe = new ShapedRecipe(saddleKey, new ItemStack(Material.SADDLE, 1));

            recipe.shape("ILI", "R R", "   ");
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('L', Material.LEATHER);
            recipe.setIngredient('R', Material.LEAD);

            Bukkit.addRecipe(recipe);
            plugin.getAresLogger().info("registered recipe: saddle");
        }

        if (config.isHeartOfTheSeaEnabled() && Bukkit.getRecipe(hosKey) == null) {
            final ShapedRecipe recipe = new ShapedRecipe(hosKey, new ItemStack(Material.HEART_OF_THE_SEA, 1));

            recipe.shape("PPP", "PSP", "PPP");
            recipe.setIngredient('P', Material.PRISMARINE_CRYSTALS);
            recipe.setIngredient('S', Material.NAUTILUS_SHELL);

            Bukkit.addRecipe(recipe);
            plugin.getAresLogger().info("registered recipe: heart of the sea");
        }

        if (config.isTridentEnabled() && Bukkit.getRecipe(tridentKey) == null) {
            final ShapedRecipe recipe = new ShapedRecipe(tridentKey, new ItemStack(Material.TRIDENT));

            recipe.shape("DHD", "DSD", " S ");
            recipe.setIngredient('D', Material.DIAMOND);
            recipe.setIngredient('H', Material.HEART_OF_THE_SEA);
            recipe.setIngredient('S', Material.STICK);

            Bukkit.addRecipe(recipe);
            plugin.getAresLogger().info("registered recipe: trident");
        }

        if (config.isChainmailArmorEnabled()) {
            if (Bukkit.getRecipe(chainmailHelmetKey) == null) {
                final ShapedRecipe recipe = new ShapedRecipe(chainmailHelmetKey, new ItemStack(Material.CHAINMAIL_HELMET));

                recipe.shape("CCC", "C C", "   ");
                recipe.setIngredient('C', Material.CHAIN);

                Bukkit.addRecipe(recipe);
                plugin.getAresLogger().info("registered recipe: chainmail helmet");
            }

            if (Bukkit.getRecipe(chainmailChestKey) == null) {
                final ShapedRecipe recipe = new ShapedRecipe(chainmailChestKey, new ItemStack(Material.CHAINMAIL_CHESTPLATE));

                recipe.shape("C C", "CCC", "CCC");
                recipe.setIngredient('C', Material.CHAIN);

                Bukkit.addRecipe(recipe);
                plugin.getAresLogger().info("registered recipe: chainmail chestplate");
            }

            if (Bukkit.getRecipe(chainmailLegKey) == null) {
                final ShapedRecipe recipe = new ShapedRecipe(chainmailLegKey, new ItemStack(Material.CHAINMAIL_LEGGINGS));

                recipe.shape("CCC", "C C", "C C");
                recipe.setIngredient('C', Material.CHAIN);

                Bukkit.addRecipe(recipe);
                plugin.getAresLogger().info("registered recipe: chainmail leggings");
            }

            if (Bukkit.getRecipe(chainmailBootKey) == null) {
                final ShapedRecipe recipe = new ShapedRecipe(chainmailBootKey, new ItemStack(Material.CHAINMAIL_BOOTS));

                recipe.shape("   ", "C C", "C C");
                recipe.setIngredient('C', Material.CHAIN);

                Bukkit.addRecipe(recipe);
                plugin.getAresLogger().info("registered recipe: chainmail boots");
            }
        }

        if (config.isTotemEnabled() && Bukkit.getRecipe(totemKey) == null) {
            final ShapedRecipe recipe = new ShapedRecipe(totemKey, new ItemStack(Material.TOTEM_OF_UNDYING));

            recipe.shape(" H ", "EEE", " E ");
            recipe.setIngredient('H', Material.PLAYER_HEAD);
            recipe.setIngredient('E', Material.EMERALD_BLOCK);

            Bukkit.addRecipe(recipe);
            plugin.getAresLogger().info("registered recipe: totem");
        }

        if (config.isGappleEnabled() && Bukkit.getRecipe(gappleKey) == null) {
            final ShapedRecipe recipe = new ShapedRecipe(gappleKey, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));

            recipe.shape("GGG", "GAG", "GGG");
            recipe.setIngredient('G', Material.GOLD_BLOCK);
            recipe.setIngredient('A', Material.APPLE);

            Bukkit.addRecipe(recipe);
            plugin.getAresLogger().info("registered recipe: gapple");
        }

        if (config.isNametagsEnabled() && Bukkit.getRecipe(nametagKey) == null) {
            final ShapedRecipe recipe = new ShapedRecipe(nametagKey, new ItemStack(Material.NAME_TAG));

            recipe.shape(" S ", " P ", " P ");
            recipe.setIngredient('S', Material.STRING);
            recipe.setIngredient('P', Material.PAPER);

            Bukkit.addRecipe(recipe);
            plugin.getAresLogger().info("registered recipe: nametag");
        }

        if (config.isSmithingUpgradeEnabled() && Bukkit.getRecipe(smithingUpgradeKey) == null) {
            final ShapedRecipe recipe = new ShapedRecipe(smithingUpgradeKey, new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE));

            recipe.shape("NNN", "NDN", "NNN");
            recipe.setIngredient('N', Material.NETHER_BRICK);
            recipe.setIngredient('D', Material.DIAMOND);

            Bukkit.addRecipe(recipe);
            plugin.getAresLogger().info("registered recipe: smithing upgrade template");
        }

        if (config.isSimpleGlisteningMelonEnabled() && Bukkit.getRecipe(simpleGlisteningMelonKey) == null) {
            final ShapedRecipe recipe = new ShapedRecipe(simpleGlisteningMelonKey, new ItemStack(Material.GLISTERING_MELON_SLICE));

            recipe.shape(" N ", "NMN", " N ");
            recipe.setIngredient('N', Material.GOLD_NUGGET);
            recipe.setIngredient('M', Material.MELON_SLICE);

            Bukkit.addRecipe(recipe);
            plugin.getAresLogger().info("registered recipe: simple glistening melon");
        }
    }

    @AllArgsConstructor
    public static class Config {
        @Getter @Setter public boolean saddlesEnabled;
        @Getter @Setter public boolean heartOfTheSeaEnabled;
        @Getter @Setter public boolean tridentEnabled;
        @Getter @Setter public boolean chainmailArmorEnabled;
        @Getter @Setter public boolean totemEnabled;
        @Getter @Setter public boolean gappleEnabled;
        @Getter @Setter public boolean nametagsEnabled;
        @Getter @Setter public boolean smithingUpgradeEnabled;
        @Getter @Setter public boolean simpleGlisteningMelonEnabled;
    }
}

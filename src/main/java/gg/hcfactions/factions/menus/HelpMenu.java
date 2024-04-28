package gg.hcfactions.factions.menus;

import com.google.common.collect.Lists;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.cx.modules.player.combat.PotionLimitModule;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.impl.Archer;
import gg.hcfactions.factions.models.classes.impl.Bard;
import gg.hcfactions.factions.models.classes.impl.Diver;
import gg.hcfactions.factions.models.classes.impl.Rogue;
import gg.hcfactions.libs.base.util.Strings;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.menu.impl.GenericMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Optional;

public final class HelpMenu extends GenericMenu {
    private final Factions plugin;
    @Getter public EHelpMenuPage currentPage;

    public HelpMenu(Factions plugin, Player player) {
        super(plugin, player, "Faction Help", 6);
        this.plugin = plugin;
        this.currentPage = EHelpMenuPage.HOME;
    }

    public void loadWindow(EHelpMenuPage page) {
        clear();

        final Clickable backButton = new Clickable(new ItemBuilder().setMaterial(Material.BARRIER).setName(ChatColor.RED + "Back").build(), 53, click ->
                loadWindow(EHelpMenuPage.HOME));

        if (page.equals(EHelpMenuPage.HOME)) {
            for (EHelpMenuPage menuPage : EHelpMenuPage.values()) {
                if (menuPage.equals(EHelpMenuPage.HOME)) {
                    continue;
                }

                final ItemStack icon = new ItemBuilder()
                        .setMaterial(menuPage.getIconMaterial())
                        .setName(menuPage.getDisplayName())
                        .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                        .addLore(menuPage.getDescription()).build();

                addItem(new Clickable(icon, menuPage.getPosition(), click -> loadWindow(menuPage)));
            }

            fill(new ItemBuilder().setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(ChatColor.RESET + " ").build());

            return;
        }

        if (page.equals(EHelpMenuPage.POTION_LIMIT)) {
            final CXService cxService = (CXService) plugin.getService(CXService.class);

            if (cxService == null) {
                getPlayer().closeInventory();
                getPlayer().sendMessage(ChatColor.RED + "Failed to obtain Command X Service");
                return;
            }

            addItem(backButton);

            int cursor = 0;

            for (PotionLimitModule.PotionLimit potionLimit : cxService.getPotionLimitModule().getPotionLimits()) {
                final ItemStack item = new ItemStack(Material.POTION);
                final ItemMeta meta = item.getItemMeta();
                final PotionMeta potionMeta = (PotionMeta) meta;

                if (potionMeta != null) {
                    //  meta.setBasePotionData(new PotionData(type, extend, upgraded));
                    final PotionEffectType effectType = potionLimit.getType();
                    final Color color = effectType.getColor();
                    final List<String> lore = Lists.newArrayList();

                    lore.add(ChatColor.YELLOW + "Amplifiable: " + (potionLimit.isAmplifiable() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
                    lore.add(ChatColor.YELLOW + "Extendable: " + (potionLimit.isExtendable() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
                    lore.add(ChatColor.YELLOW + "Splashable: " + (potionLimit.canSplash() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));

                    potionMeta.setDisplayName(net.md_5.bungee.api.ChatColor.of(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()))
                            + Strings.capitalize(effectType.getKey().getKey().toLowerCase().replaceAll("_", " "))
                            + (potionLimit.isDisabled() ? ChatColor.DARK_RED + " (DISABLED)" : ""));

                    potionMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    potionMeta.setColor(color);
                    potionMeta.setLore(lore);

                    item.setItemMeta(potionMeta);

                    addItem(new Clickable(item, cursor, click -> {}));
                    cursor += 1;
                }
            }

            return;
        }

        if (page.equals(EHelpMenuPage.ENCHANT_LIMIT)) {
            final CXService cxService = (CXService) plugin.getService(CXService.class);

            if (cxService == null) {
                getPlayer().closeInventory();
                getPlayer().sendMessage(ChatColor.RED + "Failed to obtain Command X Service");
                return;
            }

            addItem(backButton);

            int cursor = 0;

            for (Enchantment enchantment : cxService.getEnchantLimitModule().getEnchantLimits().keySet()) {
                final int maxLevel = cxService.getEnchantLimitModule().getMaxEnchantmentLevel(enchantment);
                final boolean disabled = (maxLevel <= 0);
                final ItemBuilder builder = new ItemBuilder().setMaterial(Material.ENCHANTED_BOOK);
                final List<String> lore = Lists.newArrayList();

                builder.setName(ChatColor.AQUA + Strings.capitalize(enchantment.getKey().getKey().toLowerCase().replaceAll("_", " ")) + (disabled ? ChatColor.DARK_RED + " (DISABLED)" : ""));

                if (disabled) {
                    lore.add(ChatColor.RED + "This enchantment can not be applied");
                } else {
                    lore.add(ChatColor.YELLOW + "Max Level" + ChatColor.RESET + ": " + maxLevel);
                }

                builder.addLore(lore);
                builder.addFlag(ItemFlag.HIDE_ENCHANTS);

                addItem(new Clickable(builder.build(), cursor, click -> {}));
                cursor += 1;
            }

            return;
        }

        if (page.equals(EHelpMenuPage.WORLD)) {
            final Optional<World> overworld = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment().equals(World.Environment.NORMAL)).findFirst();
            final Optional<World> nether = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment().equals(World.Environment.NETHER)).findFirst();
            final Optional<World> end = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment().equals(World.Environment.THE_END)).findFirst();

            addItem(backButton);

            overworld.ifPresent(world -> {
                final WorldBorder border = world.getWorldBorder();
                final boolean unset = border.getSize() > 30000;

                final ItemBuilder builder = new ItemBuilder().setMaterial(Material.GRASS_BLOCK);
                builder.setName(ChatColor.GREEN + "Overworld");
                builder.addLore(ChatColor.YELLOW + "Border Size" + ChatColor.RESET + ": " + (unset ? "Not set" : border.getSize()));

                addItem(new Clickable(builder.build(), 21, click -> {}));
            });

            nether.ifPresent(world -> {
                final WorldBorder border = world.getWorldBorder();
                final boolean unset = border.getSize() > 30000;

                final ItemBuilder builder = new ItemBuilder().setMaterial(Material.NETHERRACK);
                builder.setName(ChatColor.RED + "Nether");
                builder.addLore(ChatColor.YELLOW + "Border Size" + ChatColor.RESET + ": " + (unset ? "Not set" : border.getSize()));

                addItem(new Clickable(builder.build(), 22, click -> {}));
            });

            end.ifPresent(world -> {
                final WorldBorder border = world.getWorldBorder();
                final boolean unset = border.getSize() > 30000;

                final ItemBuilder builder = new ItemBuilder().setMaterial(Material.END_STONE);
                builder.setName(ChatColor.LIGHT_PURPLE + "The End");
                builder.addLore(ChatColor.YELLOW + "Border Size" + ChatColor.RESET + ": " + (unset ? "Not set" : border.getSize()));

                addItem(new Clickable(builder.build(), 23, click -> {}));
            });

            fill(new ItemBuilder().setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(ChatColor.RESET + "").build());
            return;
        }

        if (page.equals(EHelpMenuPage.CLASSES)) {
            final String INDENT = ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.GOLD;
            int cursor = 18;

            for (IClass playerClass : plugin.getClassManager().getClasses()) {
                final ItemBuilder builder = new ItemBuilder()
                        .setName(ChatColor.GOLD + playerClass.getName())
                        .addFlag(ItemFlag.HIDE_ATTRIBUTES);

                if (playerClass.getHelmet() != null) {
                    builder.setMaterial(playerClass.getHelmet());
                } else {
                    builder.setMaterial(playerClass.getChestplate());
                }

                final List<String> lore = Lists.newArrayList();

                lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + playerClass.getDescription());

                if (playerClass instanceof final Diver diverClass) {
                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Attributes" + ChatColor.YELLOW + ":");
                    lore.add(INDENT + "Call of the Sea Cooldown" + ChatColor.YELLOW + ": " + Time.convertToRemaining(diverClass.getSeaCallCooldown() * 1000L));
                    lore.add(INDENT + "Trident Bonus Min. Range" + ChatColor.YELLOW + ": " + diverClass.getMinimumRange());
                    lore.add(INDENT + "Trident Bonus Multiplier" + ChatColor.YELLOW + ": " + diverClass.getDamageMultiplier() + "x");
                }

                if (playerClass instanceof final Bard bardClass) {
                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Attributes" + ChatColor.YELLOW + ":");
                    lore.add(INDENT + "Effect Range" + ChatColor.YELLOW + ": " + bardClass.getBardRange() +  " blocks");

                    if (!bardClass.getHoldables().isEmpty()) {
                        lore.add(ChatColor.RESET + " ");
                        lore.add(ChatColor.GOLD + "Holdable Passives" + ChatColor.YELLOW + ":");

                        bardClass.getHoldables().forEach(holdable -> {
                            final String matName = Strings.capitalize(holdable.getMaterial().name().toLowerCase().replaceAll("_", " "));
                            final String effName = Strings.capitalize(holdable.getEffectType().getKey().getKey().toLowerCase().replaceAll("_", " "));
                            final int amplifier = holdable.getAmplifier();
                            final int seconds = holdable.getDuration();

                            lore.add(INDENT + ChatColor.GOLD + matName + ChatColor.YELLOW + ": "
                                    + effName + " " + (amplifier + 1) + " for " + seconds + " seconds");
                        });
                    }
                }

                if (playerClass instanceof final Archer archerClass) {
                    final int percent = (int)Math.round(archerClass.getMarkPercentage() * 100);

                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Attributes" + ChatColor.YELLOW + ":");
                    lore.add(INDENT + "Damage Per Block" + ChatColor.YELLOW + ": " + archerClass.getDamagePerBlock());
                    lore.add(INDENT + "Consecutive Damage Base" + ChatColor.YELLOW + ": " + archerClass.getConsecutiveBase());
                    lore.add(INDENT + "Consecutive Damage Multiplier" + ChatColor.YELLOW + ": " + archerClass.getConsecutiveMultiplier() + "x");
                    lore.add(INDENT + "Max Damage" + ChatColor.YELLOW + ": " + archerClass.getMaxDealtDamage());
                    lore.add(INDENT + "Archer Mark Duration" + ChatColor.YELLOW + ": " + Time.convertToRemaining(archerClass.getMarkDuration() * 1000L));
                    lore.add(INDENT + "Archer Mark Damage Percentage" + ChatColor.YELLOW + ": " + percent + "%");
                }

                if (playerClass instanceof final Rogue rogueClass) {
                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Attributes" + ChatColor.YELLOW + ": ");
                    lore.add(INDENT + "Backstab Cooldown" + ChatColor.YELLOW + ": " + rogueClass.getBackstabCooldown() + "s");
                    lore.add(INDENT + "Backstab Tickrate" + ChatColor.YELLOW + ": " + (double)(rogueClass.getBackstabTickrate() / 20) + "s");
                    lore.add(INDENT + "Backstab Damage" + ChatColor.YELLOW + ": " + rogueClass.getBackstabDamage() + ChatColor.GRAY + "" + ChatColor.ITALIC + " (Total)");
                }

                if (!playerClass.getPassiveEffects().isEmpty()) {
                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Passive Effects" + ChatColor.YELLOW + ":");
                    playerClass.getPassiveEffects().forEach((effect, amplifier) -> lore.add(INDENT + Strings.capitalize(effect.getName().toLowerCase().replaceAll("_", " ")) + ChatColor.YELLOW + ": " + (amplifier + 1)));
                }

                if (!playerClass.getConsumables().isEmpty()) {
                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Consumable Effects" + ChatColor.YELLOW + ":");

                    playerClass.getConsumables().forEach(consumable -> {
                        final String matName = Strings.capitalize(consumable.getMaterial().name().toLowerCase().replaceAll("_", " "));
                        final String effName = Strings.capitalize(consumable.getEffectType().getKey().getKey().toLowerCase().replaceAll("_", " "));
                        final int amplifier = consumable.getAmplifier();
                        final int seconds = consumable.getDuration();
                        String applicationType = null;

                        switch (consumable.getApplicationType()) {
                            case ALL -> applicationType = ChatColor.LIGHT_PURPLE + "All";
                            case FRIEND_ONLY -> applicationType = ChatColor.GREEN + "Friendly Only";
                            case ENEMY_ONLY -> applicationType = ChatColor.RED + "Enemy Only";
                            default -> applicationType = ChatColor.AQUA + "Self";
                        }

                        lore.add(INDENT + ChatColor.GOLD + matName + ChatColor.YELLOW + ": "
                                + effName + " " + (amplifier + 1) + " for " + seconds + " seconds"
                                + ChatColor.GRAY + " [" + applicationType + ChatColor.GRAY + "]");
                    });
                }

                builder.addLore(lore);


                addItem(new Clickable(builder.build(), cursor, click -> {}));
                cursor += 2;
            }

            addItem(backButton);
            fill(new ItemBuilder().setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(ChatColor.RESET + "").build());
            return;
        }
    }

    @Override
    public void open() {
        loadWindow(currentPage);
        super.open();
    }

    @AllArgsConstructor
    public enum EHelpMenuPage {
        HOME(
                "Home",
                ChatColor.YELLOW + "Home",
                ChatColor.GRAY + "Return to the main directory",
                Material.WRITABLE_BOOK,
                0),
        POTION_LIMIT(
                "Potion Limits",
                ChatColor.RED + "Potion Limits",
                ChatColor.GRAY + "View all limited potion details",
                Material.SPLASH_POTION,
                18),
        ENCHANT_LIMIT("Enchantment Limits",
                ChatColor.AQUA + "Enchant Limits",
                ChatColor.GRAY + "View all limited enchantment details",
                Material.ENCHANTED_BOOK,
                20),
        WORLD("World Details",
                ChatColor.GREEN + "World Details",
                ChatColor.GRAY + "View important information related to the Map",
                Material.CARTOGRAPHY_TABLE,
                22),
        CLASSES("Class Details",
                ChatColor.GOLD + "Class Details",
                ChatColor.GRAY + "View a detailed breakdown of each Class",
                Material.GOLDEN_HELMET,
                24),
        RECIPE("Custom Recipes",
                ChatColor.LIGHT_PURPLE + "Custom Crafting Recipes",
                ChatColor.GRAY + "View a list of all Custom Crafting Recipes",
                Material.MAP,
                26);

        @Getter public final String windowTitle;
        @Getter public final String displayName;
        @Getter public final String description;
        @Getter public final Material iconMaterial;
        @Getter public final int position;
    }
}

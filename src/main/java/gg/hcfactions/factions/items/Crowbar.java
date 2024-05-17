package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public final class Crowbar implements ICustomItem {
    @Deprecated public static final String MONSTER_SPAWNER_PREFIX = ChatColor.YELLOW + "Monster Spawners: " + ChatColor.BLUE;
    @Deprecated public static final String END_PORTAL_PREFIX = ChatColor.YELLOW + "End Portal Frames: " + ChatColor.BLUE;

    public static final Component MONSTER_SPAWNER_PREFIX_COMPONENT = Component.text("Monster Spawners:").color(NamedTextColor.YELLOW).appendSpace();
    public static final Component END_PORTAL_PREFIX_COMPONENT = Component.text("End Portal Frames:").color(NamedTextColor.YELLOW).appendSpace();
    public static final Component DURABILITY_PREFIX_COMPONENT = Component.text("Durability", NamedTextColor.GOLD).append(Component.text(":", NamedTextColor.YELLOW)).appendSpace();
    public final NamespacedKey crowbarNamespace;

    public final Factions plugin;

    public Crowbar(Factions plugin) {
        this.plugin = plugin;
        this.crowbarNamespace = new NamespacedKey(plugin, "crowbar");
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_HOE;
    }

    @Override
    public Map.Entry<NamespacedKey, String> getIdentifier() {
        return Map.entry(plugin.getNamespacedKey(), "Crowbar");
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text("Crowbar").color(NamedTextColor.DARK_RED);
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(MONSTER_SPAWNER_PREFIX + 1);
        lore.add(END_PORTAL_PREFIX + 6);
        return lore;
    }

    @Override
    public List<Component> getLoreComponents() {
        return getDurabilityComponent(plugin.getConfiguration().getCrowbarInitialDurability());
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }

    public List<Component> getDurabilityComponent(int durability) {
        List<Component> res = Lists.newArrayList();

        res.add(DURABILITY_PREFIX_COMPONENT.append(Component.text(durability, NamedTextColor.RED)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        res.add(Component.empty());

        res.add(Component.text("Monster Spawner Cost", NamedTextColor.AQUA).append(Component.text(":", NamedTextColor.LIGHT_PURPLE))
                .appendSpace().append(Component.text(plugin.getConfiguration().getCrowbarMonsterSpawnerDurabilityCost(), NamedTextColor.LIGHT_PURPLE))
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        res.add(Component.text("End Portal Cost", NamedTextColor.AQUA).append(Component.text(":", NamedTextColor.LIGHT_PURPLE))
                .appendSpace().append(Component.text(plugin.getConfiguration().getCrowbarEndPortalFrameDurabilityCost(), NamedTextColor.LIGHT_PURPLE))
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        return res;
    }

    public boolean canAfford(ItemStack item, int durabilityCost) {
        int value = getCrowbarDurability(item);
        return value >= durabilityCost;
    }

    public int getCrowbarDurability(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null || !itemMeta.getPersistentDataContainer().has(crowbarNamespace)) {
            return -1;
        }

        return itemMeta.getPersistentDataContainer().get(crowbarNamespace, PersistentDataType.INTEGER);
    }

    public void setCrowbarDurability(ItemStack item, int durability) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            throw new NullPointerException("Null ItemMeta");
        }

        itemMeta.getPersistentDataContainer().set(crowbarNamespace, PersistentDataType.INTEGER, durability);
        itemMeta.lore(getDurabilityComponent(durability));

        item.setItemMeta(itemMeta);
    }

    public void subtractDurability(ItemStack item, int toSubtract) {
        int value = getCrowbarDurability(item);
        int newValue = Math.max(value - toSubtract, 0);
        setCrowbarDurability(item, newValue);
    }

    @Override
    public ItemStack getItem() {
        ItemStack item = ICustomItem.super.getItem();
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null) {
            throw new NullPointerException("Null ItemMeta");
        }

        itemMeta.getPersistentDataContainer().set(crowbarNamespace, PersistentDataType.INTEGER, plugin.getConfiguration().getCrowbarInitialDurability());
        item.setItemMeta(itemMeta);
        return item;
    }
}

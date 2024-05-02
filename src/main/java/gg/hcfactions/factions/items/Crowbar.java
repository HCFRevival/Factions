package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class Crowbar implements ICustomItem {
    @Deprecated public static final String MONSTER_SPAWNER_PREFIX = ChatColor.YELLOW + "Monster Spawners: " + ChatColor.BLUE;
    @Deprecated public static final String END_PORTAL_PREFIX = ChatColor.YELLOW + "End Portal Frames: " + ChatColor.BLUE;
    public static final Component MONSTER_SPAWNER_PREFIX_COMPONENT = Component.text("Monster Spawners:").color(NamedTextColor.YELLOW).appendSpace();
    public static final Component END_PORTAL_PREFIX_COMPONENT = Component.text("End Portal Frames:").color(NamedTextColor.YELLOW).appendSpace();

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_HOE;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_RED + "Crowbar";
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
        final List<Component> res = Lists.newArrayList();
        // TODO: Make configurable
        res.add(MONSTER_SPAWNER_PREFIX_COMPONENT.append(Component.text("1").color(NamedTextColor.BLUE)));
        res.add(END_PORTAL_PREFIX_COMPONENT.append(Component.text("6").color(NamedTextColor.BLUE)));
        return res;
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
}

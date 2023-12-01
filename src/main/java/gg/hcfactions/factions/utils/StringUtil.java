package gg.hcfactions.factions.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

public final class StringUtil {
    public static void formatLore(List<String> lore, String entry, ChatColor color) {
        final String[] split = entry.split(" ");
        final List<String> merged = Lists.newArrayList();

        for (String s : split) {
            if (ChatColor.stripColor(Joiner.on(" ").join(merged)).length() > 20) {
                merged.add(s);
                String merge = Joiner.on(" ").join(merged);

                lore.add(color + merge);
                merged.clear();

                continue;
            }

            merged.add(s);
        }

        if (!merged.isEmpty()) {
            final String mergeString = Joiner.on(" ").join(merged);
            lore.add(color + mergeString);
        }
    }

    public static String getMythicEmblem(Material material) {
        String emblem = "\uD83D\uDDE1"; // Sword

        if (material.name().endsWith("_AXE")) {
            emblem = "\uD83E\uDE93"; // Axe
        } else if (material.equals(Material.BOW) || material.equals(Material.CROSSBOW)) {
            emblem = "\uD83C\uDFF9"; // Bow
        } else if (material.equals(Material.TRIDENT)) {
            emblem = "\uD83D\uDD31"; // Trident
        }

        return emblem;
    }

    public static String getRomanNumeral(int level) {
        final String[] thousands = {"", "M", "MM", "MMM"};
        final String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        final String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        final String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        return thousands[level / 1000] +
                hundreds[(level % 1000) / 100] +
                tens[(level % 100) / 10] +
                ones[level % 10];
    }
}

package gg.hcfactions.factions.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;

import java.util.List;

public final class StringUtil {
    public static List<String> formatLore(List<String> lore, String entry, ChatColor color) {
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

        return lore;
    }
}

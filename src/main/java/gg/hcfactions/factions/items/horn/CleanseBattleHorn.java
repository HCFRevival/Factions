package gg.hcfactions.factions.items.horn;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;

public record CleanseBattleHorn(@Getter Factions plugin) implements ICustomItem, IBattleHorn {
    @Override
    public EBattleHornType getType() {
        return EBattleHornType.CLEANSE;
    }

    @Override
    public List<PotionEffect> getActiveEffects() {
        return Lists.newArrayList();
    }

    @Override
    public Material getMaterial() {
        return Material.GOAT_HORN;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + "Cleanse";
    }

    @Override
    public List<String> getLore() {
        final List<String> res = Lists.newArrayList();
        res.add(ChatColor.RESET + " ");

        final ChatColor descColor = ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.LUCK.getEffectType().getColor().getRed(),
                PotionType.LUCK.getEffectType().getColor().getGreen(),
                PotionType.LUCK.getEffectType().getColor().getBlue())
        );

        res.add(descColor + "Cleanse all debuffs on nearby allies");

        return res;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public ItemStack getItem() {
        final ItemStack item = ICustomItem.super.getItem();
        final ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            final MusicInstrumentMeta instrumentMeta = (MusicInstrumentMeta) meta;
            instrumentMeta.setInstrument(MusicInstrument.SING);
            item.setItemMeta(instrumentMeta);
        }

        return item;
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }
}

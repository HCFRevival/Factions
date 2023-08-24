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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;

public record RetreatBattleHorn(@Getter Factions plugin) implements ICustomItem, IBattleHorn {
    @Override
    public EBattleHornType getType() {
        return EBattleHornType.RETREAT;
    }

    @Override
    public List<PotionEffect> getActiveEffects() {
        final List<PotionEffect> res = Lists.newArrayList();
        res.add(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 2));
        res.add(new PotionEffect(PotionEffectType.WEAKNESS, 30 * 20, 0));
        res.add(new PotionEffect(PotionEffectType.INVISIBILITY, 60 * 20, 0));
        return res;
    }

    @Override
    public Map<PotionEffect, Integer> getPostEffects() {
        return Maps.newHashMap();
    }

    @Override
    public Material getMaterial() {
        return Material.GOAT_HORN;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_RED + "Retreat!";
    }

    @Override
    public List<String> getLore() {
        final List<String> res = Lists.newArrayList();
        res.add(ChatColor.RESET + " ");

        final ChatColor speedColor = ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.SPEED.getEffectType().getColor().getRed(),
                PotionType.SPEED.getEffectType().getColor().getGreen(),
                PotionType.SPEED.getEffectType().getColor().getBlue())
        );

        final ChatColor weaknessColor = ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.WEAKNESS.getEffectType().getColor().getRed(),
                PotionType.WEAKNESS.getEffectType().getColor().getGreen(),
                PotionType.WEAKNESS.getEffectType().getColor().getBlue())
        );

        final ChatColor invisibilityColor = ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.INVISIBILITY.getEffectType().getColor().getRed(),
                PotionType.INVISIBILITY.getEffectType().getColor().getGreen(),
                PotionType.INVISIBILITY.getEffectType().getColor().getBlue())
        );

        res.add(speedColor + "Speed III for 0:15");
        res.add(weaknessColor + "Weakness I for 0:30");
        res.add(invisibilityColor + "Invisibility for 0:30");

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
            instrumentMeta.setInstrument(MusicInstrument.YEARN);
            item.setItemMeta(instrumentMeta);
        }

        return item;
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }
}

package gg.hcfactions.factions.items.horn.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.horn.IBattleHorn;
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

public record BerserkBattleHorn(@Getter Factions plugin) implements ICustomItem, IBattleHorn {
    @Override
    public EBattleHornType getType() {
        return EBattleHornType.BERSERK;
    }

    @Override
    public List<PotionEffect> getActiveEffects() {
        final List<PotionEffect> res = Lists.newArrayList();
        res.add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 30 * 20, 0));
        res.add(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 2));
        res.add(new PotionEffect(PotionEffectType.FAST_DIGGING, 30 * 20, 1));
        return res;
    }

    @Override
    public Map<PotionEffect, Integer> getPostEffects() {
        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20 * 20, 1);
        final PotionEffect weak = new PotionEffect(PotionEffectType.WEAKNESS, 15 * 20, 0);
        final Map<PotionEffect, Integer> res = Maps.newHashMap();

        res.put(slow, 30);
        res.put(weak, 30);

        return res;
    }

    @Override
    public Material getMaterial() {
        return Material.GOAT_HORN;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Berserk";
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

        final ChatColor strColor = ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.STRENGTH.getEffectType().getColor().getRed(),
                PotionType.STRENGTH.getEffectType().getColor().getGreen(),
                PotionType.STRENGTH.getEffectType().getColor().getBlue())
        );

        final ChatColor slowColor = ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.SLOWNESS.getEffectType().getColor().getRed(),
                PotionType.SLOWNESS.getEffectType().getColor().getGreen(),
                PotionType.SLOWNESS.getEffectType().getColor().getBlue())
        );

        final ChatColor weakColor = ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.WEAKNESS.getEffectType().getColor().getRed(),
                PotionType.WEAKNESS.getEffectType().getColor().getGreen(),
                PotionType.WEAKNESS.getEffectType().getColor().getBlue())
        );

        final ChatColor hasteColor = ChatColor.YELLOW;

        res.add(speedColor + "Speed III for 0:30");
        res.add(strColor + "Strength I for 0:30");
        res.add(hasteColor + "Haste II for 0:30");

        res.add(ChatColor.RESET + " ");
        res.add(ChatColor.RED + "Side-effects");
        res.add(slowColor + "Slowness II for 0:20");
        res.add(weakColor + "Weakness I for 0:15");

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
            instrumentMeta.setInstrument(MusicInstrument.CALL);
            item.setItemMeta(instrumentMeta);
        }

        return item;
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }
}

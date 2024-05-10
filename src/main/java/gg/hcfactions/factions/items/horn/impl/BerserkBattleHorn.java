package gg.hcfactions.factions.items.horn.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.horn.IBattleHorn;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
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
        res.add(new PotionEffect(PotionEffectType.STRENGTH, 30 * 20, 0));
        res.add(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 2));
        res.add(new PotionEffect(PotionEffectType.HASTE, 30 * 20, 1));
        return res;
    }

    @Override
    public Map<PotionEffect, Integer> getPostEffects() {
        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOWNESS, 20 * 20, 1);
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
    public Map.Entry<NamespacedKey, String> getIdentifier() {
        return Map.entry(plugin.getNamespacedKey(), "BerserkBattleHorn");
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text("Berserk", NamedTextColor.RED);
    }

    @Override
    public List<String> getLore() {
        final List<String> res = Lists.newArrayList();
        res.add(ChatColor.RESET + " ");

        final ChatColor speedColor = ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.SWIFTNESS.getEffectType().getColor().getRed(),
                PotionType.SWIFTNESS.getEffectType().getColor().getGreen(),
                PotionType.SWIFTNESS.getEffectType().getColor().getBlue())
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
    public List<Component> getLoreComponents() {
        final List<Component> res = Lists.newArrayList();
        final PotionEffectType slowness = PotionEffectType.SLOWNESS;
        final PotionEffectType weakness = PotionEffectType.WEAKNESS;
        final PotionEffectType speed = PotionEffectType.SPEED;
        final PotionEffectType strength = PotionEffectType.STRENGTH;
        final PotionEffectType haste = PotionEffectType.HASTE;

        if (speed != null) {
            res.add(Component.text("Speed III for 0:30").color(TextColor.color(speed.getColor().getRed(), speed.getColor().getGreen(), speed.getColor().getBlue())));
        }

        if (strength != null) {
            res.add(Component.text("Strength I for 0:30").color(TextColor.color(strength.getColor().getRed(), strength.getColor().getGreen(), strength.getColor().getBlue())));
        }

        if (haste != null) {
            res.add(Component.text("Haste II for 0:30").color(TextColor.color(haste.getColor().getRed(), haste.getColor().getGreen(), haste.getColor().getBlue())));
        }

        res.add(Component.text(" "));
        res.add(Component.text("Side-effects").color(NamedTextColor.RED));

        if (slowness != null) {
            res.add(Component.text("Slowness II for 0:20").color(TextColor.color(slowness.getColor().getRed(), slowness.getColor().getGreen(), slowness.getColor().getBlue())));
        }

        if (weakness != null) {
            res.add(Component.text("Weakness I for 0:15").color(TextColor.color(weakness.getColor().getRed(), weakness.getColor().getGreen(), weakness.getColor().getBlue())));
        }

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
            instrumentMeta.setInstrument(MusicInstrument.CALL_GOAT_HORN);
            item.setItemMeta(instrumentMeta);
        }

        return item;
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }
}

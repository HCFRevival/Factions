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

public record ChargeBattleHorn(@Getter Factions plugin) implements ICustomItem, IBattleHorn {
    @Override
    public EBattleHornType getType() {
        return EBattleHornType.CHARGE;
    }

    @Override
    public List<PotionEffect> getActiveEffects() {
        final List<PotionEffect> res = Lists.newArrayList();
        res.add(new PotionEffect(PotionEffectType.SPEED, 180 * 20, 1));
        res.add(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 900 * 20, 0));
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
    public Map.Entry<NamespacedKey, String> getIdentifier() {
        return Map.entry(plugin.getNamespacedKey(), "ChargeBattleHorn");
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text("Charge!").color(NamedTextColor.GOLD);
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

        final ChatColor fresColor = ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.FIRE_RESISTANCE.getEffectType().getColor().getRed(),
                PotionType.FIRE_RESISTANCE.getEffectType().getColor().getGreen(),
                PotionType.FIRE_RESISTANCE.getEffectType().getColor().getBlue())
        );

        res.add(speedColor + "Speed II for 3:00");
        res.add(fresColor + "Fire Resistance for 15:00");

        return res;
    }

    @Override
    public List<Component> getLoreComponents() {
        final List<Component> res = Lists.newArrayList();
        final PotionEffectType speed = PotionEffectType.SPEED;
        final PotionEffectType fireResistance = PotionEffectType.FIRE_RESISTANCE;

        res.add(Component.text(" "));

        if (speed != null) {
            res.add(Component.text("Speed II for 3:00").color(TextColor.color(speed.getColor().getRed(), speed.getColor().getGreen(), speed.getColor().getBlue())));
        }

         if (fireResistance != null) {
             res.add(Component.text("Fire Resistance for 15:00").color(TextColor.color(fireResistance.getColor().getRed(), fireResistance.getColor().getGreen(), fireResistance.getColor().getBlue())));
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
            instrumentMeta.setInstrument(MusicInstrument.PONDER_GOAT_HORN);
            item.setItemMeta(instrumentMeta);
        }

        return item;
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }
}

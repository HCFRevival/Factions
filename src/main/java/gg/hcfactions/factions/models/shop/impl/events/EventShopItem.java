package gg.hcfactions.factions.models.shop.impl.events;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.shop.impl.GenericShopItem;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EventShopItem extends GenericShopItem {
    @Getter public final int tokenPrice;

    public EventShopItem(
            UUID id,
            Component displayName,
            Material material,
            int amount,
            List<Component> lore,
            Map<Enchantment, Integer> enchantments,
            boolean disabled,
            int position,
            int tokenPrice
    ) {
        super(id, displayName, material, amount, lore, enchantments, position, disabled, 0.0, 0.0);
        this.tokenPrice = tokenPrice;
    }

    public EventShopItem(
            UUID id,
            Component displayName,
            Material material,
            int amount,
            List<Component> lore,
            Map<Enchantment, Integer> enchantments,
            boolean disabled,
            int position,
            int tokenPrice,
            ICustomItem customItemClass
    ) {
        super(id, displayName, material, amount, lore, enchantments, position, disabled, 0.0, 0.0, customItemClass);
        this.tokenPrice = tokenPrice;
    }

    @Override
    public ItemStack getItem(boolean asDisplay) {
        final ItemBuilder builder = new ItemBuilder();
        final List<Component> lore = Lists.newArrayList();

        if (customItemClass != null && !asDisplay) {
            return customItemClass.getItem();
        }

        builder.setMaterial(material);
        builder.setAmount(amount);
        builder.addFlag(ItemFlag.HIDE_ATTRIBUTES);

        if (getDisplayName() != null) {
            builder.setName(getDisplayName());
        }

        if (getEnchantments() != null && !getEnchantments().isEmpty()) {
            builder.addEnchant(enchantments);
        }

        if (getLore() != null && !getLore().isEmpty()) {
            lore.addAll(getLore());
        }

        if (asDisplay) {
            if (isDisabled()) {
                builder.setName(Component.text("NOT FOR SALE", NamedTextColor.DARK_RED));
            }

            lore.add(Component.text(" "));
            lore.add(Component.text("Token Buy Price", NamedTextColor.AQUA).append(Component.text(": " + tokenPrice, NamedTextColor.RED)));
        }

        builder.addLoreComponents(lore);
        return builder.build();
    }
}

package gg.hcfactions.factions.models.shop.impl.events;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.models.shop.impl.GenericShopItem;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import lombok.Getter;
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

    public EventShopItem(UUID id, String displayName, Material material, int amount, Map<Enchantment, Integer> enchantments,  boolean disabled, int position, int tokenPrice) {
        super(id, displayName, material, amount, enchantments, position, disabled, 0.0, 0.0);
        this.tokenPrice = tokenPrice;
    }

    @Override
    public ItemStack getItem(boolean asDisplay) {
        final ItemBuilder builder = new ItemBuilder();
        builder.setMaterial(material);
        builder.setAmount(amount);
        builder.addFlag(ItemFlag.HIDE_ATTRIBUTES);

        if (getDisplayName() != null) {
            builder.setName(getDisplayName());
        }

        if (getEnchantments() != null && !getEnchantments().isEmpty()) {
            builder.addEnchant(enchantments);
        }

        if (asDisplay) {
            if (isDisabled()) {
                builder.setName(ChatColor.DARK_RED + "NOT FOR SALE");
            }

            final List<String> lore = Lists.newArrayList();
            lore.add(ChatColor.RESET + " ");
            lore.add(ChatColor.AQUA + "Token Buy Price" + ChatColor.RED + ": " + tokenPrice);
            builder.addLore(lore);
        }

        return builder.build();
    }
}

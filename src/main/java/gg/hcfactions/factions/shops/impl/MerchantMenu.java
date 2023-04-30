package gg.hcfactions.factions.shops.impl;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.shop.impl.GenericMerchant;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.menu.impl.GenericMenu;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class MerchantMenu extends GenericMenu {
    @Getter public final Factions plugin;
    private final GenericMerchant<?> merchant;

    public MerchantMenu(Factions plugin, Player player, GenericMerchant<?> merchant) {
        super(plugin, player, merchant.getMerchantName(), 1);
        this.plugin = plugin;
        this.merchant = merchant;
    }

    @Override
    public void open() {
        super.open();

        merchant.getShops().forEach(shop ->
                addItem(new Clickable(shop.getIcon(), shop.getPosition(), click ->
                        plugin.getShopManager().getExecutor().openShop(player, merchant, shop))));
    }
}

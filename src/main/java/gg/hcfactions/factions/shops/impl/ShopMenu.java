package gg.hcfactions.factions.shops.impl;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.shop.impl.GenericMerchant;
import gg.hcfactions.factions.models.shop.impl.GenericShop;
import gg.hcfactions.factions.models.shop.impl.GenericShopItem;
import gg.hcfactions.factions.utils.PlayerUtil;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.menu.impl.GenericMenu;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ShopMenu extends GenericMenu {
    @Getter public final Factions plugin;
    @Getter public final GenericMerchant merchant;
    private final GenericShop shop;

    public ShopMenu(Factions plugin, Player player, GenericMerchant merchant, GenericShop shop) {
        super(plugin, player, shop.getShopName(), (shop.getItems().size() > 9) ? shop.getItems().size() % 9 : 1);
        this.plugin = plugin;
        this.merchant = merchant;
        this.shop = shop;
    }

    @Override
    public void open() {
        super.open();

        final ItemBuilder backButton = new ItemBuilder();
        backButton.setMaterial(Material.BARRIER);
        backButton.setName(ChatColor.RED + "Back to " + merchant.getMerchantName());

        addItem(new Clickable(backButton.build(), getInventory().getSize() - 1, click -> {
            player.closeInventory();
            plugin.getShopManager().getExecutor().openMerchant(player, merchant);
        }));

        shop.getItems().forEach(item -> addItem(new Clickable(item.getItem(true), item.getPosition(), click -> {
            if (click.isLeftClick()) {
                if (!item.isBuyable()) {
                    return;
                }

                handleBuy(item, new Promise() {
                    @Override
                    public void resolve() {}

                    @Override
                    public void reject(String s) {
                        player.closeInventory();
                        player.sendMessage(ChatColor.RED + s);
                    }
                });

                return;
            }

            if (click.isRightClick()) {
                if (!item.isSellable()) {
                    return;
                }

                handleSell(item, new Promise() {
                    @Override
                    public void resolve() {}

                    @Override
                    public void reject(String s) {
                        player.closeInventory();
                        player.sendMessage(ChatColor.RED + s);
                    }
                });
            }
        })));
    }

    private void handleBuy(GenericShopItem item, Promise promise) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (!factionPlayer.canAfford(item.getBuyPrice()) && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            promise.reject(FError.P_CAN_NOT_AFFORD.getErrorDescription());
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            promise.reject("You do not have enough space in your inventory");
            return;
        }

        final String itemName = item.getDisplayName() != null ? item.getDisplayName() : StringUtils.capitalize(item.getMaterial().getKey().getKey());

        factionPlayer.subtractFromBalance(item.getBuyPrice());

        player.getInventory().addItem(item.getItem(false));
        player.sendMessage(FMessage.LAYER_1 + "Purchased " + ChatColor.AQUA + "x" + item.getAmount()
                + ChatColor.RESET + " " + itemName + FMessage.LAYER_1 + " for " + ChatColor.DARK_GREEN
                + String.format("%.2f", item.getBuyPrice()));

        promise.resolve();
    }

    private void handleSell(GenericShopItem item, Promise promise) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        final ItemStack found = PlayerUtil.getFirstItemStackByMaterial(player, item.getMaterial());
        final String itemName = item.getDisplayName() != null ? item.getDisplayName() : StringUtils.capitalize(item.getMaterial().getKey().getKey());

        if (found == null) {
            promise.reject("You do not have any " + itemName + " in your inventory");
            return;
        }

        int soldAmount = 1;
        double soldPrice = 0.0;

        if (found.getAmount() < item.getAmount()) {
            final double pricePer = item.getSellPrice() / item.getAmount();
            final double value = found.getAmount() * pricePer;

            soldPrice = value;
            soldAmount = found.getAmount();

            player.getInventory().remove(found);
            factionPlayer.addToBalance(value);
        } else {
            soldAmount = item.getAmount();
            soldPrice = item.getSellPrice();

            found.setAmount(found.getAmount() - item.getAmount());
            factionPlayer.addToBalance(item.getSellPrice());
        }

        player.sendMessage(
                FMessage.LAYER_1 + "Sold " + ChatColor.AQUA + "x" + soldAmount
                + ChatColor.RESET + " " + itemName
                + FMessage.LAYER_1 + " for " + ChatColor.DARK_GREEN
                + "$" + String.format("%.2f", soldPrice));
    }
}

package gg.hcfactions.factions.shops.impl;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.shop.impl.GenericMerchant;
import gg.hcfactions.factions.models.shop.impl.GenericShop;
import gg.hcfactions.factions.models.shop.impl.GenericShopItem;
import gg.hcfactions.factions.models.shop.impl.events.EventMerchant;
import gg.hcfactions.factions.models.shop.impl.events.EventShopItem;
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
    @Getter public final GenericMerchant<?> merchant;
    private final GenericShop<?> shop;

    public ShopMenu(Factions plugin, Player player, GenericMerchant<?> merchant, GenericShop<?> shop) {
        super(plugin, player, ChatColor.stripColor(shop.getShopName()), 6);
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

        if (merchant instanceof EventMerchant) {
            shop.getItems().forEach(item -> addItem(new Clickable(item.getItem(true), item.getPosition(), click -> handleEventBuy((EventShopItem) item, new Promise() {
                @Override
                public void resolve() {}

                @Override
                public void reject(String s) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + s);
                }
            }))));

            fill(new ItemBuilder().setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(ChatColor.RESET + "").build());
            return;
        }

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

        fill(new ItemBuilder().setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(ChatColor.RESET + "").build());
    }

    private void handleEventBuy(EventShopItem item, Promise promise) {
        if (item.isDisabled()) {
            promise.reject("This item is currently disabled. Check back later.");
            return;
        }

        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        if (!faction.canAffordWithTokens(item.getTokenPrice())) {
            promise.reject(FError.P_CAN_NOT_AFFORD.getErrorDescription());
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            promise.reject("You do not have enough space in your inventory");
            return;
        }

        final String itemName = item.getDisplayName() != null ? item.getDisplayName() : StringUtils.capitalize(item.getMaterial().getKey().getKey().replaceAll("_", " "));

        faction.subtractTokens(item.getTokenPrice());
        player.getInventory().addItem(item.getItem(false));

        faction.sendMessage(FMessage.P_NAME + player.getName() + FMessage.LAYER_1 + " has "
                + FMessage.SUCCESS + "purchased" + ChatColor.AQUA + " x" + item.getAmount() + ChatColor.RESET + " "
                + itemName + FMessage.LAYER_1 + " for " + ChatColor.DARK_AQUA + item.getTokenPrice() + " tokens");

        plugin.getAresLogger().info(player.getName() + " purchased x" + item.getAmount() + itemName + " with " + item.getTokenPrice() + " tokens");

        promise.resolve();
    }

    private void handleBuy(GenericShopItem item, Promise promise) {
        if (item.isDisabled()) {
            promise.reject("This item is currently disabled. Check back later.");
            return;
        }

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

        final String itemName = item.getDisplayName() != null ? item.getDisplayName() : StringUtils.capitalize(item.getMaterial().getKey().getKey().replaceAll("_", " "));

        factionPlayer.subtractFromBalance(item.getBuyPrice());

        player.getInventory().addItem(item.getItem(false));
        player.sendMessage(FMessage.LAYER_1 + "Purchased " + ChatColor.AQUA + "x" + item.getAmount()
                + ChatColor.RESET + " " + itemName + FMessage.LAYER_1 + " for " + ChatColor.DARK_GREEN
                + String.format("%.2f", item.getBuyPrice()));

        plugin.getAresLogger().info(player.getName() + " purchased x" + item.getAmount() + itemName + " for $" + String.format("%.2f", item.getBuyPrice()));

        promise.resolve();
    }

    private void handleSell(GenericShopItem item, Promise promise) {
        if (item.isDisabled()) {
            promise.reject("This item is currently disabled. Check back later.");
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        final ItemStack found = PlayerUtil.getFirstItemStackByMaterial(player, item.getMaterial());
        final String itemName = item.getDisplayName() != null ? item.getDisplayName() : StringUtils.capitalize(item.getMaterial().getKey().getKey().replaceAll("_", " "));

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

        plugin.getAresLogger().info(player.getName() + " sold x" + item.getAmount() + itemName + " for $" + String.format("%.2f", item.getBuyPrice()));
    }
}

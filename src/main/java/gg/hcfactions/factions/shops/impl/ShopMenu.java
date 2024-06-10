package gg.hcfactions.factions.shops.impl;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.EventShopTransactionEvent;
import gg.hcfactions.factions.listeners.events.player.ShopTransactionEvent;
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
import gg.hcfactions.libs.base.util.Strings;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.menu.impl.GenericMenu;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ShopMenu extends GenericMenu {
    @Getter public final Factions plugin;
    @Getter public final GenericMerchant<?> merchant;
    private final GenericShop<?> shop;

    public ShopMenu(Factions plugin, Player player, GenericMerchant<?> merchant, GenericShop<?> shop) {
        super(plugin, player, PlainTextComponentSerializer.plainText().serialize(merchant.getMerchantName()), 6);
        this.plugin = plugin;
        this.merchant = merchant;
        this.shop = shop;
    }

    @Override
    public void open() {
        super.open();

        final ItemBuilder backButton = new ItemBuilder();
        backButton.setMaterial(Material.BARRIER);
        backButton.setName(Component.text("Back to", NamedTextColor.RED).appendSpace().append(merchant.getMerchantName()));

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

        final Component itemName = item.getDisplayName() != null
                ? item.getDisplayName()
                : Component.text(Strings.capitalize(item.getMaterial().getKey().getKey().replaceAll("_", " ")), NamedTextColor.WHITE);

        EventShopTransactionEvent transactionEvent = new EventShopTransactionEvent(player, faction, item.getItem(false), item.getTokenPrice());
        Bukkit.getPluginManager().callEvent(transactionEvent);
        if (transactionEvent.isCancelled()) {
            promise.reject("Transaction Cancelled");
            return;
        }

        faction.subtractTokens(transactionEvent.getAmount().intValue());
        player.getInventory().addItem(transactionEvent.getItem());

        faction.sendMessage(
                Component.text(player.getName(), FMessage.TC_NAME)
                        .appendSpace().append(Component.text("has", FMessage.TC_LAYER1))
                        .appendSpace().append(Component.text("purchased", FMessage.TC_SUCCESS))
                        .appendSpace().append(Component.text("x" + item.getAmount(), FMessage.TC_LAYER2))
                        .appendSpace().append(itemName).colorIfAbsent(FMessage.TC_LAYER2)
                        .appendSpace().append(Component.text("for", FMessage.TC_LAYER1))
                        .appendSpace().append(Component.text(transactionEvent.getAmount().intValue() + " tokens", FMessage.TC_INFO))
        );

        plugin.getAresLogger().info("{} purchased x{} {} with {} tokens", player.getName(), item.getAmount(), itemName, transactionEvent.getAmount());

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

        final Component itemName = item.getDisplayName() != null
                ? item.getDisplayName()
                : Component.text(Strings.capitalize(item.getMaterial().getKey().getKey().replaceAll("_", " ")));

        ShopTransactionEvent transactionEvent = new ShopTransactionEvent(player, item.getItem(false), ShopTransactionEvent.ETransactionType.BUY, item.getBuyPrice());
        Bukkit.getPluginManager().callEvent(transactionEvent);
        if (transactionEvent.isCancelled()) {
            promise.reject("Transaction Cancelled");
            return;
        }

        factionPlayer.subtractFromBalance(transactionEvent.getAmount().doubleValue());

        player.getInventory().addItem(transactionEvent.getItem());
        player.sendMessage(Component.text("Purchased", FMessage.TC_LAYER1)
                .appendSpace().append(Component.text("x" + item.getAmount(), FMessage.TC_LAYER2))
                .appendSpace().append(itemName).colorIfAbsent(FMessage.TC_LAYER2)
                .appendSpace().append(Component.text("for", FMessage.TC_LAYER1))
                .appendSpace().append(Component.text("$" + String.format("%.2f", item.getBuyPrice()), NamedTextColor.DARK_GREEN)));

        plugin.getAresLogger().info("{} purchased x{} {} for ${}", player.getName(), item.getAmount(), itemName, String.format("%.2f", item.getBuyPrice()));

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
        final Component itemName = item.getDisplayName() != null
                ? item.getDisplayName()
                : Component.text(Strings.capitalize(item.getMaterial().getKey().getKey().replaceAll("_", " ")));

        if (found == null) {
            promise.reject("You do not have any " + PlainTextComponentSerializer.plainText().serialize(itemName) + " in your inventory");
            return;
        }

        ShopTransactionEvent transactionEvent = new ShopTransactionEvent(player, item.getItem(false), ShopTransactionEvent.ETransactionType.SELL, item.getSellPrice());
        Bukkit.getPluginManager().callEvent(transactionEvent);
        if (transactionEvent.isCancelled()) {
            promise.reject("Transaction Cancelled");
            return;
        }

        int soldAmount = 1;
        double soldPrice = 0.0;

        if (found.getAmount() < item.getAmount()) {
            final double pricePer = transactionEvent.getAmount().doubleValue() / item.getAmount();
            final double value = found.getAmount() * pricePer;

            soldPrice = value;
            soldAmount = found.getAmount();

            player.getInventory().remove(found);
            factionPlayer.addToBalance(value);
        } else {
            soldAmount = item.getAmount();
            soldPrice = transactionEvent.getAmount().doubleValue();

            found.setAmount(found.getAmount() - item.getAmount());
            factionPlayer.addToBalance(item.getSellPrice());
        }

        player.sendMessage(
                Component.text("Sold", FMessage.TC_LAYER1)
                        .appendSpace().append(Component.text("x" + soldAmount, FMessage.TC_LAYER2)
                                .appendSpace().append(itemName).colorIfAbsent(FMessage.TC_LAYER2)
                                .appendSpace().append(Component.text("for", FMessage.TC_LAYER1))
                                .appendSpace().append(Component.text("$" + String.format("%.2f", soldPrice), NamedTextColor.DARK_GREEN)))
        );

        plugin.getAresLogger().info("{} sold x{} {} for ${}", player.getName(), item.getAmount(), itemName, String.format("%.2f", item.getBuyPrice()));
    }
}

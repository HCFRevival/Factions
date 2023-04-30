package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("shop")
@AllArgsConstructor
public final class ShopCommand extends BaseCommand {
    @Getter public Factions plugin;

    @Subcommand("create")
    @Description("Create a new merchant")
    @Syntax("<name>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onCreateMerchant(Player player, String merchantName) {
        plugin.getShopManager().getExecutor().createMerchant(player, merchantName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Merchant created");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to create merchant: " + s);
            }
        });
    }

    @Subcommand("addshop")
    @Description("Add a shop to an existing merchant")
    @Syntax("<merchant> <name> <position>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onAddToMerchant(Player player, String merchantName, String shopName, String positionName) {
        int position;

        try {
            position = Integer.parseInt(positionName);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid position (not a number)");
            return;
        }

        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().equals(Material.AIR)) {
            player.sendMessage(ChatColor.RED + "You are not holding an item");
            return;
        }

        plugin.getShopManager().getExecutor().addToMerchant(player, merchantName, shopName, hand, position, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Shop created");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to create shop: " + s);
            }
        });
    }

    @Subcommand("additem")
    @Description("Add an item to an existing shop")
    @Syntax("<merchant> <shop> <position> <buycost> <sellcost>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onAddToShop(Player player, String merchantName, String shopName, String positionName, String buyName, String sellName) {
        int position;
        double buyCost;
        double sellCost;

        try {
            position = Integer.parseInt(positionName);
            buyCost = Double.parseDouble(buyName);
            sellCost = Double.parseDouble(sellName);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid item attributes");
            return;
        }

        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().equals(Material.AIR)) {
            player.sendMessage(ChatColor.RED + "You are not holding an item");
            return;
        }

        plugin.getShopManager().getExecutor().addToShop(player, merchantName, shopName, hand, position, buyCost, sellCost, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Item added to shop");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to add to shop: " + s);
            }
        });
    }
}

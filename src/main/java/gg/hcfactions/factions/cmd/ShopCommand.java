package gg.hcfactions.factions.cmd;

import com.google.common.base.Joiner;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.CommandHelp;
import gg.hcfactions.libs.acf.CommandIssuer;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@CommandAlias("shop")
@AllArgsConstructor
public final class ShopCommand extends BaseCommand {
    @Getter public Factions plugin;

    @Subcommand("create")
    @Description("Create a new merchant")
    @Syntax("[-e] <name>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onCreateMerchant(Player player, String args) {
        final String[] split = args.split(" ");
        String merchantName = args;
        boolean isEvent = false;

        if (split[0].equalsIgnoreCase("-e")) {
            isEvent = true;

            if (split.length <= 1) {
                player.sendMessage(ChatColor.RED + "Merchant name required");
                return;
            }

            merchantName = Joiner.on(" ").join(Arrays.asList(split).subList(1, split.length));
        }

        plugin.getShopManager().getExecutor().createMerchant(player, merchantName, isEvent, new Promise() {
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
    @CommandCompletion("@merchants")
    public void onAddToMerchant(Player player, String merchantName, String shopName, int position) {
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

    @Subcommand("remshop")
    @Description("Remove a shop from an existing merchant")
    @Syntax("<merchant> <name>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@merchants")
    public void onRemoveFromMerchant(Player player, String merchantName, String shopName) {
        plugin.getShopManager().getExecutor().removeFromMerchant(player, merchantName, shopName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Shop deleted");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to delete shop: " + s);
            }
        });
    }

    @Subcommand("additem")
    @Description("Add an item to an existing shop")
    @Syntax("<merchant> <shop> <position> <buycost> <sellcost>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@merchants")
    public void onAddToShop(Player player, String merchantName, String shopName, int position, double buyCost, double sellCost) {
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

    @Subcommand("additem")
    @Description("Add an item to an existing event shop")
    @Syntax("<merchant> <shop> <position> <tokens>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@merchants")
    public void onAddToShop(Player player, String merchantName, String shopName, int position, int tokens) {
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().equals(Material.AIR)) {
            player.sendMessage(ChatColor.RED + "You are not holding an item");
            return;
        }

        plugin.getShopManager().getExecutor().addToEventShop(player, merchantName, shopName, hand, position, tokens, new Promise() {
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

    @Subcommand("remitem|delitem")
    @Description("Remove an item from an existing shop")
    @Syntax("<merchant> <shop> <index>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@merchants")
    public void onRemoveItem(Player player, String merchantName, String shopName, int index) {
        plugin.getShopManager().getExecutor().removeFromShop(player, merchantName, shopName, index, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Item has been removed from shop");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + "Failed to remove item from shop: " + s);
            }
        });
    }

    @Subcommand("delete")
    @Description("Delete an existing merchant")
    @Syntax("<merchant>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @CommandCompletion("@merchants")
    public void onRemoveMerchant(Player player, String merchantName) {
        plugin.getShopManager().getExecutor().deleteMerchant(player, merchantName, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(ChatColor.GREEN + "Merchant deleted");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(ChatColor.RED + s);
            }
        });
    }

    @HelpCommand
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onHelp(CommandIssuer issuer, CommandHelp help) {
        help.showHelp(issuer);
    }
}

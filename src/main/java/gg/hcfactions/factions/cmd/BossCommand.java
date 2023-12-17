package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.bosses.BossLootManager;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.bukkit.loot.impl.GenericLootable;
import gg.hcfactions.libs.bukkit.loot.impl.LootTableMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@CommandAlias("boss")
public final class BossCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("loot")
    @Description("View the current Boss Loot table")
    public void onBossLoot(Player player) {
        final LootTableMenu<GenericLootable> menu = new LootTableMenu<>(
                plugin,
                player,
                plugin.getBossManager().getLootManager(),
                plugin.getBossManager().getLootManager().getLootRepository()
        );

        menu.open();
    }

    @Subcommand("loot add")
    @Syntax("<min> <max> <prob>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Description("Add a new item to the Boss Loot Table")
    public void onBossLootAdd(Player player, int min, int max, int probability) {
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().equals(Material.AIR)) {
            player.sendMessage(FMessage.ERROR + "You are not holding an item");
            return;
        }

        final GenericLootable lootable = new GenericLootable(hand, UUID.randomUUID().toString(), min, max, probability);

        plugin.getBossManager().getLootManager().getLootRepository().add(lootable);
        plugin.getBossManager().getLootManager().saveItem(
                BossLootManager.FILE_NAME,
                BossLootManager.FILE_KEY,
                hand,
                min,
                max,
                probability
        );

        player.sendMessage(ChatColor.GREEN + "Lootable Created");
    }

    @Subcommand("loot simulate")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Description("Simulate loot tables for (n) amount of runs")
    @Syntax("<amount per run> <total runs>")
    public void onLootSimulate(Player player, int amountPerRun, int totalRuns) {
        if (totalRuns > 100) {
            player.sendMessage(ChatColor.RED + "Run limit max is 100");
            return;
        }

        final List<GenericLootable> loot = plugin.getBossManager().getLootManager().simulateDrops(plugin.getBossManager().getLootManager().getLootRepository(), amountPerRun, totalRuns);

        if (loot.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No results found (is the table empty?)");
            return;
        }

        final LootTableMenu<GenericLootable> menu = new LootTableMenu<>(plugin, player, plugin.getBossManager().getLootManager(), loot);
        menu.open();
    }

    @Subcommand("loot reload")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Description("Reload the Boss Loot Tables")
    public void onBossLootReload(CommandSender sender) {
        plugin.getBossManager().getLootManager().onReload();
        sender.sendMessage(ChatColor.GREEN + "Reloaded Boss Loot Tables");
    }
}

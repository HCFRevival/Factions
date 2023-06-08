package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.Crowbar;
import gg.hcfactions.factions.models.logger.impl.CombatLogger;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.CommandPermission;
import gg.hcfactions.libs.acf.annotation.Subcommand;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("debug")
public final class DebugCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("cl")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onCombatLoggerSpawn(Player player) {
        final CustomItemService cis = (CustomItemService) plugin.getService(CustomItemService.class);
        cis.getItem(Crowbar.class).ifPresent(crowbar -> {
            player.getInventory().addItem(crowbar.getItem());
            player.sendMessage(ChatColor.YELLOW + "Crowbar has been added to your inventory");
        });
    }
}

package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.menus.HelpMenu;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.Default;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("help|info")
public final class FactionHelpCommand extends BaseCommand {
    @Getter public Factions plugin;

    @Default
    public void onHelp(Player player) {
        final HelpMenu menu = new HelpMenu(plugin, player);
        menu.open();
    }
}

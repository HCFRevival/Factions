package gg.hcfactions.factions.cmd;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.CommandPermission;
import gg.hcfactions.libs.acf.annotation.Description;
import gg.hcfactions.libs.acf.annotation.Subcommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@CommandAlias("class")
public final class ClassCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Subcommand("reload")
    @Description("Reload all class data from file")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onReload(Player player) {
        final Map<IClass, Set<UUID>> prevClasses = Maps.newHashMap();

        plugin.getClassManager().getClasses().forEach(classInstance -> {
            prevClasses.put(classInstance, classInstance.getActivePlayers());

            classInstance.getActivePlayers().forEach(uuid -> {
                final Player classPlayer = Bukkit.getPlayer(uuid);

                if (classPlayer != null) {
                    classInstance.deactivate(classPlayer, true);
                }
            });
        });

        plugin.getClassManager().onReload();

        prevClasses.forEach((classInstance, uuids) -> uuids.forEach(uuid -> {
            final Player classPlayer = Bukkit.getPlayer(uuid);

            if (classPlayer != null) {
                classInstance.activate(classPlayer, true);
            }
        }));

        player.sendMessage(ChatColor.GREEN + "Class configuration reloaded");
    }
}

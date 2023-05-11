package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.utils.Configs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

@CommandAlias("spawn")
@AllArgsConstructor
public final class SpawnCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Default
    public void onSpawn(Player player) {
        if (!player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            final int x = (int)Math.round(player.getWorld().getSpawnLocation().getX());
            final int z = (int)Math.round(player.getWorld().getSpawnLocation().getZ());
            player.sendMessage(ChatColor.RED + "Spawn is located at " + x + ", " + z);
            return;
        }

        if (player.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            player.teleport(plugin.getConfiguration().getOverworldSpawn());
        }

        else if (player.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            player.teleport(player.getWorld().getSpawnLocation());
        }

        else if (player.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
            player.teleport(plugin.getConfiguration().getEndSpawn());
        }

        player.sendMessage(FMessage.LAYER_1 + "Teleported to " + FMessage.INFO + "Spawn");
    }

    @Subcommand("set")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    @Description("Set the spawnpoint for your world")
    @Syntax("[endexit]")
    public void onSetOverworld(Player player, @Optional String endExit) {
        String writeLocation;
        if (player.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            final boolean flag = (endExit != null && endExit.equalsIgnoreCase("endexit"));

            if (flag) {
                plugin.getConfiguration().setEndExit(player.getLocation());
                writeLocation = "end_exit";
            } else {
                plugin.getConfiguration().setOverworldSpawn(player.getLocation());
                writeLocation = "overworld_spawn";
            }
        }

        else if (player.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            player.sendMessage(ChatColor.RED + "Spawnpoint can not be set in The Nether");
            return;
        }

        else {
            plugin.getConfiguration().setEndSpawn(player.getLocation());
            writeLocation = "end_spawn";
        }

        final YamlConfiguration conf = plugin.loadConfiguration("config");
        Configs.writePlayerLocation(conf, "factions.spawns." + writeLocation, new PLocatable(player));
        plugin.saveConfiguration("config", conf);
        player.sendMessage(FMessage.SUCCESS + "Spawnpoint updated");
    }
}

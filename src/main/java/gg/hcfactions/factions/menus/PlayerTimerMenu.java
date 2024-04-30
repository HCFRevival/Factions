package gg.hcfactions.factions.menus;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.menu.impl.PaginatedMenu;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class PlayerTimerMenu extends PaginatedMenu<IFactionPlayer> {
    @Getter public final Factions plugin;
    @Getter public ETimerType timerType;
    @Getter public List<IFactionPlayer> timerPlayers;

    public PlayerTimerMenu(Factions plugin, Player player, ETimerType timerType, Collection<IFactionPlayer> entries) {
        super(plugin, player, ChatColor.stripColor(timerType.getLegacyDisplayName()) + " Timers", 6, entries);
        this.plugin = plugin;
        this.timerType = timerType;
        this.timerPlayers = Lists.newArrayList(entries);
    }

    @Override
    public void open() {
        super.open();

        addUpdater(() -> {
            timerPlayers.clear();
            timerPlayers.addAll(plugin.getPlayerManager().getPlayerRepository().stream().filter(fp -> fp.hasTimer(timerType)).toList());
            update();
        }, 20L);
    }

    @Override
    public List<IFactionPlayer> sort() {
        final List<IFactionPlayer> sorted = Lists.newArrayList(timerPlayers);
        sorted.sort(Comparator.comparing(IFactionPlayer::getUsername));
        return sorted;
    }

    @Override
    public Clickable getItem(IFactionPlayer factionPlayer, int pos) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta meta = (SkullMeta) item.getItemMeta();
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(factionPlayer.getUniqueId());
        final FTimer timer = factionPlayer.getTimer(timerType);

        if (timer == null) {
            return null;
        }

        if (meta != null) {
            final List<String> lore = Lists.newArrayList();
            lore.add(ChatColor.DARK_AQUA + "Remaining" + ChatColor.WHITE + ": " + Time.convertToRemaining(factionPlayer.getTimer(timerType).getRemaining()));

            meta.setDisplayName(ChatColor.YELLOW + factionPlayer.getUsername());
            meta.setOwningPlayer(offlinePlayer);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return new Clickable(item, pos, click -> {
            if (click.isLeftClick()) {
                final Player targetPlayer = Bukkit.getPlayer(factionPlayer.getUniqueId());

                if (targetPlayer == null) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "Player not found");
                    return;
                }

                player.teleport(targetPlayer);
                player.sendMessage(ChatColor.YELLOW + "Teleported to " + ChatColor.BLUE + targetPlayer.getName());
            }
        });
    }
}

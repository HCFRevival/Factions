package gg.hcfactions.factions.models.battlepass.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.battlepass.EBPObjectiveType;
import gg.hcfactions.factions.models.battlepass.EBPState;
import gg.hcfactions.factions.models.battlepass.IBPObjective;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.base.util.Strings;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.menu.impl.Icon;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import gg.hcfactions.libs.bukkit.services.impl.xp.XPService;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class BPObjective implements IBPObjective {
    @Getter public final Factions plugin;
    @Getter public final String identifier;
    @Getter @Setter public EBPState state;
    @Getter @Setter public Icon icon;
    @Getter @Setter public EBPObjectiveType objectiveType;
    @Getter @Setter public Material blockRequirement;
    @Getter @Setter public EntityType entityRequirement;
    @Getter @Setter public UUID claimRequirement;
    @Getter @Setter public World.Environment worldRequirement;
    @Getter @Setter public IClass classRequirement;
    @Getter @Setter public int amountRequirement;
    @Getter @Setter public int baseExp;

    public BPObjective(Factions plugin, String id) {
        this.plugin = plugin;
        this.identifier = id;
        this.state = EBPState.INACTIVE;
    }

    public ItemStack getMenuItem(BPTracker viewer) {
        final RankService rankService = (RankService) plugin.getService(RankService.class);
        final Player bukkitPlayer = Bukkit.getPlayer(viewer.getOwnerId());
        final ItemStack item = icon.getItemStack();
        final ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        final String oldDisplayName = meta.getDisplayName();
        final List<String> lore = meta.getLore() != null ? meta.getLore() : Lists.newArrayList();

        meta.setDisplayName(state.getDisplayName() + ChatColor.RESET + " " + oldDisplayName);

        if (hasEntityRequirement()) {
            final String prettyEntityName = Strings.capitalize(getEntityRequirement().name().toLowerCase().replaceAll("_", " ")) + (amountRequirement > 1 ? "s" : "");
            lore.add(ChatColor.GRAY + "Slay " + ChatColor.WHITE + "" + amountRequirement + " " + prettyEntityName);
        }

        if (hasBlockRequirement()) {
            final String prettyBlockName = Strings.capitalize(getBlockRequirement().name().toLowerCase().replaceAll("_", " "));
            lore.add(ChatColor.GRAY + "Gather " + ChatColor.WHITE + "" + amountRequirement + " " + prettyBlockName);
        }

        if (objectiveType.equals(EBPObjectiveType.CAPTURE_KOTH_TICKET)) {
            lore.add(ChatColor.GRAY + "Earn " + ChatColor.WHITE + "" + amountRequirement + " Tickets" + ChatColor.GRAY + " during " + ChatColor.WHITE + "King of the Hill");
        }

        if (objectiveType.equals(EBPObjectiveType.DPS_CHECK_DAMAGE)) {
            lore.add(ChatColor.GRAY + "Inflict " + ChatColor.WHITE + "" + String.format("%,d", amountRequirement) + " Damage" + ChatColor.GRAY + " during " + ChatColor.WHITE + "DPS Check");
        }

        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.RED + "Additional Requirements:");

        if (hasWorldRequirement()) {
            final String prettyWorldName = Strings.capitalize(getWorldRequirement().name().toLowerCase().replaceAll("_", " "));
            lore.add(ChatColor.GOLD + "World" + ChatColor.WHITE + ": " + prettyWorldName);
        }

        if (hasClaimRequirement()) {
            final IFaction faction = plugin.getFactionManager().getFactionById(getClaimRequirement());

            if (faction instanceof final PlayerFaction playerFaction) {
                lore.add(ChatColor.GOLD + "Claim" + ChatColor.WHITE + ": " + playerFaction.getName());
            } else if (faction instanceof final ServerFaction serverFaction) {
                lore.add(ChatColor.GOLD + "Claim" + ChatColor.WHITE + ": " + serverFaction.getDisplayName());
            }
        }

        if (hasClassRequirement()) {
            lore.add(ChatColor.GOLD + "Class" + ChatColor.WHITE + ": " + getClassRequirement().getName());
        }

        lore.add(ChatColor.RESET + " ");

        if (viewer.hasCompleted(this)) {
            lore.add(ChatColor.GRAY + "Your progress" + ChatColor.WHITE + ": " + ChatColor.GREEN + "Complete");
        } else if (viewer.getProgress(this) > 0) {
            lore.add(ChatColor.GRAY + "Your progress" + ChatColor.WHITE + ": " + viewer.getProgress(this) + ChatColor.GRAY + "/" + ChatColor.WHITE + amountRequirement);
        } else {
            lore.add(ChatColor.GRAY + "Your progress" + ChatColor.WHITE + ": " + ChatColor.RED + "Not started");
        }

        lore.add(ChatColor.RESET + " ");
        lore.add(XPService.XP_COLOR_ACCENT_INFO + "EXP Reward" + ChatColor.WHITE + ": +" + baseExp + " EXP");

        if (rankService != null && bukkitPlayer != null) {
            final AresRank highestRank = rankService.getHighestRank(bukkitPlayer);

            if (highestRank != null) {
                final double rankMultiplier = plugin.getBattlepassManager().getRankMultipliers().getOrDefault(highestRank, 0.0);

                if (rankMultiplier > 0.0) {
                    final int percent = (int)Math.round((rankMultiplier - 1.0) * 100);

                    lore.add(ChatColor.AQUA + "" + percent + "%" + ChatColor.DARK_AQUA + " EXP Bonus thanks to your "
                            + net.md_5.bungee.api.ChatColor.of(highestRank.getColorCode()) + Strings.capitalize(highestRank.getName()) + " Rank");
                }
            }
        }

        lore.add(ChatColor.RESET + " ");

        final long expire = (state.equals(EBPState.DAILY))
                ? plugin.getBattlepassManager().getDailyExpireTimestamp()
                : plugin.getBattlepassManager().getWeeklyExpireTimestamp();

        lore.add(ChatColor.GRAY + "Ends in" + ChatColor.WHITE + ": " + ChatColor.GREEN
                + Time.convertToRemaining(expire - Time.now()));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
}

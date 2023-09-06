package gg.hcfactions.factions.models.battlepass.impl;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.battlepass.EBPObjectiveType;
import gg.hcfactions.factions.models.battlepass.IBPObjective;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.libs.base.util.Strings;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.menu.impl.Icon;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
import gg.hcfactions.libs.bukkit.services.impl.xp.XPService;
import gg.hcfactions.libs.bukkit.utils.Colors;
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
    @Getter @Setter public boolean active;
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
    }

    public ItemStack getMenuItem(BPTracker viewer) {
        final RankService rankService = (RankService) plugin.getService(RankService.class);
        final Player bukkitPlayer = Bukkit.getPlayer(viewer.getOwnerId());
        final ItemStack item = icon.getItemStack();
        final ItemMeta meta = item.getItemMeta();

        if (meta == null || meta.getLore() == null) {
            return item;
        }

        final List<String> lore = meta.getLore();

        if (!lore.isEmpty()) {
            lore.add(ChatColor.RESET + " ");
        }

        lore.add(ChatColor.GOLD + "Amount" + ChatColor.WHITE + ": " + amountRequirement);

        if (hasEntityRequirement()) {
            final String prettyEntityName = Strings.capitalize(getEntityRequirement().name().toLowerCase().replaceAll("_", " "));
            lore.add(ChatColor.GOLD + "Creature" + ChatColor.WHITE + ": " + prettyEntityName);
        }

        if (hasBlockRequirement()) {
            final String prettyBlockName = Strings.capitalize(getBlockRequirement().name().toLowerCase().replaceAll("_", " "));
            lore.add(ChatColor.GOLD + "Block" + ChatColor.WHITE + ": " + prettyBlockName);
        }

        if (hasWorldRequirement()) {
            final String prettyWorldName = Strings.capitalize(getWorldRequirement().name().toLowerCase().replaceAll("_", " "));
            lore.add(ChatColor.GOLD + "World" + ChatColor.WHITE + ": " + prettyWorldName);
        }

        if (hasClassRequirement()) {
            lore.add(ChatColor.GOLD + "Class" + ChatColor.WHITE + ": " + getClassRequirement().getName());
        }

        lore.add(ChatColor.RESET + " ");

        if (viewer.hasCompleted(this)) {
            lore.add(ChatColor.GRAY + "Your progress" + ChatColor.WHITE + ": " + ChatColor.GREEN + "Complete");
        } else if (viewer.getProgress(this) > 0) {
            lore.add(ChatColor.GRAY + "Your progress" + ChatColor.WHITE + ": " + viewer.getProgress(this) + ChatColor.GRAY + "/" + ChatColor.RESET + amountRequirement);
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
        lore.add(ChatColor.GRAY + "Ends in" + ChatColor.WHITE + ": " + Colors.GREEN.toBukkit()
                + Time.convertToRemaining(plugin.getBattlepassManager().getDailyExpireTimestamp() - Time.now()));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
}

package gg.hcfactions.factions.cmd;

import com.google.common.collect.Lists;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.Default;
import gg.hcfactions.libs.bukkit.remap.ERemappedEffect;
import gg.hcfactions.libs.bukkit.remap.ERemappedEnchantment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@CommandAlias("help|info")
public final class FactionHelpCommand extends BaseCommand {
    @Getter public Factions plugin;

    @Default
    public void onHelp(Player player) {
        final CXService cxService = (CXService)plugin.getService(CXService.class);

        player.sendMessage(FMessage.LAYER_2 + "" + ChatColor.BOLD + "Factions Info");
        player.sendMessage(FMessage.LAYER_2 + "Map" + FMessage.LAYER_1 + ": " + plugin.getConfiguration().getMapNumber());

        if (cxService != null) {
            final List<String> enchantmentLimits = Lists.newArrayList();
            final List<String> potionLimits = Lists.newArrayList();

            cxService.getEnchantLimitModule().getEnchantLimits().forEach((enchantment, level) -> {
                final String enchantmentName = StringUtils.capitalize(ERemappedEnchantment.getRemappedEnchantment(enchantment).name().toLowerCase().replaceAll("_", " "));
                enchantmentLimits.add(FMessage.LAYER_1 + enchantmentName + " " + (level == 0 ? FMessage.ERROR + "Disabled" : FMessage.LAYER_1 + "" + level));
            });

            cxService.getPotionLimitModule().getPotionLimits().forEach(potionLimit -> {
                final String potionName = StringUtils.capitalize(ERemappedEffect.getRemappedEffect(potionLimit.getType()).name().toLowerCase().replaceAll("_", " "));
                final List<String> values = Lists.newArrayList();
                final String amplify = potionLimit.isAmplifiable() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No";
                final String extend = potionLimit.isExtendable() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No";
                final String disabled = potionLimit.isDisabled() ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No";

                potionLimits.add(FMessage.LAYER_1 + potionName + ": " + "Amplifiable: " + amplify + FMessage.LAYER_1 + ", Extendable: " + extend + FMessage.LAYER_1 + ", Disabled: " + disabled);
            });

            if (!enchantmentLimits.isEmpty()) {
                player.sendMessage(ChatColor.RESET + " ");
                player.sendMessage(FMessage.LAYER_2 + "Enchantment Limits" + FMessage.LAYER_1 + ":");
                enchantmentLimits.forEach(limit -> player.sendMessage(ChatColor.RESET + " " + FMessage.LAYER_1 + " - " + limit));
            }

            if (!potionLimits.isEmpty()) {
                if (!enchantmentLimits.isEmpty()) {
                    player.sendMessage(ChatColor.RESET + " ");
                }

                player.sendMessage(FMessage.LAYER_2 + "Potion Limits" + FMessage.LAYER_1 + ":");
                potionLimits.forEach(limit -> player.sendMessage(ChatColor.RESET + " " + FMessage.LAYER_1 + " - " + limit));
            }
        }

        final Optional<World> overworld = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment().equals(World.Environment.NORMAL)).findFirst();
        final Optional<World> nether = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment().equals(World.Environment.NETHER)).findFirst();
        final Optional<World> end = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment().equals(World.Environment.THE_END)).findFirst();

        if (overworld.isPresent() || nether.isPresent() || end.isPresent()) {
            player.sendMessage(ChatColor.RESET + " ");
            player.sendMessage(FMessage.LAYER_2 + "World Borders" + FMessage.LAYER_1 + ":");
        }

        overworld.ifPresent(w -> player.sendMessage(ChatColor.RESET + " " + FMessage.LAYER_1 + " - Overworld" + ": " + FMessage.INFO + ((w.getWorldBorder().getSize() > 25000.0) ? "None" : w.getWorldBorder().getSize())));
        nether.ifPresent(w -> player.sendMessage(ChatColor.RESET + " " + FMessage.LAYER_1 + " - The Nether" + ": " + FMessage.INFO + ((w.getWorldBorder().getSize() > 25000.0) ? "None" : w.getWorldBorder().getSize())));
        end.ifPresent(w -> player.sendMessage(ChatColor.RESET + " " + FMessage.LAYER_1 + " - The End" + ": " + FMessage.INFO + ((w.getWorldBorder().getSize() > 25000.0) ? "None" : w.getWorldBorder().getSize())));

        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(FMessage.LAYER_2 + "Useful Links" + FMessage.LAYER_1 + ":");

        player.spigot().sendMessage(new ComponentBuilder(" ")
                        .append(" - [")
                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                        .append("Events")
                        .color(net.md_5.bungee.api.ChatColor.DARK_AQUA)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event help"))
                        .append("]")
                        .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .create());

        player.spigot().sendMessage(new ComponentBuilder(" ")
                .append(" - [")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .append("Stats")
                .color(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats"))
                .append("]")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .create());

        player.spigot().sendMessage(new ComponentBuilder(" ")
                .append(" - [")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .append("Website")
                .color(net.md_5.bungee.api.ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://hcfrevival.net"))
                .append("]")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .create());
    }
}

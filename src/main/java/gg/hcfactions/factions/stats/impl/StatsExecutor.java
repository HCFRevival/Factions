package gg.hcfactions.factions.stats.impl;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.menus.StatsMenu;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.impl.PlayerStatHolder;
import gg.hcfactions.factions.stats.IStatsExecutor;
import gg.hcfactions.factions.stats.StatsManager;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public record StatsExecutor(@Getter StatsManager manager) implements IStatsExecutor {
    private Map<ItemStack, Integer> getMenuItems(PlayerStatHolder holder) {
        final Map<ItemStack, Integer> res = Maps.newHashMap();

        for (EStatisticType t : EStatisticType.values()) {
            final ItemBuilder builder = new ItemBuilder();
            final long v = holder.getStatistic(t);

            builder.setMaterial(t.getIcon());
            builder.setName(FMessage.LAYER_2 + t.getDisplayName());

            if (t.equals(EStatisticType.PLAYTIME)) {
                builder.addLore(FMessage.LAYER_1 + Time.convertToRemaining(v));
            }

            else if (t.equals(EStatisticType.LONGSHOT)) {
                builder.addLore(FMessage.LAYER_1 + "" + v + " blocks");
            }

            else if (t.equals(EStatisticType.EXP_EARNED)) {
                builder.addLore(FMessage.LAYER_1 + "" + v + " exp");
            }

            else if (t.equals(EStatisticType.EVENT_CAPTURES)) {
                builder.addLore(FMessage.LAYER_1 + "" + v + " events captured");
            }

            else {
                builder.addLore(FMessage.LAYER_1 + "" + v);
            }

            res.put(builder.build(), t.getInventoryPosition());
        }

        return res;
    }

    @Override
    public void openPlayerStats(Player viewer, String username, Promise promise) {
        final AccountService acs = (AccountService) manager.getPlugin().getService(AccountService.class);
        if (acs == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        acs.getAccount(username, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount aresAccount) {
                if (aresAccount == null) {
                    promise.reject(FError.P_NOT_FOUND.getErrorDescription());
                    return;
                }

                manager.getPlayerStatistics(aresAccount.getUniqueId(), holder -> {
                    if (holder == null) {
                        promise.reject(FError.P_NOT_FOUND.getErrorDescription());
                        return;
                    }

                    final StatsMenu menu = new StatsMenu(manager.getPlugin(), viewer, aresAccount.getUsername());
                    final Map<ItemStack, Integer> items = getMenuItems(holder);

                    items.forEach((item, pos) -> menu.addItem(new Clickable(item, pos, clickType -> {})));
                    menu.open();
                    promise.resolve();
                });
            }

            @Override
            public void reject(String s) {
                promise.reject(s);
            }
        });
    }

    @Override
    public void openFactionStats(Player viewer, String factionName, Promise promise) {

    }
}

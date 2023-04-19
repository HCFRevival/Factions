package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
@CommandAlias("state")
public final class StateCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @CommandAlias("state")
    public void onViewState(Player player) {
        player.sendMessage(FMessage.LAYER_2 + "Current state" + FMessage.LAYER_1 + ": " + plugin.getServerStateManager().getCurrentState().getDisplayName());
    }

    @Subcommand("set")
    @Description("Update server state")
    @Syntax("<state>")
    @CommandPermission(FPermissions.P_FACTIONS_ADMIN)
    public void onSetState(Player player, String stateName) {
        final EServerState state = EServerState.fromString(stateName);
        if (state == null) {
            player.sendMessage(FMessage.ERROR + "Invalid server state");
            return;
        }

        plugin.getServerStateManager().getExecutor().updateState(state, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(FMessage.SUCCESS + "Server state has been updated");
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to set server state: " + s);
            }
        });
    }
}

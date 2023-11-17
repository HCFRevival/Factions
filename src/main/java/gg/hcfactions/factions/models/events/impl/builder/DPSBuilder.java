package gg.hcfactions.factions.models.events.impl.builder;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.builder.EDPSBuildStep;
import gg.hcfactions.factions.models.events.builder.IDPSEventBuilder;
import gg.hcfactions.factions.models.events.impl.DPSEventConfig;
import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.UUID;

public final class DPSBuilder implements IDPSEventBuilder<DPSEvent> {
    @Getter public final Factions plugin;
    @Getter @Setter EDPSBuildStep currentStep;
    @Getter public final UUID builderId;
    @Getter public final String name;
    @Getter public String displayName;
    @Getter public ServerFaction owner;
    @Getter public BLocatable initialSpawnpoint;

    public DPSBuilder(Factions plugin, UUID builderId, String name) {
        this.plugin = plugin;
        this.currentStep = EDPSBuildStep.DISPLAY_NAME;
        this.builderId = builderId;
        this.name = name;
        this.displayName = null;
        this.owner = null;
        this.initialSpawnpoint = null;

        getBuilder().sendMessage(ChatColor.DARK_AQUA + "Enter the display name of the event");
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        currentStep = EDPSBuildStep.SERVER_FACTION;

        getBuilder().sendMessage(ChatColor.YELLOW + "Event display name has been updated to " + ChatColor.RESET + this.displayName);
        getBuilder().sendMessage(ChatColor.DARK_AQUA + "Enter the name of the Server Faction this event belongs to:");
    }

    @Override
    public void setOwner(String factionName) {
        final ServerFaction faction = plugin.getFactionManager().getServerFactionByName(factionName);

        if (faction == null) {
            getBuilder().sendMessage(ChatColor.RED + FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        owner = faction;
        currentStep = EDPSBuildStep.INITIAL_SPAWNPOINT;

        giveWand();
        getBuilder().sendMessage(ChatColor.DARK_AQUA + "Select the initial spawnpoint for the DPS Entity (more can be added later using " + ChatColor.YELLOW + "/event dps addspawn " + name);
    }

    @Override
    public void setInitialSpawnpoint(BLocatable initialSpawnpoint) {
        this.initialSpawnpoint = initialSpawnpoint;

        build(new FailablePromise<DPSEvent>() {
            @Override
            public void resolve(DPSEvent dpsEvent) {
                plugin.getEventManager().getEventRepository().add(dpsEvent);
                plugin.getEventManager().getBuilderManager().getBuilderRepository().removeIf(b -> b.getBuilderId().equals(builderId));
                plugin.getEventManager().saveDpsEvent(dpsEvent);

                getBuilder().sendMessage(ChatColor.GREEN + "Event created");
            }

            @Override
            public void reject(String s) {
                getBuilder().sendMessage(ChatColor.RED + "Failed to create event: " + s);
            }
        });
    }

    @Override
    public void build(FailablePromise<DPSEvent> promise) {
        if (displayName == null) {
            promise.reject("Display name is not set");
            return;
        }

        if (owner == null) {
            promise.reject("Server Faction is not set");
            return;
        }

        if (initialSpawnpoint == null) {
            promise.reject("Initial spawnpoint is not set");
            return;
        }

        final DPSEvent event = new DPSEvent(
                plugin,
                owner.getUniqueId(),
                name,
                displayName,
                new DPSEventConfig((3600*1000), 10),
                Lists.newArrayList(initialSpawnpoint),
                Lists.newArrayList()
        );

        promise.resolve(event);
    }
}

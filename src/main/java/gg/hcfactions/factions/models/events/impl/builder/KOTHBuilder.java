package gg.hcfactions.factions.models.events.impl.builder;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.builder.ECEBuildStep;
import gg.hcfactions.factions.models.events.builder.ICaptureEventBuilder;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.UUID;

@Getter
public final class KOTHBuilder implements ICaptureEventBuilder<KOTHEvent> {
    public final Factions plugin;
    public final UUID builderId;
    public final String name;
    public ServerFaction owner;
    public String displayName;
    public BLocatable cornerA;
    public BLocatable cornerB;
    @Setter ECEBuildStep currentStep;

    public KOTHBuilder(Factions plugin, UUID builderId, String name) {
        this.plugin = plugin;
        this.currentStep = ECEBuildStep.DISPLAY_NAME;
        this.builderId = builderId;
        this.name = name;
        this.displayName = null;
        this.owner = null;
        this.cornerA = null;
        this.cornerB = null;

        getBuilder().sendMessage(ChatColor.DARK_AQUA + "Enter the display name of the event");
    }

    @Override
    public void setOwner(String factionName) {
        final ServerFaction faction = plugin.getFactionManager().getServerFactionByName(factionName);

        if (faction == null) {
            getBuilder().sendMessage(ChatColor.RED + FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        owner = faction;
        currentStep = ECEBuildStep.CORNER_A;
        giveWand();
        getBuilder().sendMessage(ChatColor.DARK_AQUA + "Select the first corner of the Capture Region");
    }

    public void setDisplayName(String name) {
        displayName = ChatColor.translateAlternateColorCodes('&', name);
        currentStep = ECEBuildStep.OWNER;

        getBuilder().sendMessage(ChatColor.YELLOW + "Event display name has been updated to " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', name));
        getBuilder().sendMessage(ChatColor.DARK_AQUA + "Enter the name of the Server Faction this event belongs to");
    }

    @Override
    public void setCornerA(BLocatable location) {
        cornerA = location;
        currentStep = ECEBuildStep.CORNER_B;

        getBuilder().sendMessage(ChatColor.YELLOW + "Corner A has been set");
        getBuilder().sendMessage(ChatColor.DARK_AQUA + "Select the second corner of the Capture Region");
    }

    @Override
    public void setCornerB(BLocatable location) {
        cornerB = location;

        // final step in the build process
        build(new FailablePromise<>() {
            @Override
            public void resolve(KOTHEvent kothEvent) {
                plugin.getEventManager().getEventRepository().add(kothEvent);
                plugin.getEventManager().getBuilderManager().getBuilderRepository().removeIf(b -> b.getBuilderId().equals(builderId));
                plugin.getEventManager().saveCaptureEvent(kothEvent);

                getBuilder().sendMessage(ChatColor.GREEN + "Event created");
            }

            @Override
            public void reject(String s) {
                getBuilder().sendMessage(ChatColor.RED + "Failed to create event: " + s);
            }
        });
    }

    @Override
    public void build(FailablePromise<KOTHEvent> promise) {
        if (displayName == null) {
            promise.reject("Display name is not set");
            return;
        }

        if (cornerA == null) {
            promise.reject("Corner A is not set");
            return;
        }

        if (cornerB == null) {
            promise.reject("Corner B is not set");
            return;
        }

        final KOTHEvent event = new KOTHEvent(
                plugin,
                owner.getUniqueId(),
                name,
                displayName,
                Lists.newArrayList(),
                new CaptureRegion(cornerA, cornerB),
                new CaptureEventConfig(
                        20,
                        60,
                        100,
                        0,
                        0,
                        -1
                )
        );

        promise.resolve(event);
    }
}

package gg.hcfactions.factions.models.events.impl.builder;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.builder.ECEBuildStep;
import gg.hcfactions.factions.models.events.builder.ICaptureEventBuilder;
import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.events.impl.ConquestZone;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.util.UUID;

public final class ConquestZoneBuilder implements ICaptureEventBuilder<ConquestZone> {
    @Getter public final Factions plugin;
    @Getter public final ConquestEvent parentEvent;
    @Getter public final UUID builderId;
    @Getter public final String name;
    @Getter public ServerFaction owner;
    @Getter public String displayName;
    @Getter @Setter ECEBuildStep currentStep;
    @Getter public BLocatable cornerA;
    @Getter public BLocatable cornerB;

    public ConquestZoneBuilder(Factions plugin, ConquestEvent parentEvent, UUID builderId, String name) {
        this.plugin = plugin;
        this.parentEvent = parentEvent;
        this.builderId = builderId;
        this.name = name;
        this.displayName = null;
        this.owner = null;
        this.currentStep = ECEBuildStep.DISPLAY_NAME;

        getBuilder().sendMessage(ChatColor.DARK_AQUA + "Enter the display name of the Conquest Zone");
    }

    @Override // Not needed for Conquest Zones
    public void setOwner(String factionName) {}

    @Override
    public void setDisplayName(String name) {
        displayName = ChatColor.translateAlternateColorCodes('&', name);
        currentStep = ECEBuildStep.CORNER_A;

        getBuilder().sendMessage(ChatColor.YELLOW + "Event display name has been updated to " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', name));
        getBuilder().sendMessage(ChatColor.DARK_AQUA + "Select the first corner of the Capture Region");

        giveWand();
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

        build(new FailablePromise<>() {
            @Override
            public void resolve(ConquestZone conquestZone) {
                getBuilder().sendMessage(ChatColor.GREEN + "Zone created");
                parentEvent.getZones().add(conquestZone);
                plugin.getEventManager().saveConquestEvent(parentEvent);
                plugin.getEventManager().getBuilderManager().getBuilderRepository().removeIf(b -> b.getBuilderId().equals(builderId));
            }

            @Override
            public void reject(String s) {
                getBuilder().sendMessage(ChatColor.RED + "Failed to create event zone: " + s);
            }
        });
    }

    @Override
    public void build(FailablePromise<ConquestZone> promise) {
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

        final ConquestZone zone = new ConquestZone(parentEvent, name, displayName, new CaptureRegion(cornerA, cornerB));
        promise.resolve(zone);
    }
}

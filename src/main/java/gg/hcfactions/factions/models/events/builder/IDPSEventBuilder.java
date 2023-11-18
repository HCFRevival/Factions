package gg.hcfactions.factions.models.events.builder;

import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;

import java.util.List;

public interface IDPSEventBuilder extends IEventBuilder {
    EDPSBuildStep getCurrentStep();
    String getDisplayName();
    List<BLocatable> getSpawnpoints();

    void setCurrentStep(EDPSBuildStep step);
    void setSpawnpoint(BLocatable point);
    void build(FailablePromise<DPSEvent> promise);
}

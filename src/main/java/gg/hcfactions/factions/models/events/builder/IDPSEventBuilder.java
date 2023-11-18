package gg.hcfactions.factions.models.events.builder;

import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;

public interface IDPSEventBuilder<T> extends IEventBuilder {
    EDPSBuildStep getCurrentStep();

    String getDisplayName();
    BLocatable getInitialSpawnpoint();

    void setCurrentStep(EDPSBuildStep step);
    void setInitialSpawnpoint(BLocatable location);
    void build(FailablePromise<T> promise);
}

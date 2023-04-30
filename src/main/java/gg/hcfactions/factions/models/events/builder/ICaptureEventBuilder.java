package gg.hcfactions.factions.models.events.builder;

import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;

public interface ICaptureEventBuilder<T> extends IEventBuilder {
    ECEBuildStep getCurrentStep();

    String getDisplayName();
    BLocatable getCornerA();
    BLocatable getCornerB();

    void setCurrentStep(ECEBuildStep step);
    void setCornerA(BLocatable location);
    void setCornerB(BLocatable location);

    void build(FailablePromise<T> promise);
}

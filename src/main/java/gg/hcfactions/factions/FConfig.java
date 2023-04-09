package gg.hcfactions.factions;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public final class FConfig {
    // faction naming
    @Getter public int minFactionNameLength;
    @Getter public int maxFactionNameLength;
    @Getter public List<String> disallowedFactionNames;

    // claiming
    @Getter public int defaultServerFactionBuildBuffer;
    @Getter public int defaultServerFactionClaimBuffer;
    @Getter public int defaultPlayerFactionClaimBuffer;

    // TODO: load values from config
    public void loadConfig() {
        minFactionNameLength = 3;
        maxFactionNameLength = 16;
        disallowedFactionNames = Arrays.asList("test");

        defaultServerFactionBuildBuffer = 16;
        defaultServerFactionClaimBuffer = 32;
        defaultPlayerFactionClaimBuffer = 1;
    }
}

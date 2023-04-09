package gg.hcfactions.factions;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public final class FConfig {
    @Getter public int minFactionNameLength;
    @Getter public int maxFactionNameLength;
    @Getter public List<String> disallowedFactionNames;

    public void loadConfig() {
        minFactionNameLength = 3;
        maxFactionNameLength = 16;
        disallowedFactionNames = Arrays.asList("test");
    }
}

package gg.hcfactions.factions.manager;

public interface IManager {
    void onEnable();
    void onDisable();
    default void onReload() {}
}

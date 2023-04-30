package gg.hcfactions.factions.models.shop;

public interface ITokenHolder {
    int getTokens();

    void setTokens(int i);

    default void addTokens(int amount) {
        setTokens(getTokens() + amount);
    }

    default void subtractTokens(int amount) {
        setTokens(Math.max(0, (getTokens() - amount)));
    }

    default boolean canAffordWithTokens(int amount) {
        return getTokens() >= amount;
    }
}

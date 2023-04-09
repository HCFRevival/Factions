package gg.hcfactions.factions.models.econ;

public interface IBankable {
    /**
     * @return Bank balance
     */
    double getBalance();

    /**
     * Sets the balance to the provided amount
     * @param amt Amount to set to
     */
    void setBalance(double amt);

    /**
     * Adds the provided amount to the existing balance
     * @param amt Amount to add
     */
    default void addToBalance(double amt) {
        setBalance(getBalance() + amt);
    }

    /**
     * Subtracts the provided amount from the existing balance
     * @param amt Amount to subtract
     */
    default void subtractFromBalance(double amt) {
        if (!canAfford(amt)) {
            setBalance(0);
            return;
        }

        setBalance(getBalance() - amt);
    }

    /**
     * Returns true if this account can afford the provided price
     * @param amt Amount to compare
     * @return True if this account can afford this price
     */
    default boolean canAfford(double amt) {
        return getBalance() >= amt;
    }
}

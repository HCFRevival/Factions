package gg.hcfactions.factions.models.claim;

public enum EClaimBufferType {
    BUILD,
    CLAIM;

    /**
     * Returns buffer type matching provided name
     * @param name Name to query
     * @return EClaimBufferType
     */
    public static EClaimBufferType getBufferTypeByName(String name) {
        for (EClaimBufferType v : values()) {
          if (v.name().equalsIgnoreCase(name)) {
              return v;
          }
        }

        return null;
    }
}

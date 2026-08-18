package deepbluehaven.pojo.enums;

public enum RoomTag {
    GARDEN_VIEW("Garden View"),
    POOL_VIEW("Pool View"),
    OCEAN_VIEW("Ocean View"),
    PARTIAL_OCEAN_VIEW("Partial Ocean View"),
    BEACHFRONT("Beachfront");

    private final String displayName;

    RoomTag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
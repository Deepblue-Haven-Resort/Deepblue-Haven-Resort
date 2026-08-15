package deepbluehaven.pojo.enums;

public enum ServiceCategory {
    FOOD_BEVERAGE("Food Beverage"),
    LAUNDRY("Laundry"),
    SPA("Spa"),
    TRANSPORT("Transport"),
    MINI_BAR("Mini Bar"),
    SPORT("Sport"),
    OTHER("Other");

    private final String displayName;

    ServiceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name();
    }
}

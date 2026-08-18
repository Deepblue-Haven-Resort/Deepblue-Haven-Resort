package deepbluehaven.pojo.enums;

public enum ServiceCategory {
    FOOD_BEVERAGE("Food Beverage"),
    FOOD("Food"),
    BEVERAGE("Beverage"),
    DINING("Dining"),
    LAUNDRY("Laundry"),
    SPA("Spa"),
    WELLNESS("Wellness"),
    FITNESS("Fitness"),
    TRANSPORT("Transport"),
    MINI_BAR("Mini Bar"),
    SPORT("Sport"),
    TOUR("Tour"),
    ENTERTAINMENT("Entertainment"),
    OTHER("Other");

    private final String displayName;

    ServiceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name();
    }
}

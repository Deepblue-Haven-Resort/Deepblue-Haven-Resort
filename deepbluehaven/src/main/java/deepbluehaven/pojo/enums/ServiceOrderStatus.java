package deepbluehaven.pojo.enums;

public enum ServiceOrderStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    IN_PROGRESS("In Progress"),
    DELIVERED("Delivered"),
    SERVED("Served"),
    READY("Ready"),
    COMPLETED("Completed"),
    PAID("Paid"),
    CANCELLED("Cancelled"),
    REJECTED("Rejected");

    private final String displayName;

    ServiceOrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name();
    }
}

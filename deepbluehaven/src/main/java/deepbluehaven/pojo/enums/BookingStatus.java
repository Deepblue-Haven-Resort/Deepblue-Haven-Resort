package deepbluehaven.pojo.enums;

public enum BookingStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    CHECKED_IN("Checked In"),
    CHECKED_OUT("Checked Out"),
    COMPLETED("Completed"),
    REJECTED("Rejected"),
    EXPIRED("Expired");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name();
    }
}
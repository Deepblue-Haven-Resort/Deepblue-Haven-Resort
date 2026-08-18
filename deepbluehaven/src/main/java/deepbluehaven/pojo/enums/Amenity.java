package deepbluehaven.pojo.enums;

public enum Amenity {
    WIFI("Free Wi-Fi"),
    SWIMMING_POOL("Swimming Pool"),
    GYM("Fitness Gym"),
    SPA("Spa & Wellness"),
    PARKING("Free Parking"),
    RESTAURANT("Resort Restaurant"),
    BAR("Lounge Bar"),
    ROOM_SERVICE("Room Service"),
    LAUNDRY("Laundry Service"),
    AIR_CONDITIONING("Air Conditioning"),
    MINI_BAR("Mini Bar"),
    SAFE_BOX("Safe Box"),
    BATHTUB("Luxury Bathtub"),
    BALCONY("Private Balcony"),
    OCEAN_VIEW("Ocean View"),
    SMART_TV("Smart TV"),
    COFFEE_MAKER("Coffee Maker"),
    HAIR_DRYER("Hair Dryer"),
    IRON("Iron & Board"),
    PET_FRIENDLY("Pet Friendly");

    private final String displayName;

    Amenity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
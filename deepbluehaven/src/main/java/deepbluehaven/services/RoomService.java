package deepbluehaven.services;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.AmenityViewDTO;
import deepbluehaven.dto.ResortFilterOptionDTO;
import deepbluehaven.dto.RoomCardViewDTO;
import deepbluehaven.pojo.Resort;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.enums.Amenity;
import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.pojo.enums.RoomTag;
import deepbluehaven.repositories.RoomRepository;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    private final List<RoomStatus> ACTIVE_STATUSES = List.of(
        RoomStatus.AVAILABLE, 
        RoomStatus.OCCUPIED, 
        RoomStatus.CLEANING
    );

    @Transactional(readOnly = true)
    public List<RoomCardViewDTO> getAvailableRoomCards() {
        return getAvailableRoomCards(java.util.Collections.emptySet());
    }

    @Transactional(readOnly = true)
    public List<RoomCardViewDTO> getAvailableRoomCards(java.util.Set<Long> favoriteRoomIds) {
        return roomRepository.findByStatusesWithResort(ACTIVE_STATUSES).stream()
                .map(room -> toCardView(room, favoriteRoomIds))
                .collect(Collectors.toList());
    }

    public List<RoomCardViewDTO> convertToCardViews(java.util.Collection<Room> rooms, java.util.Set<Long> favoriteRoomIds) {
        if (rooms == null) return new ArrayList<>();
        return rooms.stream().map(room -> toCardView(room, favoriteRoomIds)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResortFilterOptionDTO> getResortFilterOptions() {
        Map<String, String> uniqueResorts = new LinkedHashMap<>(); 

        roomRepository.findByStatusesWithResort(ACTIVE_STATUSES).forEach(room -> {
            Resort resort = room.getResort();
            uniqueResorts.putIfAbsent(slugify(resort.getName()), resort.getName());
        });

        return uniqueResorts.entrySet().stream().map(entry -> new ResortFilterOptionDTO(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public RoomCardViewDTO toCardView(Room room) {
        return toCardView(room, java.util.Collections.emptySet());
    }

    public RoomCardViewDTO toCardView(Room room, java.util.Set<Long> favoriteRoomIds) {
        RoomCardViewDTO view = new RoomCardViewDTO();
        Resort resort = room.getResort();

        view.setId(room.getId());
        view.setFavorite(favoriteRoomIds != null && favoriteRoomIds.contains(room.getId()));
        view.setImageUrl(room.getImages().isEmpty() ? "/assets/pic/room-placeholder.jpg" : room.getImages().get(0));
        view.setImageAlt(room.getRoomType().name() + " " + room.getRoomNumber());

        view.setBadge(""); 
        view.setRoomName(formatRoomTypeLabel(room.getRoomType()) + " - " + room.getRoomNumber());
        view.setLocation(resort != null ? (resort.getLocation() + ", " + resort.getName()) : "DeepBlue Haven");

        mapStatusToView(room.getStatus(), view);

        view.setBedInfo(room.getCapacity() + " Guests");
        view.setBathInfo(formatRoomTypeLabel(room.getRoomType()) + " Room");
        view.setSizeInfo(room.getArea() != null ? room.getArea() + "m²" : "-");

        view.setPriceText(formatVnd(room.getBasePrice()) + " VND");
        view.setPriceValue(room.getBasePrice() != null ? room.getBasePrice().longValue() : 0);

        view.setResortValue(resort != null ? slugify(resort.getName()) : "deepblue-haven");
        view.setTypeValue(room.getRoomType().name().toLowerCase());
        view.setGuestsValue(room.getCapacity() != null ? room.getCapacity() : 2);
        view.setViewValue(room.getTags().stream().map(this::tagToViewValue).collect(Collectors.joining(",")));

        view.setRatingValue(0); 
        view.setDetailUrl("/rooms/" + room.getId());

        return view;
    }

    private void mapStatusToView(RoomStatus status, RoomCardViewDTO view) {
        switch (status) {
            case AVAILABLE -> {
                view.setIcon("fa-check");
                view.setStatusText("Available");
                view.setTagClass("room-card__tag--blue");
            }
            case OCCUPIED -> {
                view.setIcon("fa-lock");
                view.setStatusText("Occupied");
                view.setTagClass("room-card__tag--gray"); 
            }
            case CLEANING -> {  
                view.setIcon("fa-broom");
                view.setStatusText("Cleaning");
                view.setTagClass("room-card__tag--gold"); 
            }
        }
    }

    private String formatRoomTypeLabel(deepbluehaven.pojo.enums.RoomType type) {
        String name = type.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private String tagToViewValue(RoomTag tag) {
        return switch (tag) {
            case GARDEN_VIEW -> "garden";
            case POOL_VIEW -> "pool";
            case OCEAN_VIEW -> "ocean";
            case PARTIAL_OCEAN_VIEW -> "partial-ocean";
            case BEACHFRONT -> "beachfront";
        };
    }

    private String formatVnd(java.math.BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount.longValue());
    }

    private String slugify(String input) {
        return input.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }

    @Transactional(readOnly = true)
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    private static final Map<Amenity, AmenityViewDTO> AMENITY_DISPLAY = new EnumMap<>(Amenity.class);
    static {
        AMENITY_DISPLAY.put(Amenity.WIFI, new AmenityViewDTO("fa-wifi", "High-speed Wi-Fi"));
        AMENITY_DISPLAY.put(Amenity.SWIMMING_POOL, new AmenityViewDTO("fa-person-swimming", "Swimming pool"));
        AMENITY_DISPLAY.put(Amenity.GYM, new AmenityViewDTO("fa-dumbbell", "Gym access"));
        AMENITY_DISPLAY.put(Amenity.SPA, new AmenityViewDTO("fa-spa", "Spa access"));
        AMENITY_DISPLAY.put(Amenity.PARKING, new AmenityViewDTO("fa-square-parking", "Parking"));
        AMENITY_DISPLAY.put(Amenity.RESTAURANT, new AmenityViewDTO("fa-utensils", "Restaurant access"));
        AMENITY_DISPLAY.put(Amenity.BAR, new AmenityViewDTO("fa-martini-glass", "Bar access"));
        AMENITY_DISPLAY.put(Amenity.ROOM_SERVICE, new AmenityViewDTO("fa-bell-concierge", "Room service"));
        AMENITY_DISPLAY.put(Amenity.LAUNDRY, new AmenityViewDTO("fa-shirt", "Laundry service"));
        AMENITY_DISPLAY.put(Amenity.AIR_CONDITIONING, new AmenityViewDTO("fa-snowflake", "Air conditioning"));
        AMENITY_DISPLAY.put(Amenity.MINI_BAR, new AmenityViewDTO("fa-martini-glass-citrus", "Mini bar"));
        AMENITY_DISPLAY.put(Amenity.SAFE_BOX, new AmenityViewDTO("fa-vault", "Safety box"));
        AMENITY_DISPLAY.put(Amenity.BATHTUB, new AmenityViewDTO("fa-bath", "Bathtub"));
        AMENITY_DISPLAY.put(Amenity.BALCONY, new AmenityViewDTO("fa-door-open", "Private balcony"));
        AMENITY_DISPLAY.put(Amenity.OCEAN_VIEW, new AmenityViewDTO("fa-water", "Ocean view"));
        AMENITY_DISPLAY.put(Amenity.SMART_TV, new AmenityViewDTO("fa-tv", "Smart TV"));
        AMENITY_DISPLAY.put(Amenity.COFFEE_MAKER, new AmenityViewDTO("fa-mug-hot", "Coffee maker"));
        AMENITY_DISPLAY.put(Amenity.HAIR_DRYER, new AmenityViewDTO("fa-wind", "Hair dryer"));
        AMENITY_DISPLAY.put(Amenity.IRON, new AmenityViewDTO("fa-shirt", "Iron"));
        AMENITY_DISPLAY.put(Amenity.PET_FRIENDLY, new AmenityViewDTO("fa-paw", "Pet friendly"));
    }
 
    public List<AmenityViewDTO> getAmenityViews(Room room) {
        return room.getAmenities().stream()
                .map(AMENITY_DISPLAY::get)
                .collect(Collectors.toList());
    }
}
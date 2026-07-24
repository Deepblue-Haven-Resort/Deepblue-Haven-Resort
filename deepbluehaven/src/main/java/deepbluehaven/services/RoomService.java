package deepbluehaven.services;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.ResortFilterOptionDTO;
import deepbluehaven.dto.RoomCardViewDTO;
import deepbluehaven.pojo.Resort;
import deepbluehaven.pojo.Room;
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
        return roomRepository.findByStatusesWithResort(ACTIVE_STATUSES).stream().map(this::toCardView).collect(Collectors.toList());
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

    private RoomCardViewDTO toCardView(Room room) {
        RoomCardViewDTO view = new RoomCardViewDTO();
        Resort resort = room.getResort();

        view.setImageUrl(room.getImages().isEmpty() ? "/assets/pic/room-placeholder.jpg" : room.getImages().get(0));
        view.setImageAlt(room.getRoomType().name() + " " + room.getRoomNumber());

        view.setBadge(""); 
        view.setRoomName(formatRoomTypeLabel(room.getRoomType()) + " - " + room.getRoomNumber());
        view.setLocation(resort.getLocation() + ", " + resort.getName());

        mapStatusToView(room.getStatus(), view);

        view.setBedInfo(room.getCapacity() + " Guests");
        view.setBathInfo(formatRoomTypeLabel(room.getRoomType()) + " Room");
        view.setSizeInfo(room.getArea() != null ? room.getArea() + "m²" : "-");

        view.setPriceText(formatVnd(room.getBasePrice()) + " VND");
        view.setPriceValue(room.getBasePrice().longValue());

        view.setResortValue(slugify(resort.getName()));
        view.setTypeValue(room.getRoomType().name().toLowerCase());
        view.setGuestsValue(room.getCapacity());
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
}
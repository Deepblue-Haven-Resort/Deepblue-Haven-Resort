package deepbluehaven.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.CustomerProfileDTO;
import deepbluehaven.dto.RoomCardViewDTO;
import deepbluehaven.dto.ServiceDTO;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.CustomerLoyaltyLog;
import deepbluehaven.pojo.CustomerProfile;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.enums.ServiceStatus;
import deepbluehaven.repositories.CustomerLoyaltyLogRepository;
import deepbluehaven.repositories.CustomerProfileRepository;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.repositories.ServiceRepository;

@Service
public class CustomerService {

    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final CustomerLoyaltyLogRepository customerLoyaltyLogRepository;

    public CustomerService(ServiceRepository serviceRepository,
                           CustomerRepository customerRepository,
                           CustomerProfileRepository customerProfileRepository,
                           RoomRepository roomRepository,
                           RoomService roomService,
                           CustomerLoyaltyLogRepository customerLoyaltyLogRepository) {
        this.serviceRepository = serviceRepository;
        this.customerRepository = customerRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.roomRepository = roomRepository;
        this.roomService = roomService;
        this.customerLoyaltyLogRepository = customerLoyaltyLogRepository;
    }

    @Transactional(readOnly = true)
    public CustomerProfileDTO.Response getCustomerProfile(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        CustomerProfile profile = customer.getProfile();
        if (profile == null) {
            profile = new CustomerProfile();
            profile.setCustomer(customer);
            profile.setFullName(customer.getUsername());
            profile.setEmail("customer@deepbluehaven.com");
            profile.setPhoneNumber("—");
            customer.setProfile(profile);
            customerRepository.save(customer);
        }
        return toProfileResponse(profile);
    }

    @Transactional
    public CustomerProfileDTO.Response updateCustomerProfile(Long customerId, CustomerProfileDTO.Request req) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        CustomerProfile profile = customer.getProfile();
        if (profile == null) {
            profile = new CustomerProfile();
            profile.setCustomer(customer);
            profile.setFullName(customer.getUsername());
            customer.setProfile(profile);
        }

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            profile.setFullName(req.getFullName().trim());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            profile.setEmail(req.getEmail().trim());
        }
        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()) {
            profile.setPhoneNumber(req.getPhoneNumber().trim());
        }
        if (req.getBirthDay() != null) {
            profile.setBirthDay(req.getBirthDay());
        }
        if (req.getAvatarUrl() != null && !req.getAvatarUrl().isBlank()) {
            profile.setAvatarUrl(req.getAvatarUrl().trim());
        }

        customerProfileRepository.save(profile);
        return toProfileResponse(profile);
    }

    private CustomerProfileDTO.Response toProfileResponse(CustomerProfile profile) {
        CustomerProfileDTO.Response dto = new CustomerProfileDTO.Response();
        dto.setCustomerId(profile.getCustomerId());
        dto.setFullName(profile.getFullName() != null ? profile.getFullName() : profile.getCustomer().getUsername());
        dto.setEmail(profile.getEmail() != null ? profile.getEmail() : "");
        dto.setPhoneNumber(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "");
        dto.setAvatarUrl(profile.getAvatarUrl() != null ? profile.getAvatarUrl() : "https://res.cloudinary.com/xio0mgix/image/upload/v1786687334/53cfbdcb-9c58-471c-96ab-cddf0c65f52e.png");
        dto.setBirthDay(profile.getBirthDay());
        dto.setTotalBookings(profile.getTotalBookings() != null ? profile.getTotalBookings() : 0);
        dto.setTotalSpent(profile.getTotalSpent() != null ? profile.getTotalSpent() : BigDecimal.ZERO);
        dto.setTotalPoints(profile.getTotalPoints() != null ? profile.getTotalPoints() : 0);
        dto.setSegment(profile.getMembershipTier() != null && profile.getMembershipTier().getTierName() != null ? profile.getMembershipTier().getTierName().name() : (profile.getSegment() != null ? profile.getSegment() : "Blue Member"));
        if (profile.getMembershipTier() != null) {
            dto.setMembershipTierId(profile.getMembershipTier().getId());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public Set<Long> getFavoriteRoomIds(Long customerId) {
        if (customerId == null) return Collections.emptySet();
        return customerRepository.findByIdWithFavoriteRooms(customerId)
                .map(c -> c.getFavoriteRooms().stream().map(Room::getId).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @Transactional(readOnly = true)
    public Set<Long> getFavoriteServiceIds(Long customerId) {
        if (customerId == null) return Collections.emptySet();
        return customerRepository.findByIdWithFavoriteServices(customerId)
                .map(c -> c.getFavoriteServices().stream().map(deepbluehaven.pojo.Service::getId).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @Transactional
    public boolean toggleFavoriteRoom(Long customerId, Long roomId) {
        Customer customer = customerRepository.findByIdWithFavoriteRooms(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        boolean removed = customer.getFavoriteRooms().removeIf(r -> r.getId().equals(roomId));
        if (!removed) {
            customer.getFavoriteRooms().add(room);
        }
        customerRepository.save(customer);
        return !removed;
    }

    @Transactional
    public boolean toggleFavoriteService(Long customerId, Long serviceId) {
        Customer customer = customerRepository.findByIdWithFavoriteServices(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        deepbluehaven.pojo.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        boolean removed = customer.getFavoriteServices().removeIf(s -> s.getId().equals(serviceId));
        if (!removed) {
            customer.getFavoriteServices().add(service);
        }
        customerRepository.save(customer);
        return !removed;
    }

    @Transactional(readOnly = true)
    public List<RoomCardViewDTO> getFavoriteRooms(Long customerId) {
        if (customerId == null) return Collections.emptyList();
        Set<Long> favRoomIds = getFavoriteRoomIds(customerId);
        return customerRepository.findByIdWithFavoriteRooms(customerId)
                .map(c -> roomService.convertToCardViews(c.getFavoriteRooms(), favRoomIds))
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO.Response> getFavoriteServices(Long customerId) {
        if (customerId == null) return Collections.emptyList();
        Set<Long> favServiceIds = getFavoriteServiceIds(customerId);
        return customerRepository.findByIdWithFavoriteServices(customerId)
                .map(c -> c.getFavoriteServices().stream().map(s -> toResponse(s, favServiceIds)).toList())
                .orElse(Collections.emptyList());
    }

    public List<ServiceDTO.Response> getAllActiveServices() {
        return getAllActiveServices(Collections.emptySet());
    }

    public List<ServiceDTO.Response> getAllActiveServices(Set<Long> favoriteServiceIds) {
        List<deepbluehaven.pojo.Service> entities = serviceRepository.findByStatus(ServiceStatus.ACTIVE);
        return entities.stream().map(s -> toResponse(s, favoriteServiceIds)).toList();
    }

    public List<ServiceDTO.Response> getAllOutOfStockServices() {
        return getAllOutOfStockServices(Collections.emptySet());
    }

    public List<ServiceDTO.Response> getAllOutOfStockServices(Set<Long> favoriteServiceIds) {
        List<deepbluehaven.pojo.Service> entities = serviceRepository.findByStatus(ServiceStatus.OUT_OF_STOCK);
        return entities.stream().map(s -> toResponse(s, favoriteServiceIds)).toList();
    }

    public List<ServiceDTO.Response> getVisibleServices() {
        return getVisibleServices(Collections.emptySet());
    }

    public List<ServiceDTO.Response> getVisibleServices(Set<Long> favoriteServiceIds) {
        List<ServiceDTO.Response> result = new ArrayList<>();
        result.addAll(getAllActiveServices(favoriteServiceIds));
        result.addAll(getAllOutOfStockServices(favoriteServiceIds));
        return result;
    }

    private ServiceDTO.Response toResponse(deepbluehaven.pojo.Service entity) {
        return toResponse(entity, Collections.emptySet());
    }

    private ServiceDTO.Response toResponse(deepbluehaven.pojo.Service entity, Set<Long> favoriteServiceIds) {
        ServiceDTO.Response dto = new ServiceDTO.Response();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType());
        dto.setCategory(entity.getCategory());
        dto.setCategoryDisplayName(entity.getCategory() != null ? entity.getCategory().getDisplayName() : "Other");
        dto.setBasePrice(entity.getBasePrice());
        dto.setUnit(entity.getUnit());
        dto.setImages(entity.getImages() != null ? entity.getImages() : new ArrayList<>());
        dto.setStatus(entity.getStatus());
        boolean isAvailable = (entity.getStatus() != null && entity.getStatus().name().equals("ACTIVE"));
        dto.setStatusClass(isAvailable ? "service-card__status--available" : "service-card__status--out-of-stock");
        dto.setStatusValue(isAvailable ? "active" : "out-of-stock");
        dto.setFavorite(favoriteServiceIds != null && favoriteServiceIds.contains(entity.getId()));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CustomerLoyaltyLog> getLoyaltyLogsByCustomer(Long customerId) {
        return customerLoyaltyLogRepository.findByCustomerIdOrderByTimestampDesc(customerId);
    }
}

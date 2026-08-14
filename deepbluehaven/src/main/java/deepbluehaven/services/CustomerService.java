package deepbluehaven.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.CustomerProfileDTO;
import deepbluehaven.dto.ServiceDTO;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.CustomerProfile;
import deepbluehaven.pojo.enums.ServiceStatus;
import deepbluehaven.repositories.CustomerProfileRepository;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.ServiceRepository;

@Service
public class CustomerService {

    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final CustomerProfileRepository customerProfileRepository;

    public CustomerService(ServiceRepository serviceRepository,
                           CustomerRepository customerRepository,
                           CustomerProfileRepository customerProfileRepository) {
        this.serviceRepository = serviceRepository;
        this.customerRepository = customerRepository;
        this.customerProfileRepository = customerProfileRepository;
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

    public List<ServiceDTO.Response> getAllActiveServices() {
        List<deepbluehaven.pojo.Service> entities = serviceRepository.findByStatus(ServiceStatus.ACTIVE);
        return entities.stream().map(this::toResponse).toList();
    }

    public List<ServiceDTO.Response> getAllOutOfStockServices() {
        List<deepbluehaven.pojo.Service> entities = serviceRepository.findByStatus(ServiceStatus.OUT_OF_STOCK);
        return entities.stream().map(this::toResponse).toList();
    }

    public List<ServiceDTO.Response> getVisibleServices() {
        List<ServiceDTO.Response> result = new ArrayList<>();
        result.addAll(getAllActiveServices());
        result.addAll(getAllOutOfStockServices());
        return result;
    }

    private ServiceDTO.Response toResponse(deepbluehaven.pojo.Service entity) {
        ServiceDTO.Response dto = new ServiceDTO.Response();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType());
        dto.setCategory(entity.getCategory());
        dto.setBasePrice(entity.getBasePrice());
        dto.setUnit(entity.getUnit());
        dto.setImages(entity.getImages() != null ? entity.getImages() : new ArrayList<>());
        dto.setStatus(entity.getStatus());
        boolean isAvailable = (entity.getStatus() != null && entity.getStatus().name().equals("ACTIVE"));
        dto.setStatusClass(isAvailable ? "service-card__status--available" : "service-card__status--out-of-stock");
        dto.setStatusValue(isAvailable ? "active" : "out-of-stock");
        return dto;
    }
}

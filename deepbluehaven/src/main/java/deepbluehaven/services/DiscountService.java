package deepbluehaven.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.DiscountDTO;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.CustomerDiscount;
import deepbluehaven.pojo.Discount;
import deepbluehaven.pojo.enums.CustomerDiscountStatus;
import deepbluehaven.pojo.enums.DiscountType;
import deepbluehaven.pojo.enums.NotificationType;
import deepbluehaven.repositories.CustomerDiscountRepository;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.DiscountRepository;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final CustomerDiscountRepository customerDiscountRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public DiscountService(DiscountRepository discountRepository,
                           CustomerDiscountRepository customerDiscountRepository,
                           CustomerRepository customerRepository,
                           NotificationService notificationService) {
        this.discountRepository = discountRepository;
        this.customerDiscountRepository = customerDiscountRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Map<String, Object> claimVoucher(Long customerId, String code) {
        if (customerId == null) {
            return Map.of("success", false, "message", "Please log in to claim vouchers");
        }
        if (code == null || code.isBlank()) {
            return Map.of("success", false, "message", "Invalid voucher code");
        }

        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            return Map.of("success", false, "message", "Customer account not found");
        }

        Discount discount = discountRepository.findByCode(code.trim().toUpperCase()).orElse(null);
        if (discount == null || !Boolean.TRUE.equals(discount.getIsActive())) {
            return Map.of("success", false, "message", "Voucher is invalid or has expired");
        }

        if (discount.getEndDate() != null && discount.getEndDate().isBefore(java.time.LocalDate.now())) {
            return Map.of("success", false, "message", "This voucher expired on " + discount.getEndDate());
        }

        if (discount.getUsageLimit() != null && discount.getUsageCount() != null && discount.getUsageCount() >= discount.getUsageLimit()) {
            return Map.of("success", false, "message", "This voucher has reached its maximum global usage limit");
        }

        Optional<CustomerDiscount> existing = customerDiscountRepository.findByCustomerIdAndDiscountId(customerId, discount.getId());
        if (existing.isPresent()) {
            CustomerDiscount cd = existing.get();
            if (cd.getStatus() == CustomerDiscountStatus.AVAILABLE) {
                return Map.of("success", false, "message", "You have already claimed this voucher in your wallet!");
            }
        }

        CustomerDiscount cd = new CustomerDiscount();
        cd.setCustomer(customer);
        cd.setDiscount(discount);
        cd.setStatus(CustomerDiscountStatus.AVAILABLE);
        cd.setAcquiredAt(LocalDateTime.now());
        customerDiscountRepository.save(cd);

        notificationService.createCustomerNotification(
                customer,
                "Voucher Claimed: " + discount.getCode(),
                "You have successfully claimed voucher " + discount.getCode() + " (" + discount.getDescription() + "). Use it at checkout!",
                NotificationType.PROMOTION,
                "/profile"
        );

        return Map.of("success", true, "message", "Voucher " + discount.getCode() + " claimed successfully! Check your profile wallet.");
    }

    public List<DiscountDTO.Response> getActiveOffers() {
        return discountRepository.findByIsActiveTrue().stream().map(this::toResponse).toList();
    }

    public java.util.Optional<DiscountDTO.Response> getOfferByCode(String code) {
        return discountRepository.findByCode(code).map(this::toResponse);
    }

    private DiscountDTO.Response toResponse(Discount entity) {
        DiscountDTO.Response dto = new DiscountDTO.Response();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setType(entity.getType());
        dto.setDiscountValue(entity.getDiscountValue());
        dto.setDescription(entity.getDescription());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setMinValueService(entity.getMinValueService());
        dto.setUsageLimit(entity.getUsageLimit());
        dto.setUsageCount(entity.getUsageCount());
        dto.setLimitPerUser(entity.getLimitPerUser());
        dto.setIsActive(entity.getIsActive());
        dto.setIsStackable(entity.getIsStackable());

        if (entity.getType() == DiscountType.PERCENTAGE) {
            dto.setBadgeText(entity.getDiscountValue().stripTrailingZeros().toPlainString() + "% OFF");
            dto.setBadgeClass("offer-card__badge--primary");
        } else {
            dto.setBadgeText(entity.getDiscountValue().stripTrailingZeros().toPlainString() + " VND OFF");
            dto.setBadgeClass("offer-card__badge--gold");
        }

        if (entity.getEndDate() != null) {
            dto.setValidityText("Valid until " + entity.getEndDate().format(DATE_FORMATTER));
        } else {
            dto.setValidityText("Limited time offer");
        }

        if (entity.getMinValueService() != null) {
            dto.setPriceText(String.format("%,d VND", entity.getMinValueService().longValue()));
        } else {
            dto.setPriceText("Contact Us");
        }

        if (entity.getRoomType() != null && !entity.getRoomType().isBlank()) {
            dto.setCategory("rooms");
            dto.setCategoryLabel("Room Offer - " + entity.getRoomType());
            dto.setCategoryIcon("fa-bed");
        } else {
            dto.setCategory("experiences");
            dto.setCategoryLabel("Special Experience");
            dto.setCategoryIcon("fa-umbrella-beach");
        }

        String search = (entity.getCode() + " " + (entity.getDescription() != null ? entity.getDescription() : "") + " " + (entity.getRoomType() != null ? entity.getRoomType() : "")).toLowerCase();
        dto.setSearchContent(search);

        return dto;
    }
}

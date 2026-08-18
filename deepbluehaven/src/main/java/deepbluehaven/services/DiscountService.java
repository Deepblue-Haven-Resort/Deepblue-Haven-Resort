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
        if (entity == null) {
            return null;
        }
        DiscountDTO.Response dto = new DiscountDTO.Response();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode() != null ? entity.getCode() : "OFFER");
        dto.setType(entity.getType() != null ? entity.getType() : DiscountType.PERCENTAGE);
        dto.setDiscountValue(entity.getDiscountValue() != null ? entity.getDiscountValue() : java.math.BigDecimal.ZERO);
        dto.setDescription(entity.getDescription() != null ? entity.getDescription() : "Special Deep Blue Haven Resort Promotion");
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setMinValueService(entity.getMinValueService());
        dto.setUsageLimit(entity.getUsageLimit());
        dto.setUsageCount(entity.getUsageCount() != null ? entity.getUsageCount() : 0);
        dto.setLimitPerUser(entity.getLimitPerUser() != null ? entity.getLimitPerUser() : 1);
        dto.setIsActive(Boolean.TRUE.equals(entity.getIsActive()));
        dto.setIsStackable(Boolean.TRUE.equals(entity.getIsStackable()));

        if (dto.getType() == DiscountType.PERCENTAGE) {
            String val = dto.getDiscountValue() != null ? dto.getDiscountValue().stripTrailingZeros().toPlainString() : "0";
            dto.setBadgeText(val + "% OFF");
            dto.setBadgeClass("offer-card__badge--primary");
        } else {
            String val = dto.getDiscountValue() != null ? String.format("%,d", dto.getDiscountValue().longValue()) : "0";
            dto.setBadgeText(val + " VND OFF");
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

        String codeUpper = entity.getCode() != null ? entity.getCode().toUpperCase() : "";
        if (entity.getRoomType() != null && !entity.getRoomType().isBlank()) {
            dto.setCategory("rooms");
            dto.setCategoryLabel("Room Offer - " + entity.getRoomType());
            dto.setCategoryIcon("fa-bed");
            dto.setImageUrl("https://res.cloudinary.com/xio0mgix/image/upload/v1786686618/1f196b65-2daa-49b5-9a6c-f1e5e1d50a6e.png");
        } else if (codeUpper.contains("SPA") || codeUpper.contains("RELAX") || codeUpper.contains("MASSAGE")) {
            dto.setCategory("spa");
            dto.setCategoryLabel("Spa & Wellness Offer");
            dto.setCategoryIcon("fa-spa");
            dto.setImageUrl("https://res.cloudinary.com/xio0mgix/image/upload/v1786686824/f9b9112d-047b-4756-a23d-95fd3d7dd478.png");
        } else if (codeUpper.contains("DINE") || codeUpper.contains("FOOD") || codeUpper.contains("BUFFET") || codeUpper.contains("RESTAURANT")) {
            dto.setCategory("dining");
            dto.setCategoryLabel("Dining & Cuisine Offer");
            dto.setCategoryIcon("fa-utensils");
            dto.setImageUrl("https://res.cloudinary.com/xio0mgix/image/upload/v1786686829/9ce92d78-c98f-4a75-854a-50a54bc99a48.png");
        } else if (codeUpper.contains("VIP") || codeUpper.contains("LUXURY") || codeUpper.contains("PRESIDENT")) {
            dto.setCategory("rooms");
            dto.setCategoryLabel("VIP Executive Stay");
            dto.setCategoryIcon("fa-crown");
            dto.setImageUrl("https://res.cloudinary.com/xio0mgix/image/upload/v1786686505/937e83ec-1217-4027-8c10-0b204c14631c.png");
        } else {
            dto.setCategory("experiences");
            dto.setCategoryLabel("Special Experience");
            dto.setCategoryIcon("fa-umbrella-beach");
            dto.setImageUrl("https://res.cloudinary.com/xio0mgix/image/upload/v1786686614/56d6bc0e-045d-4c53-8635-32d2ecc0b840.png");
        }

        String search = (dto.getCode() + " " + dto.getDescription() + " " + (entity.getRoomType() != null ? entity.getRoomType() : "")).toLowerCase();
        dto.setSearchContent(search);

        return dto;
    }
}

package deepbluehaven.services;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import deepbluehaven.dto.DiscountDTO;
import deepbluehaven.pojo.Discount;
import deepbluehaven.pojo.enums.DiscountType;
import deepbluehaven.repositories.DiscountRepository;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
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

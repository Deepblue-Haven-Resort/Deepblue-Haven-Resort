package deepbluehaven.dto;

import deepbluehaven.pojo.enums.TierStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class MembershipTierDTO {

    public static class Request {
        @NotNull(message = "Tier name is required")
        private TierStatus tierName;

        @NotNull(message = "Minimum points are required")
        @Min(value = 0, message = "Minimum points cannot be negative")
        private Integer minPoints;

        @NotNull(message = "Point multiplier is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Point multiplier must be greater than 0")
        private BigDecimal pointMultiplier;

        @NotNull(message = "Discount rate is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Discount rate cannot be negative")
        private BigDecimal discountRate;

        @NotNull(message = "Priority duration is required")
        @Min(value = 0, message = "Priority duration cannot be negative")
        private Integer priorityDuration;

        private String description;

        public Request() {}

        public TierStatus getTierName() { 
            return tierName; }
        public void setTierName(TierStatus tierName) { 
            this.tierName = tierName; }

        public Integer getMinPoints() { 
            return minPoints; }
        public void setMinPoints(Integer minPoints) { 
            this.minPoints = minPoints; }

        public BigDecimal getPointMultiplier() { 
            return pointMultiplier; }
        public void setPointMultiplier(BigDecimal pointMultiplier) { 
            this.pointMultiplier = pointMultiplier; }

        public BigDecimal getDiscountRate() { 
            return discountRate; }
        public void setDiscountRate(BigDecimal discountRate) { 
            this.discountRate = discountRate; }

        public Integer getPriorityDuration() { 
            return priorityDuration; }
        public void setPriorityDuration(Integer priorityDuration) { 
            this.priorityDuration = priorityDuration; }

        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }
    }

    public static class Response {
        private Long id;
        private TierStatus tierName;
        private Integer minPoints;
        private BigDecimal pointMultiplier;
        private BigDecimal discountRate;
        private Integer priorityDuration;
        private String description;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) {
             this.id = id; }

        public TierStatus getTierName() { 
            return tierName; }
        public void setTierName(TierStatus tierName) { 
            this.tierName = tierName; }

        public Integer getMinPoints() { 
            return minPoints; }
        public void setMinPoints(Integer minPoints) { 
            this.minPoints = minPoints; }

        public BigDecimal getPointMultiplier() { 
            return pointMultiplier; }
        public void setPointMultiplier(BigDecimal pointMultiplier) { 
            this.pointMultiplier = pointMultiplier; }

        public BigDecimal getDiscountRate() { 
            return discountRate; }
        public void setDiscountRate(BigDecimal discountRate) { 
            this.discountRate = discountRate; }

        public Integer getPriorityDuration() { 
            return priorityDuration; }
        public void setPriorityDuration(Integer priorityDuration) { 
            this.priorityDuration = priorityDuration; }

        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }
    }
}

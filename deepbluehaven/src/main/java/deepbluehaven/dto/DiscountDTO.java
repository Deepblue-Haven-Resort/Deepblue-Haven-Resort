package deepbluehaven.dto;
import deepbluehaven.pojo.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DiscountDTO {

    public static class Request {
        @NotBlank(message = "Code is required")
        private String code;

        @NotNull(message = "Discount type is required")
        private DiscountType type;

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than 0")
        private BigDecimal discountValue;

        private String description;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        private BigDecimal minValueService;

        @NotNull(message = "Usage limit is required")
        @Min(value = 1, message = "Usage limit must be at least 1")
        private Integer usageLimit;

        @NotNull(message = "Limit per user is required")
        @Min(value = 1, message = "Limit per user must be at least 1")
        private Integer limitPerUser;

        private Long membershipTierId;
        private String roomType;

        @NotNull(message = "isActive flag is required")
        private Boolean isActive;

        @NotNull(message = "isStackable flag is required")
        private Boolean isStackable;

        @NotNull(message = "Created by Worker ID is required")
        private Long createdById;

        public Request() {}

        public String getCode() { 
            return code; }
        public void setCode(String code) { 
            this.code = code; }

        public DiscountType getType() { 
            return type; }
        public void setType(DiscountType type) { 
            this.type = type; }

        public BigDecimal getDiscountValue() { 
            return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { 
            this.discountValue = discountValue; }

        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }

        public LocalDate getStartDate() { 
            return startDate; }
        public void setStartDate(LocalDate startDate) { 
            this.startDate = startDate; }

        public LocalDate getEndDate() { 
            return endDate; }
        public void setEndDate(LocalDate endDate) { 
            this.endDate = endDate; }

        public BigDecimal getMinValueService() { 
            return minValueService; }
        public void setMinValueService(BigDecimal minValueService) { 
            this.minValueService = minValueService; }

        public Integer getUsageLimit() { 
            return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { 
            this.usageLimit = usageLimit; }

        public Integer getLimitPerUser() { 
            return limitPerUser; }
        public void setLimitPerUser(Integer limitPerUser) { 
            this.limitPerUser = limitPerUser; }

        public Long getMembershipTierId() { 
            return membershipTierId; }
        public void setMembershipTierId(Long membershipTierId) { 
            this.membershipTierId = membershipTierId; }

        public String getRoomType() { 
            return roomType; }
        public void setRoomType(String roomType) { 
            this.roomType = roomType; }

        public Boolean getIsActive() { 
            return isActive; }
        public void setIsActive(Boolean isActive) { 
            this.isActive = isActive; }

        public Boolean getIsStackable() { 
            return isStackable; }
        public void setIsStackable(Boolean isStackable) { 
            this.isStackable = isStackable; }

        public Long getCreatedById() { 
            return createdById; }
        public void setCreatedById(Long createdById) { 
            this.createdById = createdById; }
    }

    public static class Response {
        private Long id;
        private String code;
        private DiscountType type;
        private BigDecimal discountValue;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal minValueService;
        private Integer usageLimit;
        private Integer usageCount;
        private Integer limitPerUser;
        private Long membershipTierId;
        private String roomType;
        private Boolean isActive;
        private Boolean isStackable;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long createdById;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public String getCode() { 
            return code; }
        public void setCode(String code) { 
            this.code = code; }

        public DiscountType getType() { 
            return type; }
        public void setType(DiscountType type) { 
            this.type = type; }

        public BigDecimal getDiscountValue() { 
            return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { 
            this.discountValue = discountValue; }

        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }

        public LocalDate getStartDate() { 
            return startDate; }
        public void setStartDate(LocalDate startDate) { 
            this.startDate = startDate; }

        public LocalDate getEndDate() { 
            return endDate; }
        public void setEndDate(LocalDate endDate) { 
            this.endDate = endDate; }

        public BigDecimal getMinValueService() { 
            return minValueService; }
        public void setMinValueService(BigDecimal minValueService) { 
            this.minValueService = minValueService; }

        public Integer getUsageLimit() { 
            return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { 
            this.usageLimit = usageLimit; }

        public Integer getUsageCount() { 
            return usageCount; }
        public void setUsageCount(Integer usageCount) { 
            this.usageCount = usageCount; }

        public Integer getLimitPerUser() { 
            return limitPerUser; }
        public void setLimitPerUser(Integer limitPerUser) { 
            this.limitPerUser = limitPerUser; }

        public Long getMembershipTierId() { 
            return membershipTierId; }
        public void setMembershipTierId(Long membershipTierId) { 
            this.membershipTierId = membershipTierId; }

        public String getRoomType() { 
            return roomType; }
        public void setRoomType(String roomType) { 
            this.roomType = roomType; }

        public Boolean getIsActive() { 
            return isActive; }
        public void setIsActive(Boolean isActive) { 
            this.isActive = isActive; }

        public Boolean getIsStackable() { 
            return isStackable; }
        public void setIsStackable(Boolean isStackable) { 
            this.isStackable = isStackable; }

        public LocalDateTime getCreatedAt() { 
            return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { 
            this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { 
            return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { 
            this.updatedAt = updatedAt; }

        public Long getCreatedById() { 
            return createdById; }
        public void setCreatedById(Long createdById) { 
            this.createdById = createdById; }
    }
}
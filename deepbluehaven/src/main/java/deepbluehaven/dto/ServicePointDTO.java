package deepbluehaven.dto;

import deepbluehaven.pojo.enums.CalculationType;
import deepbluehaven.pojo.enums.ServiceCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ServicePointDTO {

    public static class Request {
        @NotNull(message = "Service category is required")
        private ServiceCategory serviceCategory;

        @NotNull(message = "Calculation type is required")
        private CalculationType calculationType;

        @Min(value = 0, message = "Fixed points cannot be negative")
        private Integer fixedPoints;

        @DecimalMin(value = "0.0", inclusive = true, message = "Reward percentage cannot be negative")
        private BigDecimal rewardPercentage;

        @NotNull(message = "isActive flag is required")
        private Boolean isActive;

        public Request() {}

        public ServiceCategory getServiceCategory() { 
            return serviceCategory; }
        public void setServiceCategory(ServiceCategory serviceCategory) { 
            this.serviceCategory = serviceCategory; }

        public CalculationType getCalculationType() { 
            return calculationType; }
        public void setCalculationType(CalculationType calculationType) { 
            this.calculationType = calculationType; }

        public Integer getFixedPoints() { 
            return fixedPoints; }
        public void setFixedPoints(Integer fixedPoints) { 
            this.fixedPoints = fixedPoints; }

        public BigDecimal getRewardPercentage() { 
            return rewardPercentage; }
        public void setRewardPercentage(BigDecimal rewardPercentage) { 
            this.rewardPercentage = rewardPercentage; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) {
            this.isActive = isActive; }
    }

    public static class Response {
        private Long id;
        private ServiceCategory serviceCategory;
        private CalculationType calculationType;
        private Integer fixedPoints;
        private BigDecimal rewardPercentage;
        private Boolean isActive;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) {
             this.id = id; }

        public ServiceCategory getServiceCategory() { 
            return serviceCategory; }
        public void setServiceCategory(ServiceCategory serviceCategory) { 
            this.serviceCategory = serviceCategory; }

        public CalculationType getCalculationType() { 
            return calculationType; }
        public void setCalculationType(CalculationType calculationType) { 
            this.calculationType = calculationType; }

        public Integer getFixedPoints() { 
            return fixedPoints; }
        public void setFixedPoints(Integer fixedPoints) { 
            this.fixedPoints = fixedPoints; }

        public BigDecimal getRewardPercentage() { 
            return rewardPercentage; }
        public void setRewardPercentage(BigDecimal rewardPercentage) { 
            this.rewardPercentage = rewardPercentage; }

        public Boolean getIsActive() { 
            return isActive; }
        public void setIsActive(Boolean isActive) {
            this.isActive = isActive; }
    }
}

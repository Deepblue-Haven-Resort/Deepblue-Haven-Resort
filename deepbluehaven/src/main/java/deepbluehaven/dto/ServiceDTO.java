package deepbluehaven.dto;

import deepbluehaven.pojo.enums.ServiceCategory;
import deepbluehaven.pojo.enums.ServiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ServiceDTO {

    public static class Request {
        @NotNull(message = "Resort ID is required")
        private Long resortId;

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name cannot exceed 150 characters")
        private String name;

        private String description;

        @NotBlank(message = "Type is required")
        @Size(max = 100, message = "Type cannot exceed 100 characters")
        private String type;

        @NotNull(message = "Category is required")
        private ServiceCategory category;

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Base price cannot be negative")
        private BigDecimal basePrice;

        @NotBlank(message = "Unit is required")
        @Size(max = 50, message = "Unit cannot exceed 50 characters")
        private String unit;

        @NotNull(message = "Status is required")
        private ServiceStatus status;

        public Request() {}

        public Long getResortId() { 
            return resortId; }
        public void setResortId(Long resortId) { 
            this.resortId = resortId; }

        public String getName() { 
            return name; }
        public void setName(String name) { 
            this.name = name; }

        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }

        public String getType() { 
            return type; }
        public void setType(String type) { 
            this.type = type; }

        public ServiceCategory getCategory() { 
            return category; }
        public void setCategory(ServiceCategory category) { 
            this.category = category; }

        public BigDecimal getBasePrice() { 
            return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { 
            this.basePrice = basePrice; }

        public String getUnit() { 
            return unit; }
        public void setUnit(String unit) { 
            this.unit = unit; }

        public ServiceStatus getStatus() { 
            return status; }
        public void setStatus(ServiceStatus status) { 
            this.status = status; }
    }

    public static class Response {
        private Long id;
        private Long resortId;
        private String name;
        private String description;
        private String type;
        private ServiceCategory category;
        private BigDecimal basePrice;
        private String unit;
        private ServiceStatus status;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getResortId() { 
            return resortId; }
        public void setResortId(Long resortId) { 
            this.resortId = resortId; }

        public String getName() { 
            return name; }
        public void setName(String name) { 
            this.name = name; }

        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }

        public String getType() { 
            return type; }
        public void setType(String type) { 
            this.type = type; }

        public ServiceCategory getCategory() { 
            return category; }
        public void setCategory(ServiceCategory category) { 
            this.category = category; }

        public BigDecimal getBasePrice() { 
            return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { 
            this.basePrice = basePrice; }

        public String getUnit() { 
            return unit; }
        public void setUnit(String unit) { 
            this.unit = unit; }

        public ServiceStatus getStatus() { 
            return status; }
        public void setStatus(ServiceStatus status) { 
            this.status = status; }
    }
}
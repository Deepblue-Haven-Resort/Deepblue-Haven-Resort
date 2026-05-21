package deepbluehaven.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InventoryItemDTO {

    public static class Request {
        @NotNull(message = "Resort ID is required")
        private Long resortId;

        @NotBlank(message = "Item name is required")
        @Size(max = 150, message = "Name cannot exceed 150 characters")
        private String name;

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        private Integer quantity;

        @NotBlank(message = "Unit is required")
        @Size(max = 50, message = "Unit cannot exceed 50 characters")
        private String unit;

        @NotNull(message = "Supplier ID is required")
        private Long supplierId;

        public Request() {}

        public Long getResortId() { 
            return resortId; }
        public void setResortId(Long resortId) { 
            this.resortId = resortId; }

        public String getName() { 
            return name; }
        public void setName(String name) { 
            this.name = name; }

        public Integer getQuantity() { 
            return quantity; }
        public void setQuantity(Integer quantity) { 
            this.quantity = quantity; }

        public String getUnit() { 
            return unit; }
        public void setUnit(String unit) { 
            this.unit = unit; }

        public Long getSupplierId() { 
            return supplierId; }
        public void setSupplierId(Long supplierId) { 
            this.supplierId = supplierId; }
    }

    public static class Response {
        private Long id;
        private Long resortId;
        private String name;
        private Integer quantity;
        private String unit;
        private Long supplierId;

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

        public Integer getQuantity() { 
            return quantity; }
        public void setQuantity(Integer quantity) { 
            this.quantity = quantity; }

        public String getUnit() { 
            return unit; }
        public void setUnit(String unit) { 
            this.unit = unit; }

        public Long getSupplierId() { 
            return supplierId; }
        public void setSupplierId(Long supplierId) { 
            this.supplierId = supplierId; }
    }
}
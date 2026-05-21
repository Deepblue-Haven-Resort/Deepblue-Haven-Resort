package deepbluehaven.dto;

import java.math.BigDecimal;

import deepbluehaven.pojo.enums.RoomStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoomDTO {

    public static class Request {
        @NotNull(message = "Resort ID is required")
        private Long resortId;

        @NotBlank(message = "Room number is required")
        private String roomNumber;

        @NotBlank(message = "Room type is required")
        private String type;

        @NotNull(message = "Room status is required")
        private RoomStatus status;

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        private Integer capacity;

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be 0 or greater")
        private BigDecimal basePrice;

        public Request() {}

        public Long getResortId() { 
            return resortId; }
        public void setResortId(Long resortId) { 
            this.resortId = resortId; }

        public String getRoomNumber() { 
            return roomNumber; }
        public void setRoomNumber(String roomNumber) { 
            this.roomNumber = roomNumber; }

        public String getType() { 
            return type; }
        public void setType(String type) { 
            this.type = type; }

        public RoomStatus getStatus() { 
            return status; }
        public void setStatus(RoomStatus status) { 
            this.status = status; }

        public Integer getCapacity() { 
            return capacity; }
        public void setCapacity(Integer capacity) { 
            this.capacity = capacity; }

        public BigDecimal getBasePrice() { 
            return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { 
            this.basePrice = basePrice; }
    }

    public static class Response {
        private Long id;
        private Long resortId;
        private String roomNumber;
        private String type;
        private RoomStatus status;
        private Integer capacity;
        private BigDecimal basePrice;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getResortId() { 
            return resortId; }
        public void setResortId(Long resortId) { 
            this.resortId = resortId; }

        public String getRoomNumber() { 
            return roomNumber; }
        public void setRoomNumber(String roomNumber) { 
            this.roomNumber = roomNumber; }

        public String getType() { 
            return type; }
        public void setType(String type) { 
            this.type = type; }

        public RoomStatus getStatus() { 
            return status; }
        public void setStatus(RoomStatus status) { 
            this.status = status; }

        public Integer getCapacity() { 
            return capacity; }
        public void setCapacity(Integer capacity) { 
            this.capacity = capacity; }

        public BigDecimal getBasePrice() { 
            return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { 
            this.basePrice = basePrice; }
    }
}

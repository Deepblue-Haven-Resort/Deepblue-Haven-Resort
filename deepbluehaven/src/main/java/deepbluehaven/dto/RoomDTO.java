package deepbluehaven.dto;

import java.math.BigDecimal;
import java.util.List;

import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.pojo.enums.RoomTag;
import deepbluehaven.pojo.enums.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class RoomDTO {

    public static class Request {
        @NotNull(message = "Resort ID is required")
        private Long resortId;

        @NotBlank(message = "Room number is required")
        private String roomNumber;

        @NotNull(message = "Room type is required")
        private RoomType roomType;
        
        @NotEmpty(message = "You must provide at least one tag")
        private List<RoomTag> tags;


        @NotBlank(message = "Description is strictly required")
        private String description;

        @NotEmpty(message = "You must provide at least one image")
        private List<String> images;
        
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

        public RoomType getRoomType() { 
            return roomType; }
        public void setRoomType(RoomType roomType) { 
            this.roomType = roomType; }

        public List<RoomTag> getTags() { 
            return tags; }
        public void setTags(List<RoomTag> tags) { 
            this.tags = tags; }

        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }

        public List<String> getImages() { 
            return images; }
        public void setImages(List<String> images) { 
            this.images = images; }

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
        private RoomType roomType;
        private List<RoomTag> tags;
        private String description;
        private List<String> images;
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

        public RoomType getRoomType() { 
            return roomType; }
            
        public void setRoomType(RoomType roomType) { 
            this.roomType = roomType; }
        public List<RoomTag> getTags() { 
            return tags; }
        public void setTags(List<RoomTag> tags) { 
            this.tags = tags; }

        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }

        public List<String> getImages() { 
            return images; }
        public void setImages(List<String> images) { 
            this.images = images; }

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

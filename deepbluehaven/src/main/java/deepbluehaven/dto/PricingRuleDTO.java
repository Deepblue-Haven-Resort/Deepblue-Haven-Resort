package deepbluehaven.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class PricingRuleDTO {

    public static class Request {
        @NotBlank(message = "Room type is required")
        @Size(max = 100, message = "Room type cannot exceed 100 characters")
        private String roomType;

        @NotNull(message = "Multiplier is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Multiplier must be strictly greater than 0")
        private BigDecimal multiplier;

        @NotBlank(message = "Season is required")
        @Size(max = 100, message = "Season cannot exceed 100 characters")
        private String season;

        public Request() {}

        public String getRoomType() { 
            return roomType; }
        public void setRoomType(String roomType) { 
            this.roomType = roomType; }

        public BigDecimal getMultiplier() { 
            return multiplier; }
        public void setMultiplier(BigDecimal multiplier) { 
            this.multiplier = multiplier; }

        public String getSeason() { 
            return season; }
        public void setSeason(String season) { 
            this.season = season; }
    }

    public static class Response {
        private Long id;
        private String roomType;
        private BigDecimal multiplier;
        private String season;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public String getRoomType() { 
            return roomType; }
        public void setRoomType(String roomType) { 
            this.roomType = roomType; }

        public BigDecimal getMultiplier() { 
            return multiplier; }
        public void setMultiplier(BigDecimal multiplier) { 
            this.multiplier = multiplier; }

        public String getSeason() { 
            return season; }
        public void setSeason(String season) { 
            this.season = season; }
    }
}
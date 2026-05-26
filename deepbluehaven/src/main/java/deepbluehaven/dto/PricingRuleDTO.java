package deepbluehaven.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import deepbluehaven.pojo.enums.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class PricingRuleDTO {

    public static class Request {

        @NotNull(message = "Room type is required")
        private RoomType roomType;

        @NotNull(message = "Multiplier is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Multiplier must be strictly greater than 0")
        private BigDecimal multiplier;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;
        public Request() {}

        public RoomType getRoomType() { 
            return roomType; }
        public void setRoomType(RoomType roomType) { 
            this.roomType = roomType; }

        public BigDecimal getMultiplier() { 
            return multiplier; }
        public void setMultiplier(BigDecimal multiplier) { 
            this.multiplier = multiplier; }

        public LocalDate getStartDate() { 
            return startDate; }
        public void setStartDate(LocalDate startDate) { 
            this.startDate = startDate; }

        public LocalDate getEndDate() { 
            return endDate; }
        public void setEndDate(LocalDate endDate) { 
            this.endDate = endDate; }   
    }

    public static class Response {
        private Long id;
        private RoomType roomType;
        private BigDecimal multiplier;
        private LocalDate startDate;
        private LocalDate endDate;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public RoomType getRoomType() { 
            return roomType; }
        public void setRoomType(RoomType roomType) { 
            this.roomType = roomType; }

        public BigDecimal getMultiplier() { 
            return multiplier; }
        public void setMultiplier(BigDecimal multiplier) { 
            this.multiplier = multiplier; }

        public LocalDate getStartDate() { 
            return startDate; }
        public void setStartDate(LocalDate startDate) { 
            this.startDate = startDate; }

        public LocalDate getEndDate() { 
            return endDate; }
        public void setEndDate(LocalDate endDate) { 
            this.endDate = endDate; }
    }
}
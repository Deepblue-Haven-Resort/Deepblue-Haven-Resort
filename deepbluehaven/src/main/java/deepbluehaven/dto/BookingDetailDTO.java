package deepbluehaven.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookingDetailDTO {

    public static class Request {
        @NotNull(message = "Booking ID is required")
        private Long bookingId;

        @NotNull(message = "Room ID is required")
        private Long roomId;

        @NotNull(message = "Room type is required")
        private RoomType roomType;

        @NotNull(message = "Check-in date is required")
        private LocalDate checkIn;

        @NotNull(message = "Check-out date is required")
        private LocalDate checkOut;

        @NotNull(message = "Price per night is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price per night cannot be negative")
        private BigDecimal pricePerNight;

        @NotNull(message = "Sub total is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Sub total cannot be negative")
        private BigDecimal subTotal;

        @NotNull(message = "Status is required")
        private BookingStatus status;

        @NotBlank(message = "Action is required")
        private String action;

        public Request() {}

        public Long getBookingId() { 
            return bookingId; }
        public void setBookingId(Long bookingId) {
            this.bookingId = bookingId; }

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }

        public RoomType getRoomType() { 
            return roomType; }
        public void setRoomType(RoomType roomType) { 
            this.roomType = roomType; }

        public LocalDate getCheckIn() { 
            return checkIn; }
        public void setCheckIn(LocalDate checkIn) { 
            this.checkIn = checkIn; }

        public LocalDate getCheckOut() { 
            return checkOut; }
        public void setCheckOut(LocalDate checkOut) { 
            this.checkOut = checkOut; }

        public BigDecimal getPricePerNight() { 
            return pricePerNight; }
        public void setPricePerNight(BigDecimal pricePerNight) {
             this.pricePerNight = pricePerNight; }

        public BigDecimal getSubTotal() { 
            return subTotal; }
        public void setSubTotal(BigDecimal subTotal) { 
            this.subTotal = subTotal; }

        public BookingStatus getStatus() { 
            return status; }
        public void setStatus(BookingStatus status) { 
            this.status = status; }

        public String getAction() { 
            return action; }
        public void setAction(String action) { 
            this.action = action; }
    }

    public static class Response {
        private Long id;
        private Long bookingId;
        private Long roomId;
        private RoomType roomType;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private BigDecimal pricePerNight;
        private BigDecimal subTotal;
        private BookingStatus status;
        private String action;
        private LocalDateTime timestamp;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getBookingId() { 
            return bookingId; }
        public void setBookingId(Long bookingId) { 
            this.bookingId = bookingId; }

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }

        public RoomType getRoomType() { 
            return roomType; }
        public void setRoomType(RoomType roomType) {
            this.roomType = roomType; }

        public LocalDate getCheckIn() { 
            return checkIn; }
        public void setCheckIn(LocalDate checkIn) { 
            this.checkIn = checkIn; }

        public LocalDate getCheckOut() { 
            return checkOut; }
        public void setCheckOut(LocalDate checkOut) { 
            this.checkOut = checkOut; }

        public BigDecimal getPricePerNight() { 
            return pricePerNight; }
        public void setPricePerNight(BigDecimal pricePerNight) { 
            this.pricePerNight = pricePerNight; }

        public BigDecimal getSubTotal() { 
            return subTotal; }
        public void setSubTotal(BigDecimal subTotal) { 
            this.subTotal = subTotal; }

        public BookingStatus getStatus() { 
            return status; }
        public void setStatus(BookingStatus status) { 
            this.status = status; }

        public String getAction() { 
            return action; }
        public void setAction(String action) { 
            this.action = action; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }
    }
}
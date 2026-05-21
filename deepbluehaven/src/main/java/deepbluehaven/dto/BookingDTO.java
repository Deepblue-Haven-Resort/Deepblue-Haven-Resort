package deepbluehaven.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import deepbluehaven.pojo.enums.BookingStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public class BookingDTO {

    public static class Request {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        private Long createdById; 

        @NotNull(message = "Status is required")
        private BookingStatus status;

        @NotNull(message = "Booking time is required")
        @PastOrPresent(message = "Booking time cannot be in the future")
        private LocalDateTime bookingTime;

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Total amount cannot be negative")
        private BigDecimal totalAmount;

        private String note;

        public Request() {}

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getCreatedById() { 
            return createdById; }
        public void setCreatedById(Long createdById) { 
            this.createdById = createdById; }

        public BookingStatus getStatus() { 
            return status; }
        public void setStatus(BookingStatus status) { 
            this.status = status; }

        public LocalDateTime getBookingTime() { 
            return bookingTime; }
        public void setBookingTime(LocalDateTime bookingTime) { 
            this.bookingTime = bookingTime; }

        public BigDecimal getTotalAmount() { 
            return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { 
            this.totalAmount = totalAmount; }

        public String getNote() { return note; }
        public void setNote(String note) { 
            this.note = note; }
    }

    public static class Response {
        private Long id;
        private Long customerId;
        private Long createdById;
        private BookingStatus status;
        private LocalDateTime bookingTime;
        private BigDecimal totalAmount;
        private String note;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getCreatedById() { 
            return createdById; }
        public void setCreatedById(Long createdById) { 
            this.createdById = createdById; }

        public BookingStatus getStatus() { 
            return status; }
        public void setStatus(BookingStatus status) { 
            this.status = status; }

        public LocalDateTime getBookingTime() { 
            return bookingTime; }
        public void setBookingTime(LocalDateTime bookingTime) { 
            this.bookingTime = bookingTime; }

        public BigDecimal getTotalAmount() { 
            return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { 
            this.totalAmount = totalAmount; }

        public String getNote() { 
            return note; }
        public void setNote(String note) { 
            this.note = note; }
    }
}
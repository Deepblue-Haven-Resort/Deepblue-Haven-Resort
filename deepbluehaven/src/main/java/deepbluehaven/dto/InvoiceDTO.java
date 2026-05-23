package deepbluehaven.dto;

import deepbluehaven.pojo.enums.InvoiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceDTO {

    public static class Request {
        private Long bookingId;
        private Long customerId;
        
        @NotNull(message = "Worker ID is required")
        private Long workerId;

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Total amount cannot be negative")
        private BigDecimal totalAmount;

        @NotNull(message = "Paid amount is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Paid amount cannot be negative")
        private BigDecimal paidAmount;

        @NotNull(message = "Status is required")
        private InvoiceStatus status;

        public Request() {}

        public Long getBookingId() { 
            return bookingId; }
        public void setBookingId(Long bookingId) { 
            this.bookingId = bookingId; }

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public BigDecimal getTotalAmount() { 
            return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { 
            this.totalAmount = totalAmount; }

        public BigDecimal getPaidAmount() {
            return paidAmount; }
        public void setPaidAmount(BigDecimal paidAmount) { 
            this.paidAmount = paidAmount; }

        public InvoiceStatus getStatus() { 
            return status; }
        public void setStatus(InvoiceStatus status) { 
            this.status = status; }
    }

    public static class Response {
        private Long id;
        private Long bookingId;
        private Long customerId;
        private Long workerId;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private InvoiceStatus status;
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

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public BigDecimal getTotalAmount() { 
            return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { 
            this.totalAmount = totalAmount; }

        public BigDecimal getPaidAmount() { 
            return paidAmount; }
        public void setPaidAmount(BigDecimal paidAmount) { 
            this.paidAmount = paidAmount; }

        public InvoiceStatus getStatus() { 
            return status; }
        public void setStatus(InvoiceStatus status) { 
            this.status = status; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }
    }
}
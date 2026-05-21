package deepbluehaven.dto;

import deepbluehaven.pojo.enums.ServiceOrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceOrderDTO {

    public static class Request {
        private Long bookingId;
        private Long customerId;
        private String guestPhone;

        @Email(message = "Must be a valid email format")
        private String guestEmail;

        @NotNull(message = "Service ID is required")
        private Long serviceId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Total price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Total price cannot be negative")
        private BigDecimal totalPrice;

        private String note;

        @NotNull(message = "Status is required")
        private ServiceOrderStatus status;

        private Long processedById;

        @NotNull(message = "Order time is required")
        private LocalDateTime orderTime;

        private LocalDateTime completedTime;

        @NotBlank(message = "Action is required")
        private String action;

        public Request() {}

        public Long getBookingId() { 
            return bookingId; }
        public void setBookingId(Long bookingId) { 
            this.bookingId = bookingId; }

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public String getGuestPhone() { 
            return guestPhone; }
        public void setGuestPhone(String guestPhone) { 
            this.guestPhone = guestPhone; }

        public String getGuestEmail() { 
            return guestEmail; }
        public void setGuestEmail(String guestEmail) { 
            this.guestEmail = guestEmail; }

        public Long getServiceId() { 
            return serviceId; }
        public void setServiceId(Long serviceId) { 
            this.serviceId = serviceId; }

        public Integer getQuantity() { 
            return quantity; }
        public void setQuantity(Integer quantity) { 
            this.quantity = quantity; }

        public BigDecimal getTotalPrice() { 
            return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { 
            this.totalPrice = totalPrice; }

        public String getNote() { 
            return note; }
        public void setNote(String note) { 
            this.note = note; }

        public ServiceOrderStatus getStatus() { 
            return status; }
        public void setStatus(ServiceOrderStatus status) { 
            this.status = status; }

        public Long getProcessedById() { 
            return processedById; }
        public void setProcessedById(Long processedById) { 
            this.processedById = processedById; }

        public LocalDateTime getOrderTime() { 
            return orderTime; }
        public void setOrderTime(LocalDateTime orderTime) { 
            this.orderTime = orderTime; }

        public LocalDateTime getCompletedTime() { 
            return completedTime; }
        public void setCompletedTime(LocalDateTime completedTime) { 
            this.completedTime = completedTime; }

        public String getAction() { 
            return action; }
        public void setAction(String action) { 
            this.action = action; }
    }

    public static class Response {
        private Long id;
        private Long bookingId;
        private Long customerId;
        private String guestPhone;
        private String guestEmail;
        private Long serviceId;
        private Integer quantity;
        private BigDecimal totalPrice;
        private String note;
        private ServiceOrderStatus status;
        private Long processedById;
        private LocalDateTime orderTime;
        private LocalDateTime completedTime;
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

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public String getGuestPhone() { 
            return guestPhone; }
        public void setGuestPhone(String guestPhone) { 
            this.guestPhone = guestPhone; }

        public String getGuestEmail() { 
            return guestEmail; }
        public void setGuestEmail(String guestEmail) { 
            this.guestEmail = guestEmail; }

        public Long getServiceId() { 
            return serviceId; }
        public void setServiceId(Long serviceId) { 
            this.serviceId = serviceId; }

        public Integer getQuantity() { 
            return quantity; }
        public void setQuantity(Integer quantity) { 
            this.quantity = quantity; }

        public BigDecimal getTotalPrice() { 
            return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { 
            this.totalPrice = totalPrice; }

        public String getNote() { 
            return note; }
        public void setNote(String note) { 
            this.note = note; }

        public ServiceOrderStatus getStatus() { 
            return status; }
        public void setStatus(ServiceOrderStatus status) { 
            this.status = status; }

        public Long getProcessedById() { 
            return processedById; }
        public void setProcessedById(Long processedById) { 
            this.processedById = processedById; }

        public LocalDateTime getOrderTime() { 
            return orderTime; }
        public void setOrderTime(LocalDateTime orderTime) { 
            this.orderTime = orderTime; }

        public LocalDateTime getCompletedTime() { 
            return completedTime; }
        public void setCompletedTime(LocalDateTime completedTime) { 
            this.completedTime = completedTime; }

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
package deepbluehaven.dto;

import java.time.LocalDateTime;

import deepbluehaven.pojo.enums.CustomerDiscountStatus;
import jakarta.validation.constraints.NotNull;

public class CustomerDiscountDTO {

    public static class Request {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        @NotNull(message = "Discount ID is required")
        private Long discountId;

        @NotNull(message = "Status is required")
        private CustomerDiscountStatus status;

        public Request() {}

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public Long getDiscountId() { return discountId; }
        public void setDiscountId(Long discountId) { this.discountId = discountId; }

        public CustomerDiscountStatus getStatus() { return status; }
        public void setStatus(CustomerDiscountStatus status) { this.status = status; }
    }

    public static class Response {
        private Long id;
        private Long customerId;
        private Long discountId;
        private CustomerDiscountStatus status;
        private LocalDateTime acquiredAt;
        private LocalDateTime usedAt;
        private Long referenceInvoiceId;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getDiscountId() { 
            return discountId; }
        public void setDiscountId(Long discountId) { 
            this.discountId = discountId; }

        public CustomerDiscountStatus getStatus() { 
            return status; }
        public void setStatus(CustomerDiscountStatus status) { 
            this.status = status; }

        public LocalDateTime getAcquiredAt() { 
            return acquiredAt; }
        public void setAcquiredAt(LocalDateTime acquiredAt) { 
            this.acquiredAt = acquiredAt; }

        public LocalDateTime getUsedAt() { 
            return usedAt; }
        public void setUsedAt(LocalDateTime usedAt) { 
            this.usedAt = usedAt; }

        public Long getReferenceInvoiceId() { 
            return referenceInvoiceId; }
        public void setReferenceInvoiceId(Long referenceInvoiceId) { 
            this.referenceInvoiceId = referenceInvoiceId; }
    }
}
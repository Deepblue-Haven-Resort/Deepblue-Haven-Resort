package deepbluehaven.dto;

import deepbluehaven.pojo.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class InvoiceStatusLogDTO {

    public static class Request {
        @NotNull(message = "Invoice ID is required")
        private Long invoiceId;

        @NotNull(message = "Worker ID is required")
        private Long workerId;

        private InvoiceStatus previousStatus;

        @NotNull(message = "Current status is required")
        private InvoiceStatus currentStatus;

        public Request() {}

        public Long getInvoiceId() { 
            return invoiceId; }
        public void setInvoiceId(Long invoiceId) { 
            this.invoiceId = invoiceId; }

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public InvoiceStatus getPreviousStatus() { 
            return previousStatus; }
        public void setPreviousStatus(InvoiceStatus previousStatus) { 
            this.previousStatus = previousStatus; }

        public InvoiceStatus getCurrentStatus() { 
            return currentStatus; }
        public void setCurrentStatus(InvoiceStatus currentStatus) { 
            this.currentStatus = currentStatus; }
    }

    public static class Response {
        private Long id;
        private Long invoiceId;
        private Long workerId;
        private InvoiceStatus previousStatus;
        private InvoiceStatus currentStatus;
        private LocalDateTime timestamp;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getInvoiceId() { 
            return invoiceId; }
        public void setInvoiceId(Long invoiceId) { 
            this.invoiceId = invoiceId; }

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public InvoiceStatus getPreviousStatus() { 
            return previousStatus; }
        public void setPreviousStatus(InvoiceStatus previousStatus) { 
            this.previousStatus = previousStatus; }

        public InvoiceStatus getCurrentStatus() { 
            return currentStatus; }
        public void setCurrentStatus(InvoiceStatus currentStatus) { 
            this.currentStatus = currentStatus; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }
    }
}

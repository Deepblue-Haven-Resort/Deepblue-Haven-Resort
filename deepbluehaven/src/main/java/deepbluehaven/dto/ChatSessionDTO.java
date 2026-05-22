package deepbluehaven.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatSessionDTO {

    public static class Request {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        private Long currentAssigneeId;

        @NotBlank(message = "Status is required")
        @Size(max = 50, message = "Status cannot exceed 50 characters")
        private String status;

        public Request() {}

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getCurrentAssigneeId() { 
            return currentAssigneeId; }
        public void setCurrentAssigneeId(Long currentAssigneeId) { 
            this.currentAssigneeId = currentAssigneeId; }

        public String getStatus() { 
            return status; }
        public void setStatus(String status) { 
            this.status = status; }
    }

    public static class Response {
        private Long id;
        private Long customerId;
        private Long currentAssigneeId;
        private boolean isRead;
        private LocalDateTime startTime;
        private String status;
        private LocalDateTime updatedAt;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getCurrentAssigneeId() { 
            return currentAssigneeId; }
        public void setCurrentAssigneeId(Long currentAssigneeId) { 
            this.currentAssigneeId = currentAssigneeId; }

        public boolean isRead() { 
            return isRead; }
        public void setRead(boolean isRead) { 
            this.isRead = isRead; }

        public LocalDateTime getStartTime() { 
            return startTime; }
        public void setStartTime(LocalDateTime startTime) { 
            this.startTime = startTime; }

        public LocalDateTime getUpdatedAt() { 
            return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { 
            this.updatedAt = updatedAt; }

        public String getStatus() { 
            return status; }
        public void setStatus(String status) { 
            this.status = status; }
    }
}

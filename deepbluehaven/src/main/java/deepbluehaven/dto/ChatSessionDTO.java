package deepbluehaven.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class ChatSessionDTO {

    public static class Request {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        @NotBlank(message = "Status is required")
        @Size(max = 50, message = "Status cannot exceed 50 characters")
        private String status;

        public Request() {}

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public String getStatus() { 
            return status; }
        public void setStatus(String status) { 
            this.status = status; }
    }

    public static class Response {
        private Long id;
        private Long customerId;
        private LocalDateTime startTime;
        private String status;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public LocalDateTime getStartTime() { 
            return startTime; }
        public void setStartTime(LocalDateTime startTime) { 
            this.startTime = startTime; }

        public String getStatus() { 
            return status; }
        public void setStatus(String status) { 
            this.status = status; }
    }
}

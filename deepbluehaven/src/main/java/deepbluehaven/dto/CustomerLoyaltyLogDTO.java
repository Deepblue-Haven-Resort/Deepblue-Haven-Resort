package deepbluehaven.dto;

import deepbluehaven.pojo.enums.ReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class CustomerLoyaltyLogDTO {

    public static class Request {
        @NotNull(message = "Reference type is required")
        private ReferenceType referenceType;

        @NotNull(message = "Customer ID is required")
        private Long customerId;

        @NotNull(message = "Reference ID is required")
        private Long referenceId;

        @NotNull(message = "Points changed is required")
        private Integer pointsChanged;

        @NotBlank(message = "Reason is required")
        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        private String reason;

        public Request() {}

        public ReferenceType getReferenceType() { 
            return referenceType; }
        public void setReferenceType(ReferenceType referenceType) { 
            this.referenceType = referenceType; }

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getReferenceId() { 
            return referenceId; }
        public void setReferenceId(Long referenceId) { 
            this.referenceId = referenceId; }

        public Integer getPointsChanged() { 
            return pointsChanged; }
        public void setPointsChanged(Integer pointsChanged) { 
            this.pointsChanged = pointsChanged; }

        public String getReason() { 
            return reason; }
        public void setReason(String reason) { 
            this.reason = reason; }
    }

    public static class Response {
        private Long id;
        private ReferenceType referenceType;
        private Long customerId;
        private Long referenceId;
        private Integer pointsChanged;
        private String reason;
        private LocalDateTime timestamp;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public ReferenceType getReferenceType() { 
            return referenceType; }
        public void setReferenceType(ReferenceType referenceType) { 
            this.referenceType = referenceType; }

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getReferenceId() { 
            return referenceId; }
        public void setReferenceId(Long referenceId) { 
            this.referenceId = referenceId; }

        public Integer getPointsChanged() { 
            return pointsChanged; }
        public void setPointsChanged(Integer pointsChanged) { 
            this.pointsChanged = pointsChanged; }

        public String getReason() { 
            return reason; }
        public void setReason(String reason) { 
            this.reason = reason; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }
    }
}
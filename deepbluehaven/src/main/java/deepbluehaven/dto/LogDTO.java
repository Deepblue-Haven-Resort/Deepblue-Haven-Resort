package deepbluehaven.dto;

import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.ObjectType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class LogDTO {

    public static class Request {
        @NotNull(message = "Object type is required")
        private ObjectType objectType;

        @NotNull(message = "Object ID is required")
        private Long objectId;

        @Size(max = 100, message = "Correlation ID cannot exceed 100 characters")
        private String correlationId;

        @NotNull(message = "Action code is required")
        private ActionCode actionCode;

        private Long workerId;

        @Size(max = 50, message = "Previous status cannot exceed 50 characters")
        private String previousStatus;

        @Size(max = 50, message = "Current status cannot exceed 50 characters")
        private String currentStatus;

        private String metadata;

        public Request() {}

        public ObjectType getObjectType() { 
            return objectType; }
        public void setObjectType(ObjectType objectType) { 
            this.objectType = objectType; }

        public Long getObjectId() { 
            return objectId; }
        public void setObjectId(Long objectId) { 
            this.objectId = objectId; }

        public String getCorrelationId() { 
            return correlationId; }
        public void setCorrelationId(String correlationId) { 
            this.correlationId = correlationId; }

        public ActionCode getActionCode() { 
            return actionCode; }
        public void setActionCode(ActionCode actionCode) { 
            this.actionCode = actionCode; }

        public Long getWorkerId() { return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public String getPreviousStatus() { 
            return previousStatus; }
        public void setPreviousStatus(String previousStatus) { 
            this.previousStatus = previousStatus; }

        public String getCurrentStatus() { 
            return currentStatus; }
        public void setCurrentStatus(String currentStatus) { 
            this.currentStatus = currentStatus; }

        public String getMetadata() { 
            return metadata; }
        public void setMetadata(String metadata) { 
            this.metadata = metadata; }
    }

    public static class Response {
        private Long id;
        private ObjectType objectType;
        private Long objectId;
        private String correlationId;
        private ActionCode actionCode;
        private LocalDateTime timestamp;
        private Long workerId;
        private String previousStatus;
        private String currentStatus;
        private String metadata;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public ObjectType getObjectType() { 
            return objectType; }
        public void setObjectType(ObjectType objectType) { 
            this.objectType = objectType; }

        public Long getObjectId() { 
            return objectId; }
        public void setObjectId(Long objectId) { 
            this.objectId = objectId; }

        public String getCorrelationId() { 
            return correlationId; }
        public void setCorrelationId(String correlationId) { 
            this.correlationId = correlationId; }

        public ActionCode getActionCode() { 
            return actionCode; }
        public void setActionCode(ActionCode actionCode) { 
            this.actionCode = actionCode; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public String getPreviousStatus() { 
            return previousStatus; }
        public void setPreviousStatus(String previousStatus) { 
            this.previousStatus = previousStatus; }

        public String getCurrentStatus() { 
            return currentStatus; }
        public void setCurrentStatus(String currentStatus) { 
            this.currentStatus = currentStatus; }

        public String getMetadata() { 
            return metadata; }
        public void setMetadata(String metadata) { 
            this.metadata = metadata; }
    }
}
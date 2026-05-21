package deepbluehaven.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class WorkerRoomAssignmentLogDTO {

    public static class Request {
        @NotNull(message = "Worker ID is required")
        private Long workerId;

        @NotNull(message = "Room ID is required")
        private Long roomId;

        @NotBlank(message = "Action is required")
        private String action;

        @NotNull(message = "Updated By Worker ID is required")
        private Long updatedById;

        public Request() {}

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }

        public String getAction() { 
            return action; }
        public void setAction(String action) { 
            this.action = action; }

        public Long getUpdatedById() { 
            return updatedById; }
        public void setUpdatedById(Long updatedById) { 
            this.updatedById = updatedById; }
    }

    public static class Response {
        private Long id;
        private Long workerId;
        private Long roomId;
        private String action;
        private Long updatedById;
        private LocalDateTime timestamp;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }

        public String getAction() { 
            return action; }
        public void setAction(String action) { 
            this.action = action; }

        public Long getUpdatedById() { 
            return updatedById; }
        public void setUpdatedById(Long updatedById) { 
            this.updatedById = updatedById; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }
    }
}
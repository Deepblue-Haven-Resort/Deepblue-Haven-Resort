package deepbluehaven.dto;

import java.time.LocalDateTime;

import deepbluehaven.pojo.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskDTO {

    public static class Request {
        @NotNull(message = "Room ID is required")
        private Long roomId;

        @NotBlank(message = "Task type is required")
        @Size(max = 100, message = "Type cannot exceed 100 characters")
        private String type;

        private Long assignedToId; 

        @NotNull(message = "Status is required")
        private TaskStatus status;

        @NotBlank(message = "Action is required")
        @Size(max = 100, message = "Action cannot exceed 100 characters")
        private String action;

        public Request() {}

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }

        public String getType() { 
            return type; }
        public void setType(String type) { 
            this.type = type; }

        public Long getAssignedToId() { 
            return assignedToId; }
        public void setAssignedToId(Long assignedToId) { 
            this.assignedToId = assignedToId; }

        public TaskStatus getStatus() { 
            return status; }
        public void setStatus(TaskStatus status) { 
            this.status = status; }

        public String getAction() { 
            return action; }
        public void setAction(String action) { 
            this.action = action; }
    }

    public static class Response {
        private Long id;
        private Long roomId;
        private String type;
        private Long assignedToId;
        private TaskStatus status;
        private String action;
        private LocalDateTime timestamp;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }

        public String getType() { 
            return type; }
        public void setType(String type) { 
            this.type = type; }

        public Long getAssignedToId() { 
            return assignedToId; }
        public void setAssignedToId(Long assignedToId) { 
            this.assignedToId = assignedToId; }

        public TaskStatus getStatus() { 
            return status; }
        public void setStatus(TaskStatus status) { 
            this.status = status; }

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
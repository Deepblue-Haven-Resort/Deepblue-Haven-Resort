package deepbluehaven.dto;

import java.time.LocalDateTime;

import deepbluehaven.pojo.enums.RoomStatus;
import jakarta.validation.constraints.NotNull;

public class RoomStatusLogDTO {

    public static class Request {
        @NotNull(message = "Room ID is required")
        private Long roomId;

        @NotNull(message = "Worker ID is required")
        private Long workerId;

        private RoomStatus previousStatus;

        @NotNull(message = "Current status is required")
        private RoomStatus currentStatus;

        public Request() {}

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public RoomStatus getPreviousStatus() { 
            return previousStatus; }
        public void setPreviousStatus(RoomStatus previousStatus) { 
            this.previousStatus = previousStatus; }

        public RoomStatus getCurrentStatus() { 
            return currentStatus; }
        public void setCurrentStatus(RoomStatus currentStatus) { 
            this.currentStatus = currentStatus; }
    }

    public static class Response {
        private Long id;
        private Long roomId;
        private Long workerId;
        private RoomStatus previousStatus;
        private RoomStatus currentStatus;
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

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public RoomStatus getPreviousStatus() { 
            return previousStatus; }
        public void setPreviousStatus(RoomStatus previousStatus) { 
            this.previousStatus = previousStatus; }

        public RoomStatus getCurrentStatus() { 
            return currentStatus; }
        public void setCurrentStatus(RoomStatus currentStatus) { 
            this.currentStatus = currentStatus; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }
    }
}

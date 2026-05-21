package deepbluehaven.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class ChatMessageDTO {

    public static class Request {
        @NotNull(message = "Session ID is required")
        private Long sessionId;

        @NotBlank(message = "Sender is required")
        @Size(max = 100, message = "Sender cannot exceed 100 characters")
        private String sender;

        @NotBlank(message = "Content is required")
        private String content;

        public Request() {}

        public Long getSessionId() { 
            return sessionId; }
        public void setSessionId(Long sessionId) { 
            this.sessionId = sessionId; }

        public String getSender() { 
            return sender; }
        public void setSender(String sender) { 
            this.sender = sender; }

        public String getContent() { 
            return content; }
        public void setContent(String content) { 
            this.content = content; }
    }

    public static class Response {
        private Long id;
        private Long sessionId;
        private String sender;
        private String content;
        private LocalDateTime timestamp;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getSessionId() { 
            return sessionId; }
        public void setSessionId(Long sessionId) { 
            this.sessionId = sessionId; }

        public String getSender() { 
            return sender; }
        public void setSender(String sender) { 
            this.sender = sender; }

        public String getContent() { 
            return content; }
        public void setContent(String content) { 
            this.content = content; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }
    }
}


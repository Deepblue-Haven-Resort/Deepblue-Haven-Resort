package deepbluehaven.dto;

import java.time.LocalDateTime;

import deepbluehaven.pojo.enums.MessageType;
import deepbluehaven.pojo.enums.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatMessageDTO {

    public static class Request {
        @NotNull(message = "Session ID is required")
        private Long sessionId;

        private Long senderId;

        @NotNull(message = "Sender type is strictly required")
        private SenderType senderType;

        @NotNull(message = "Message type is strictly required")
        private MessageType messageType;

        @NotBlank(message = "Content is required")
        private String content;

        public Request() {}

        public Long getSessionId() { 
            return sessionId; }
        public void setSessionId(Long sessionId) { 
            this.sessionId = sessionId; }
            
        public Long getSenderId() { 
            return senderId; }
        public void setSenderId(Long senderId) { 
            this.senderId = senderId; }

        public SenderType getSenderType() { 
            return senderType; }
        public void setSenderType(SenderType senderType) { 
            this.senderType = senderType; }

        public MessageType getMessageType() { 
            return messageType; }
        public void setMessageType(MessageType messageType) { 
            this.messageType = messageType; }

        public String getContent() { 
            return content; }
        public void setContent(String content) { 
            this.content = content; }
    }

    public static class Response {
        private Long id;
        private Long sessionId;
        private Long senderId;
        private SenderType senderType;
        private MessageType messageType;
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

        public Long getSenderId() { 
            return senderId; }
        public void setSenderId(Long senderId) { 
            this.senderId = senderId; }

        public SenderType getSenderType() { 
            return senderType; }
        public void setSenderType(SenderType senderType) { 
            this.senderType = senderType; }

        public MessageType getMessageType() { 
            return messageType; }
        public void setMessageType(MessageType messageType) { 
            this.messageType = messageType; }

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


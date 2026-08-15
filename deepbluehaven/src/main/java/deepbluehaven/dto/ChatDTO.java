package deepbluehaven.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import deepbluehaven.pojo.enums.ChatStatus;
import deepbluehaven.pojo.enums.MessageType;
import deepbluehaven.pojo.enums.SenderType;

public class ChatDTO {

    public static class MessageView {
        private Long id;
        private Long sessionId;
        private Long senderId;
        private String senderName;
        private SenderType senderType;
        private MessageType messageType;
        private String content;
        private boolean isRead;
        private LocalDateTime timestamp;
        private String formattedTime;

        public MessageView() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }

        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }

        public SenderType getSenderType() { return senderType; }
        public void setSenderType(SenderType senderType) { this.senderType = senderType; }

        public MessageType getMessageType() { return messageType; }
        public void setMessageType(MessageType messageType) { this.messageType = messageType; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean isRead) { this.isRead = isRead; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; 
            if (timestamp != null) {
                this.formattedTime = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        }

        public String getFormattedTime() { return formattedTime; }
        public void setFormattedTime(String formattedTime) { this.formattedTime = formattedTime; }
    }

    public static class SessionView {
        private Long id;
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private String customerTier;
        private String customerAvatar;
        private Long assigneeId;
        private String assigneeName;
        private ChatStatus status;
        private boolean isRead;
        private LocalDateTime startTime;
        private LocalDateTime updatedAt;
        private String lastMessage;
        private String lastMessageTime;
        private Long unreadCount;
        private List<MessageView> messages;

        public SessionView() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

        public String getCustomerTier() { return customerTier; }
        public void setCustomerTier(String customerTier) { this.customerTier = customerTier; }

        public String getCustomerAvatar() { return customerAvatar; }
        public void setCustomerAvatar(String customerAvatar) { this.customerAvatar = customerAvatar; }

        public Long getAssigneeId() { return assigneeId; }
        public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }

        public String getAssigneeName() { return assigneeName; }
        public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

        public ChatStatus getStatus() { return status; }
        public void setStatus(ChatStatus status) { this.status = status; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean isRead) { this.isRead = isRead; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { 
            this.updatedAt = updatedAt; 
            if (updatedAt != null) {
                this.lastMessageTime = updatedAt.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM"));
            }
        }

        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

        public String getLastMessageTime() { return lastMessageTime; }
        public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }

        public Long getUnreadCount() { return unreadCount; }
        public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }

        public List<MessageView> getMessages() { return messages; }
        public void setMessages(List<MessageView> messages) { this.messages = messages; }
    }

    public static class SendMessageRequest {
        private Long sessionId;
        private String content;
        private String guestName;
        private String guestEmail;

        public SendMessageRequest() {}

        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }

        public String getGuestEmail() { return guestEmail; }
        public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    }
}

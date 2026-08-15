package deepbluehaven.pojo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import deepbluehaven.pojo.enums.MessageType;
import deepbluehaven.pojo.enums.SenderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Chat_Message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "sender_name", length = 150, columnDefinition = "NVARCHAR(150)")
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private SenderType senderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "content", nullable = false, length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public ChatSession getChatSession() { 
        return chatSession; 
    }
    public void setChatSession(ChatSession chatSession) { 
        this.chatSession = chatSession; 
    }

    public Long getSenderId() { 
        return senderId; 
    }
    public void setSenderId(Long senderId) { 
        this.senderId = senderId; 
    }

    public String getSenderName() {
        return senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public SenderType getSenderType() { 
        return senderType; 
    }
    public void setSenderType(SenderType senderType) { 
        this.senderType = senderType; 
    }

    public MessageType getMessageType() { 
        return messageType; 
    }
    public void setMessageType(MessageType messageType) { 
        this.messageType = messageType; 
    }

    public String getContent() { 
        return content; 
    }
    public void setContent(String content) { 
        this.content = content; 
    }

    public boolean isRead() {
        return isRead;
    }
    public boolean getIsRead() {
        return isRead;
    }
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; 
    }
}
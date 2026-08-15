package deepbluehaven.pojo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import deepbluehaven.pojo.enums.ChatStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Chat_Session")
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker assignee;

    @Column(name = "current_assignee_id")
    private Long currentAssigneeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ChatStatus status = ChatStatus.WAITING;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "start_time", nullable = false, updatable = false)
    private LocalDateTime startTime;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessage> messages = new ArrayList<>();

    public ChatSession() {}

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public Customer getCustomer() { 
        return customer; 
    }
    public void setCustomer(Customer customer) { 
        this.customer = customer; 
    }

    public Worker getAssignee() {
        return assignee;
    }
    public void setAssignee(Worker assignee) {
        this.assignee = assignee;
        if (assignee != null) {
            this.currentAssigneeId = assignee.getId();
        }
    }

    public Long getCurrentAssigneeId() { 
        return currentAssigneeId; 
    }
    public void setCurrentAssigneeId(Long currentAssigneeId) { 
        this.currentAssigneeId = currentAssigneeId; 
    }

    public LocalDateTime getStartTime() { 
        return startTime; 
    }
    public void setStartTime(LocalDateTime startTime) { 
        this.startTime = startTime; 
    }

    public ChatStatus getStatus() { 
        return status; 
    }
    public void setStatus(ChatStatus status) { 
        this.status = status; 
    }

    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    public boolean isRead() { 
        return isRead; 
    }
    public boolean isIsread() {
        return isRead;
    }
    public boolean getIsRead() {
        return isRead;
    }
    public void setRead(boolean isRead) { 
        this.isRead = isRead; 
    }
    public void setIsread(boolean isRead) { 
        this.isRead = isRead; 
    }
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public List<ChatMessage> getMessages() { 
        return messages; 
    }
    public void setMessages(List<ChatMessage> messages) { 
        this.messages = messages; 
    }

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        message.setChatSession(this);
    }
}

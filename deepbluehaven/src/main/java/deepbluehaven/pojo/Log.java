package deepbluehaven.pojo;

import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.ObjectType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "Log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false, length = 50)
    private ObjectType objectType;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "correlation_id", nullable = true, length = 100)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_code", nullable = false, length = 50)
    private ActionCode actionCode;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "worker_id", nullable = true)
    private Long workerId;

    @Column(name = "previous_status", nullable = true, length = 50)
    private String previousStatus;

    @Column(name = "current_status", nullable = true, length = 50)
    private String currentStatus;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    public Log() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
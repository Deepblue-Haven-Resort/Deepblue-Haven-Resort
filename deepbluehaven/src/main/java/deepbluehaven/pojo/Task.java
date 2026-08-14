package deepbluehaven.pojo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import deepbluehaven.pojo.enums.TaskStatus;
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
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "Task", indexes = {
    @Index(name = "idx_task_assigned_status", columnList = "assigned_to_id, status"),
    @Index(name = "idx_task_room", columnList = "room_id")
})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_type_id", nullable = false)
    private TaskType taskType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = true)
    private Worker assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id", nullable = true)
    private Worker assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TaskStatus status;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "due_time", nullable = true)
    private LocalDateTime dueTime;

    @Column(name = "proof_image_url", length = 500)
    private String proofImageUrl;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public Task() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Room getRoom() { 
        return room; }
    public void setRoom(Room room) { 
        this.room = room; }

    public TaskType getTaskType() { 
        return taskType; }
    public void setTaskType(TaskType taskType) { 
        this.taskType = taskType; }
    public Worker getAssignedBy() { 
        return assignedBy; }
    public void setAssignedBy(Worker assignedBy) { 
        this.assignedBy = assignedBy; }

    public Worker getAssignedTo() { 
        return assignedTo; }
    public void setAssignedTo(Worker assignedTo) { 
        this.assignedTo = assignedTo; }

    public TaskStatus getStatus() { 
        return status; }
    public void setStatus(TaskStatus status) { 
        this.status = status; }

    public String getAction() { 
        return action; }
    public void setAction(String action) { 
        this.action = action; }

    public String getProofImageUrl() {
        return proofImageUrl; }
    public void setProofImageUrl(String proofImageUrl) {
        this.proofImageUrl = proofImageUrl; }

    public LocalDateTime getDueTime() {
        return dueTime; }
    public void setDueTime(LocalDateTime dueTime) {
        this.dueTime = dueTime; }

    public LocalDateTime getTimestamp() { 
        return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; }
}

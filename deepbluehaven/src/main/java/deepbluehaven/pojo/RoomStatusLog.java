package deepbluehaven.pojo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import deepbluehaven.pojo.enums.RoomStatus;
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
@Table(name = "Room_Status_Log")
public class RoomStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = true, length = 30)
    private RoomStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private RoomStatus currentStatus;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public RoomStatusLog() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Room getRoom() { 
        return room; }
    public void setRoom(Room room) { 
        this.room = room; }

    public Worker getWorker() { 
        return worker; }
    public void setWorker(Worker worker) { 
        this.worker = worker; }

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
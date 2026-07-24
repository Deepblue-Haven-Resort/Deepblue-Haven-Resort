package deepbluehaven.pojo;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import deepbluehaven.pojo.enums.BookingStatus;
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
@Table(name = "Booking_Log")
public class BookingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = true, length = 30)
    private BookingStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private BookingStatus currentStatus;

    @Column(name = "note", length = 500, columnDefinition = "NVARCHAR(500)")
    private String note;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public BookingLog() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Booking getBooking() { 
        return booking; }
    public void setBooking(Booking booking) { 
        this.booking = booking; }

    public Long getActorId() { 
        return actorId; }
    public void setActorId(Long actorId) { 
        this.actorId = actorId; }

    public BookingStatus getPreviousStatus() { 
        return previousStatus; }
    public void setPreviousStatus(BookingStatus previousStatus) { 
        this.previousStatus = previousStatus; }

    public BookingStatus getCurrentStatus() { 
        return currentStatus; }
    public void setCurrentStatus(BookingStatus currentStatus) { 
        this.currentStatus = currentStatus; }

    public String getNote() { 
        return note; }
    public void setNote(String note) { 
        this.note = note; }

    public LocalDateTime getTimestamp() { 
        return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; }
}

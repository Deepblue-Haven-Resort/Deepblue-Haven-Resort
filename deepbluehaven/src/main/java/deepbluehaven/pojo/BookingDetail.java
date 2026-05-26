package deepbluehaven.pojo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.RoomType;
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
@Table(name = "Booking_Detail")
public class BookingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "price_per_night", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "sub_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal subTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public BookingDetail() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Booking getBooking() { 
        return booking; }
    public void setBooking(Booking booking) { 
        this.booking = booking; }

    public Room getRoom() { 
        return room; }
    public void setRoom(Room room) { 
        this.room = room; }

    public RoomType getRoomType() { 
        return roomType; }
    public void setRoomType(RoomType roomType) { 
        this.roomType = roomType; }

    public LocalDate getCheckIn() { 
        return checkIn; }
    public void setCheckIn(LocalDate checkIn) { 
        this.checkIn = checkIn; }

    public LocalDate getCheckOut() { 
        return checkOut; }
    public void setCheckOut(LocalDate checkOut) { 
        this.checkOut = checkOut; }

    public BigDecimal getPricePerNight() { 
        return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { 
        this.pricePerNight = pricePerNight; }

    public BigDecimal getSubTotal() { 
        return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { 
        this.subTotal = subTotal; }

    public BookingStatus getStatus() { 
        return status; }
    public void setStatus(BookingStatus status) { 
        this.status = status; }

    public String getAction() { 
        return action; }
    public void setAction(String action) { 
        this.action = action; }

    public LocalDateTime getTimestamp() { 
        return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; }
}
package deepbluehaven.pojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import deepbluehaven.pojo.enums.BookingStatus;
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
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "Booking", indexes = {
    @Index(name = "idx_booking_time_status", columnList = "booking_time, status"),
    @Index(name = "idx_booking_customer", columnList = "customer_id")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = true)
    private Worker createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingTime;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "note", length = 500, columnDefinition = "NVARCHAR(500)")
    private String note;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingDetail> details = new ArrayList<>();

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    private List<BookingLog> logs = new ArrayList<>();

    public Booking() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Customer getCustomer() { 
        return customer; }
    public void setCustomer(Customer customer) { 
        this.customer = customer; }

    public Worker getCreatedBy() { 
        return createdBy; }
    public void setCreatedBy(Worker createdBy) { 
        this.createdBy = createdBy; }

    public BookingStatus getStatus() { 
        return status; }
    public void setStatus(BookingStatus status) { 
        this.status = status; }

    public LocalDateTime getBookingTime() { 
        return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { 
        this.bookingTime = bookingTime; }

    public BigDecimal getTotalAmount() { 
        return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { 
        this.totalAmount = totalAmount; }

    public String getNote() { 
        return note; }
    public void setNote(String note) { 
        this.note = note; }

    public List<BookingDetail> getDetails() { 
        return details; }
    public void setDetails(List<BookingDetail> details) { 
        this.details = details; }

    public List<BookingLog> getLogs() { 
        return logs; }
    public void setLogs(List<BookingLog> logs) { 
        this.logs = logs; }
}
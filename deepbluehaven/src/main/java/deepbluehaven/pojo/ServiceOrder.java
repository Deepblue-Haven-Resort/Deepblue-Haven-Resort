package deepbluehaven.pojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import deepbluehaven.pojo.enums.ServiceOrderStatus;
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
@Table(name = "Service_Order")
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    private Customer customer;

    @Column(name = "guest_phone", nullable = true, length = 20)
    private String guestPhone;

    @Column(name = "guest_email", nullable = true, length = 120)
    private String guestEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "note", length = 500, columnDefinition = "NVARCHAR(500)")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ServiceOrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id", nullable = true)
    private Worker processedBy;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Column(name = "completed_time", nullable = true)
    private LocalDateTime completedTime;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public ServiceOrder() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Booking getBooking() { 
        return booking; }
    public void setBooking(Booking booking) { 
        this.booking = booking; }

    public Customer getCustomer() { 
        return customer; }
    public void setCustomer(Customer customer) { 
        this.customer = customer; }

    public String getGuestPhone() { 
        return guestPhone; }
    public void setGuestPhone(String guestPhone) { 
        this.guestPhone = guestPhone; }

    public String getGuestEmail() { 
        return guestEmail; }
    public void setGuestEmail(String guestEmail) { 
        this.guestEmail = guestEmail; }

    public Service getService() { 
        return service; }
    public void setService(Service service) { 
        this.service = service; }

    public Integer getQuantity() { 
        return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; }

    public BigDecimal getTotalPrice() { 
        return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { 
        this.totalPrice = totalPrice; }

    public String getNote() { 
        return note; }
    public void setNote(String note) { 
        this.note = note; }

    public ServiceOrderStatus getStatus() { 
        return status; }
    public void setStatus(ServiceOrderStatus status) { 
        this.status = status; }

    public Worker getProcessedBy() { 
        return processedBy; }
    public void setProcessedBy(Worker processedBy) { 
        this.processedBy = processedBy; }

    public LocalDateTime getOrderTime() { 
        return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { 
        this.orderTime = orderTime; }

    public LocalDateTime getCompletedTime() { 
        return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { 
        this.completedTime = completedTime; }

    public String getAction() { 
        return action; }
    public void setAction(String action) { 
        this.action = action; }

    public LocalDateTime getTimestamp() { 
        return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; }
}
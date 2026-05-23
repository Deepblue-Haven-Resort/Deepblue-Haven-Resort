package deepbluehaven.pojo;

import deepbluehaven.pojo.enums.InvoiceStatus;
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

import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Invoice")
public class Invoice {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InvoiceStatus status;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentTransaction> paymentTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    private List<InvoiceStatusLog> statusLogs = new ArrayList<>();

    public Invoice() {}

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

    public Worker getWorker() { 
        return worker; }
    public void setWorker(Worker worker) { 
        this.worker = worker; }

    public BigDecimal getTotalAmount() { 
        return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { 
        this.totalAmount = totalAmount; }

    public BigDecimal getPaidAmount() { 
        return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { 
        this.paidAmount = paidAmount; }

    public InvoiceStatus getStatus() { 
        return status; }
    public void setStatus(InvoiceStatus status) { 
        this.status = status; }

    public LocalDateTime getTimestamp() { 
        return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; }

    public List<PaymentTransaction> getPaymentTransactions() { 
        return paymentTransactions; }
    public void setPaymentTransactions(List<PaymentTransaction> paymentTransactions) { 
        this.paymentTransactions = paymentTransactions; }

    public List<InvoiceStatusLog> getStatusLogs() { 
        return statusLogs; }
    public void setStatusLogs(List<InvoiceStatusLog> statusLogs) { 
        this.statusLogs = statusLogs; }
}
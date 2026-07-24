package deepbluehaven.pojo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import deepbluehaven.pojo.enums.ReferenceType;
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
@Table(name = "Customer_Loyalty_Log")
public class CustomerLoyaltyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 50)
    private ReferenceType referenceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "points_changed", nullable = false)
    private Integer pointsChanged;

    @Column(name = "reason", nullable = false, length = 500, columnDefinition = "NVARCHAR(500)")
    private String reason;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public CustomerLoyaltyLog() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public ReferenceType getReferenceType() { 
        return referenceType; }
    public void setReferenceType(ReferenceType referenceType) { 
        this.referenceType = referenceType; }

    public Customer getCustomer() { 
        return customer; }
    public void setCustomer(Customer customer) { 
        this.customer = customer; }

    public Long getReferenceId() { 
        return referenceId; }
    public void setReferenceId(Long referenceId) { 
        this.referenceId = referenceId; }

    public Integer getPointsChanged() { 
        return pointsChanged; }
    public void setPointsChanged(Integer pointsChanged) { 
        this.pointsChanged = pointsChanged; }

    public String getReason() { 
        return reason; }
    public void setReason(String reason) { 
        this.reason = reason; }

    public LocalDateTime getTimestamp() { 
        return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; }
}

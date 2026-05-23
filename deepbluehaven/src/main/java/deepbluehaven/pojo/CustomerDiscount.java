package deepbluehaven.pojo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import deepbluehaven.pojo.enums.CustomerDiscountStatus;
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
@Table(name = "Customer_Discount")
public class CustomerDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CustomerDiscountStatus status;

    @CreationTimestamp
    @Column(name = "acquired_at", nullable = false, updatable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "used_at", nullable = true)
    private LocalDateTime usedAt;

    @Column(name = "reference_invoice_id", nullable = true)
    private Long referenceInvoiceId;

    public CustomerDiscount() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Customer getCustomer() { 
        return customer; }
    public void setCustomer(Customer customer) { 
        this.customer = customer; }

    public Discount getDiscount() { 
        return discount; }
    public void setDiscount(Discount discount) { 
        this.discount = discount; }

    public CustomerDiscountStatus getStatus() { 
        return status; }
    public void setStatus(CustomerDiscountStatus status) { 
        this.status = status; }

    public LocalDateTime getAcquiredAt() { 
        return acquiredAt; }
    public void setAcquiredAt(LocalDateTime acquiredAt) { 
        this.acquiredAt = acquiredAt; }

    public LocalDateTime getUsedAt() { 
        return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { 
        this.usedAt = usedAt; }

    public Long getReferenceInvoiceId() { 
        return referenceInvoiceId; }
    public void setReferenceInvoiceId(Long referenceInvoiceId) { 
        this.referenceInvoiceId = referenceInvoiceId; }
}
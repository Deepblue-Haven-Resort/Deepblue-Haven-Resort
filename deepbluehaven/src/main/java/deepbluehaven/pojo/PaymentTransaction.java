package deepbluehaven.pojo;

import deepbluehaven.pojo.enums.PaymentMethod;
import deepbluehaven.pojo.enums.PaymentType;
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
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment_Transaction")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    private PaymentType paymentType;

    @Column(name = "transaction_ref", nullable = true, length = 100)
    private String transactionRef;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public PaymentTransaction() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Invoice getInvoice() { 
        return invoice; }
    public void setInvoice(Invoice invoice) { 
        this.invoice = invoice; }

    public BigDecimal getAmount() {
        return amount; }
    public void setAmount(BigDecimal amount) { 
        this.amount = amount; }

    public PaymentMethod getPaymentMethod() { 
        return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { 
        this.paymentMethod = paymentMethod; }

    public PaymentType getPaymentType() { 
        return paymentType; }
    public void setPaymentType(PaymentType paymentType) { 
        this.paymentType = paymentType; }

    public String getTransactionRef() { 
        return transactionRef; }
    public void setTransactionRef(String transactionRef) { 
        this.transactionRef = transactionRef; }

    public String getAction() { 
        return action; }
    public void setAction(String action) { 
        this.action = action; }

    public LocalDateTime getTimestamp() { 
        return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; }
}

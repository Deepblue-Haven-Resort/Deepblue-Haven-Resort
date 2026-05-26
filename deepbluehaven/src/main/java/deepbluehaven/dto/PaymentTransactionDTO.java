package deepbluehaven.dto;

import deepbluehaven.pojo.enums.PaymentMethod;
import deepbluehaven.pojo.enums.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTransactionDTO {

    public static class Request {
        @NotNull(message = "Invoice ID is required")
        private Long invoiceId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
        private BigDecimal amount;

        @NotNull(message = "Payment method is required")
        private PaymentMethod paymentMethod;

        @NotNull(message = "Payment type is required")
        private PaymentType paymentType;

        private String transactionRef;

        @NotBlank(message = "Action is required")
        private String action;

        public Request() {}

        public Long getInvoiceId() { return invoiceId; }
        public void setInvoiceId(Long invoiceId) { 
            this.invoiceId = invoiceId; }

        public BigDecimal getAmount() { return amount; }
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
    }

    public static class Response {
        private Long id;
        private Long invoiceId;
        private BigDecimal amount;
        private PaymentMethod paymentMethod;
        private PaymentType paymentType;
        private String transactionRef;
        private String action;
        private LocalDateTime timestamp;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getInvoiceId() { 
            return invoiceId; }
        public void setInvoiceId(Long invoiceId) { 
            this.invoiceId = invoiceId; }

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
}
package deepbluehaven.pojo;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Auth_Access_Log")
public class AuthAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public AuthAccessLog() {}

    public Long getId() { 
        return id; }
    public void setId(Long id) { 
        this.id = id; }

    public Long getAccountId() { 
        return accountId; }
    public void setAccountId(Long accountId) { 
        this.accountId = accountId; }

    public String getAccountType() { 
        return accountType; }
    public void setAccountType(String accountType) { 
        this.accountType = accountType; }

    public String getAction() { 
        return action; }
    public void setAction(String action) { 
        this.action = action; }

    public String getIpAddress() { 
        return ipAddress; }
    public void setIpAddress(String ipAddress) { 
        this.ipAddress = ipAddress; }

    public String getUserAgent() { 
        return userAgent; }
    public void setUserAgent(String userAgent) { 
        this.userAgent = userAgent; }

    public LocalDateTime getTimestamp() { 
        return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; }
}
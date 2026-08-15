package deepbluehaven.pojo;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import deepbluehaven.pojo.enums.InquiryStatus;
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
@Table(name = "Contact_Inquiry")
public class ContactInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String fullName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "resort_location", length = 150, columnDefinition = "NVARCHAR(150)")
    private String resortLocation;

    @Column(name = "inquiry_type", length = 100, columnDefinition = "NVARCHAR(100)")
    private String inquiryType;

    @Column(name = "message", nullable = false, length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InquiryStatus status = InquiryStatus.PENDING;

    @Column(name = "reply_notes", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String replyNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private Worker resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ContactInquiry() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getResortLocation() {
        return resortLocation;
    }
    public void setResortLocation(String resortLocation) {
        this.resortLocation = resortLocation;
    }

    public String getInquiryType() {
        return inquiryType;
    }
    public void setInquiryType(String inquiryType) {
        this.inquiryType = inquiryType;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public InquiryStatus getStatus() {
        return status;
    }
    public void setStatus(InquiryStatus status) {
        this.status = status;
    }

    public String getReplyNotes() {
        return replyNotes;
    }
    public void setReplyNotes(String replyNotes) {
        this.replyNotes = replyNotes;
    }

    public Worker getResolvedBy() {
        return resolvedBy;
    }
    public void setResolvedBy(Worker resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

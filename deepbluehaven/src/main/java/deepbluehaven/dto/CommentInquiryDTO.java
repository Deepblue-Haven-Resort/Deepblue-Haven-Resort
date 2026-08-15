package deepbluehaven.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import deepbluehaven.pojo.enums.InquiryStatus;

public class CommentInquiryDTO {

    public static class CommentView {
        private Long id;
        private Long customerId;
        private String customerName;
        private String customerAvatar;
        private String targetType; // ROOM, SERVICE, RESORT, GENERAL
        private String targetName;
        private Integer rating;
        private String content;
        private Boolean isComplaint;
        private Boolean isResolved;
        private String response;
        private String respondedByName;
        private LocalDateTime respondedAt;
        private String formattedRespondedAt;
        private List<String> images;
        private LocalDateTime createdAt;
        private String formattedCreatedAt;

        public CommentView() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getCustomerAvatar() { return customerAvatar; }
        public void setCustomerAvatar(String customerAvatar) { this.customerAvatar = customerAvatar; }

        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }

        public String getTargetName() { return targetName; }
        public void setTargetName(String targetName) { this.targetName = targetName; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Boolean getIsComplaint() { return isComplaint; }
        public void setIsComplaint(Boolean isComplaint) { this.isComplaint = isComplaint; }

        public Boolean getIsResolved() { return isResolved; }
        public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }

        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }

        public String getRespondedByName() { return respondedByName; }
        public void setRespondedByName(String respondedByName) { this.respondedByName = respondedByName; }

        public LocalDateTime getRespondedAt() { return respondedAt; }
        public void setRespondedAt(LocalDateTime respondedAt) { 
            this.respondedAt = respondedAt; 
            if (respondedAt != null) {
                this.formattedRespondedAt = respondedAt.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"));
            }
        }

        public String getFormattedRespondedAt() { return formattedRespondedAt; }
        public void setFormattedRespondedAt(String formattedRespondedAt) { this.formattedRespondedAt = formattedRespondedAt; }

        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { 
            this.createdAt = createdAt; 
            if (createdAt != null) {
                this.formattedCreatedAt = createdAt.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"));
            }
        }

        public String getFormattedCreatedAt() { return formattedCreatedAt; }
        public void setFormattedCreatedAt(String formattedCreatedAt) { this.formattedCreatedAt = formattedCreatedAt; }
    }

    public static class InquiryView {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private String resortLocation;
        private String inquiryType;
        private String message;
        private InquiryStatus status;
        private String replyNotes;
        private String resolvedByName;
        private LocalDateTime resolvedAt;
        private String formattedResolvedAt;
        private LocalDateTime createdAt;
        private String formattedCreatedAt;

        public InquiryView() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getResortLocation() { return resortLocation; }
        public void setResortLocation(String resortLocation) { this.resortLocation = resortLocation; }

        public String getInquiryType() { return inquiryType; }
        public void setInquiryType(String inquiryType) { this.inquiryType = inquiryType; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public InquiryStatus getStatus() { return status; }
        public void setStatus(InquiryStatus status) { this.status = status; }

        public String getReplyNotes() { return replyNotes; }
        public void setReplyNotes(String replyNotes) { this.replyNotes = replyNotes; }

        public String getResolvedByName() { return resolvedByName; }
        public void setResolvedByName(String resolvedByName) { this.resolvedByName = resolvedByName; }

        public LocalDateTime getResolvedAt() { return resolvedAt; }
        public void setResolvedAt(LocalDateTime resolvedAt) { 
            this.resolvedAt = resolvedAt; 
            if (resolvedAt != null) {
                this.formattedResolvedAt = resolvedAt.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"));
            }
        }

        public String getFormattedResolvedAt() { return formattedResolvedAt; }
        public void setFormattedResolvedAt(String formattedResolvedAt) { this.formattedResolvedAt = formattedResolvedAt; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { 
            this.createdAt = createdAt; 
            if (createdAt != null) {
                this.formattedCreatedAt = createdAt.format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"));
            }
        }

        public String getFormattedCreatedAt() { return formattedCreatedAt; }
        public void setFormattedCreatedAt(String formattedCreatedAt) { this.formattedCreatedAt = formattedCreatedAt; }
    }

    public static class Statistics {
        private Double averageRating;
        private Long totalComments;
        private Long complaintsCount;
        private Long unansweredCount;
        private Long totalInquiries;
        private Long pendingInquiries;

        public Statistics() {}

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Long getTotalComments() { return totalComments; }
        public void setTotalComments(Long totalComments) { this.totalComments = totalComments; }

        public Long getComplaintsCount() { return complaintsCount; }
        public void setComplaintsCount(Long complaintsCount) { this.complaintsCount = complaintsCount; }

        public Long getUnansweredCount() { return unansweredCount; }
        public void setUnansweredCount(Long unansweredCount) { this.unansweredCount = unansweredCount; }

        public Long getTotalInquiries() { return totalInquiries; }
        public void setTotalInquiries(Long totalInquiries) { this.totalInquiries = totalInquiries; }

        public Long getPendingInquiries() { return pendingInquiries; }
        public void setPendingInquiries(Long pendingInquiries) { this.pendingInquiries = pendingInquiries; }
    }
}

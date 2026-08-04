package deepbluehaven.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import deepbluehaven.pojo.Notification;
import deepbluehaven.pojo.enums.NotificationType;

public class NotificationDTO {

    public static class Response {
        private Long id;
        private String title;
        private String message;
        private NotificationType type;
        private String link;

        @JsonProperty("isRead")
        private Boolean isRead;

        private LocalDateTime createdAt;
        private String timeAgo;

        public Response() {}

        public static Response fromEntity(Notification notification) {
            Response dto = new Response();
            dto.setId(notification.getId());
            dto.setTitle(notification.getTitle());
            dto.setMessage(notification.getMessage());
            dto.setType(notification.getType());
            dto.setLink(notification.getLink());
            dto.setIsRead(notification.getIsRead() != null ? notification.getIsRead() : false);
            dto.setCreatedAt(notification.getCreatedAt());
            dto.setTimeAgo(calculateTimeAgo(notification.getCreatedAt()));
            return dto;
        }

        private static String calculateTimeAgo(LocalDateTime createdAt) {
            if (createdAt == null) return "";
            Duration duration = Duration.between(createdAt, LocalDateTime.now());
            long minutes = Math.max(0, duration.toMinutes());
            if (minutes < 1) return "Vừa xong";
            if (minutes < 60) return minutes + " phút trước";
            long hours = duration.toHours();
            if (hours < 24) return hours + " giờ trước";
            long days = duration.toDays();
            if (days < 7) return days + " ngày trước";
            return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public NotificationType getType() { return type; }
        public void setType(NotificationType type) { this.type = type; }

        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }

        @JsonProperty("isRead")
        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public String getTimeAgo() { return timeAgo; }
        public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    }

    public static class ListResponse {
        private List<Response> notifications;
        private long unreadCount;

        public ListResponse() {}

        public ListResponse(List<Response> notifications, long unreadCount) {
            this.notifications = notifications;
            this.unreadCount = unreadCount;
        }

        public List<Response> getNotifications() { return notifications; }
        public void setNotifications(List<Response> notifications) { this.notifications = notifications; }

        public long getUnreadCount() { return unreadCount; }
        public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
    }
}

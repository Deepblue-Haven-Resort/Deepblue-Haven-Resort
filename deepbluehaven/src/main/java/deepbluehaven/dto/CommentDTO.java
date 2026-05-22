package deepbluehaven.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CommentDTO {

    public static class Request {
        @NotNull(message = "Customer ID is strictly required")
        private Long customerId;

        private Long resortId;
        private Long roomId;
        private Long serviceId;
        private Long workerId;

        @NotBlank(message = "Content cannot be blank")
        private String content;

        @NotNull(message = "Rating is strictly required")
        @Min(value = 1, message = "Rating must be at least 1 star")
        @Max(value = 5, message = "Rating cannot exceed 5 stars")
        private Integer rating;

        @NotEmpty(message = "You must upload at least one image for the review")
        private List<String> images;

        public Request() {}

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getResortId() { 
            return resortId; }
        public void setResortId(Long resortId) { 
            this.resortId = resortId; }

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }

        public Long getServiceId() { 
            return serviceId; }
        public void setServiceId(Long serviceId) { 
            this.serviceId = serviceId; }

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public String getContent() { 
            return content; }
        public void setContent(String content) { 
            this.content = content; }

        public Integer getRating() { 
            return rating; }
        public void setRating(Integer rating) { 
            this.rating = rating; }

        public List<String> getImages() { 
            return images; }
        public void setImages(List<String> images) { 
            this.images = images; }
    }

    public static class Response {
        private Long id;
        private Long customerId;
        private Long resortId;
        private Long roomId;
        private Long serviceId;
        private Long workerId;
        private String content;
        private Integer rating;
        private List<String> images;
        private LocalDateTime createdAt;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public Long getResortId() { 
            return resortId; }
        public void setResortId(Long resortId) { 
            this.resortId = resortId; }

        public Long getRoomId() {
            return roomId; }
        public void setRoomId(Long roomId) {
             this.roomId = roomId; }

        public Long getServiceId() { 
            return serviceId; }
        public void setServiceId(Long serviceId) { 
            this.serviceId = serviceId; }

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public String getContent() { 
            return content; }
        public void setContent(String content) { 
            this.content = content; }

        public Integer getRating() { 
            return rating; }
        public void setRating(Integer rating) { 
            this.rating = rating; }

        public List<String> getImages() { 
            return images; }
        public void setImages(List<String> images) { 
            this.images = images; }

        public LocalDateTime getCreatedAt() { 
            return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { 
            this.createdAt = createdAt; }
    }
}
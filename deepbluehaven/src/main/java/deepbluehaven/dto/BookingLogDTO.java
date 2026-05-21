package deepbluehaven.dto;

import java.time.LocalDateTime;

import deepbluehaven.pojo.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;

public class BookingLogDTO {

    public static class Request {
        @NotNull(message = "Booking ID is required")
        private Long bookingId;

        @NotNull(message = "Actor ID is required")
        private Long actorId;

        private BookingStatus previousStatus;

        @NotNull(message = "Current status is required")
        private BookingStatus currentStatus;

        private String note;

        public Request() {}

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public Long getActorId() { return actorId; }
        public void setActorId(Long actorId) { this.actorId = actorId; }

        public BookingStatus getPreviousStatus() { return previousStatus; }
        public void setPreviousStatus(BookingStatus previousStatus) { this.previousStatus = previousStatus; }

        public BookingStatus getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(BookingStatus currentStatus) { this.currentStatus = currentStatus; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class Response {
        private Long id;
        private Long bookingId;
        private Long actorId;
        private BookingStatus previousStatus;
        private BookingStatus currentStatus;
        private String note;
        private LocalDateTime timestamp;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getBookingId() { 
            return bookingId; }
        public void setBookingId(Long bookingId) { 
            this.bookingId = bookingId; }

        public Long getActorId() { 
            return actorId; }
        public void setActorId(Long actorId) { 
            this.actorId = actorId; }

        public BookingStatus getPreviousStatus() { 
            return previousStatus; }
        public void setPreviousStatus(BookingStatus previousStatus) { 
            this.previousStatus = previousStatus; }

        public BookingStatus getCurrentStatus() { 
            return currentStatus; }
        public void setCurrentStatus(BookingStatus currentStatus) { 
            this.currentStatus = currentStatus; }

        public String getNote() { 
            return note; }
        public void setNote(String note) { 
            this.note = note; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }
    }
}

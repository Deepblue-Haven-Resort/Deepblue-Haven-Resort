package deepbluehaven.dto;

public class RoomHighlightDTO {

    public static class Request {
        private Long roomId;
        private String title;
        private String description;

        public Request() {}
        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }
        public String getTitle() { 
            return title; }
        public void setTitle(String title) { 
            this.title = title; }
        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }
    }

    public static class Response {
        private Long id;
        private Long roomId;
        private String title;
        private String description;

        public Response() {}
        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }
        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) { 
            this.roomId = roomId; }
        public String getTitle() { 
            return title; }
        public void setTitle(String title) { 
            this.title = title; }
        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }
    }
}
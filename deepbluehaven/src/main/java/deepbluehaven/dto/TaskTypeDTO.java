package deepbluehaven.dto;

public class TaskTypeDTO {

    public static class Request {
        private String name;
        private String description;
        private Integer requiredLevel;
        private Boolean isActive;

        public Request() {}
        public String getName() { 
            return name; }
        public void setName(String name) { 
            this.name = name; }
        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }
        public Integer getRequiredLevel() { 
            return requiredLevel; }
        public void setRequiredLevel(Integer requiredLevel) { 
            this.requiredLevel = requiredLevel; }
        public Boolean getIsActive() { 
            return isActive; }
        public void setIsActive(Boolean isActive) { 
            this.isActive = isActive; }
    }

    public static class Response {
        private Long id;
        private String name;
        private String description;
        private Integer requiredLevel;
        private Boolean isActive;

        public Response() {}
        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }
        public String getName() { 
            return name; }
        public void setName(String name) { 
            this.name = name; }
        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }
        public Integer getRequiredLevel() { 
            return requiredLevel; }
        public void setRequiredLevel(Integer requiredLevel) { 
            this.requiredLevel = requiredLevel; }
        public Boolean getIsActive() { 
            return isActive; }
        public void setIsActive(Boolean isActive) { 
            this.isActive = isActive; }
    }
}
package deepbluehaven.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResortDTO {

    public static class Request {
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name cannot exceed 150 characters")
        private String name;

        @NotBlank(message = "Location is required")
        @Size(max = 255, message = "Location cannot exceed 255 characters")
        private String location;

        private String script;

        public Request() {}

        public String getName() { 
            return name; }
        public void setName(String name) { 
            this.name = name; }

        public String getLocation() { 
            return location; }
        public void setLocation(String location) { 
            this.location = location; }

        public String getScript() { 
            return script; }
        public void setScript(String script) { 
            this.script = script; }
    }

    public static class Response {
        private Long id;
        private String name;
        private String location;
        private String script;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public String getName() { 
            return name; }
        public void setName(String name) { 
            this.name = name; }

        public String getLocation() { 
            return location; }
        public void setLocation(String location) { 
            this.location = location; }

        public String getScript() { 
            return script; }
        public void setScript(String script) { 
            this.script = script; }
    }
}

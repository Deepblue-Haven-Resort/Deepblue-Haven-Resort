package deepbluehaven.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import deepbluehaven.pojo.enums.WorkerStatus;

public class WorkerDTO {

    public static class Request {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        @NotNull(message = "Status is required")
        private WorkerStatus status;

        public Request() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public WorkerStatus getStatus() { return status; }
        public void setStatus(WorkerStatus status) { this.status = status; }
    }

    public static class Response {
        private Long id;
        private String username;
        private WorkerStatus status;
        private String fullName;   
        private String role;
        private String phoneNumber;
        private String email;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public String getUsername() { 
            return username; }
        public void setUsername(String username) { 
            this.username = username; }

        public WorkerStatus getStatus() { 
            return status; }
        public void setStatus(WorkerStatus status) { 
            this.status = status; }
    }
}
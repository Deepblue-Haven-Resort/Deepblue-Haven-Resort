package deepbluehaven.dto;

import deepbluehaven.pojo.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WorkerProfileDTO {

    public static class Request {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotNull(message = "Role is required")
        private Role role;

        @NotNull(message = "Role level is required")
        private Integer roleLevel;

        @NotBlank(message = "Phone number is required")
        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        private String phoneNumber;

        @Email(message = "Must be a valid email format")
        private String email;

        public Request() {}

        public String getFullName() { 
            return fullName; }
        public void setFullName(String fullName) { 
            this.fullName = fullName; }

        public Role getRole() { 
            return role; }
        public void setRole(Role role) { 
            this.role = role; }

        public Integer getRoleLevel() { 
            return roleLevel; }
        public void setRoleLevel(Integer roleLevel) { 
            this.roleLevel = roleLevel; }

        public String getPhoneNumber() { 
            return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { 
            this.phoneNumber = phoneNumber; }

        public String getEmail() { 
            return email; }
        public void setEmail(String email) { 
            this.email = email; }
    }

    public static class Response {
        private Long workerId;
        private String fullName;
        private Role role;
        private Integer roleLevel;
        private String phoneNumber;
        private String email;

        public Response() {}

        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }

        public String getFullName() { 
            return fullName; }
        public void setFullName(String fullName) { 
            this.fullName = fullName; }

        public Role getRole() { 
            return role; }
        public void setRole(Role role) { 
            this.role = role; }

        public Integer getRoleLevel() { 
            return roleLevel; }
        public void setRoleLevel(Integer roleLevel) { 
            this.roleLevel = roleLevel; }

        public String getPhoneNumber() { 
            return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { 
            this.phoneNumber = phoneNumber; }

        public String getEmail() {
             return email; }
        public void setEmail(String email) {
             this.email = email; }
    }
}
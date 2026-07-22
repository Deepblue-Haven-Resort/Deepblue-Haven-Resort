package deepbluehaven.dto;
import java.time.LocalDateTime;
import java.util.List;

import deepbluehaven.pojo.enums.PermissionTag;
import deepbluehaven.pojo.enums.WorkerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WorkerDTO {

    public static class Request {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        @NotNull(message = "Status is required")
        private WorkerStatus status;

        @NotNull(message = "Role level is required")
        private Integer roleLevel;
        @NotNull(message = "At least one role tag is required")
        private List<PermissionTag> roleTags;

        private LocalDateTime createdAt;

        public Request() {}

        public String getUsername() { 
            return username; }
        public void setUsername(String username) { 
            this.username = username; }

        public String getPassword() { 
            return password; }
        public void setPassword(String password) { 
            this.password = password; }

        public WorkerStatus getStatus() { 
            return status; }
        public void setStatus(WorkerStatus status) {
            this.status = status; }

        public Integer getRoleLevel() { 
            return roleLevel; }
        public void setRoleLevel(Integer roleLevel) { 
            this.roleLevel = roleLevel; }
        public List<PermissionTag> getRoleTags() { 
            return roleTags; }
        public void setRoleTags(List<PermissionTag> roleTags) { 
            this.roleTags = roleTags; }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class Response {
        private Long id;
        private String username;
        private WorkerStatus status;
        private String fullName;   
        private String role;
        private String phoneNumber;
        private String email;
        private Integer roleLevel;
        private List<PermissionTag> roleTags;
        private LocalDateTime createdAt;

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
        public Integer getRoleLevel() { 
            return roleLevel; }
        public void setRoleLevel(Integer roleLevel) { 
            this.roleLevel = roleLevel; }
        public List<PermissionTag> getRoleTags() { 
            return roleTags; }
        public void setRoleTags(List<PermissionTag> roleTags) { 
            this.roleTags = roleTags; }
                 public String getFullName() {
            return fullName; }
        public void setFullName(String fullName) {
            this.fullName = fullName; }
 
        public String getRole() {
            return role; }
        public void setRole(String role) {
            this.role = role; }
 
        public String getPhoneNumber() {
            return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber; }
 
        public String getEmail() {
            return email; }
        public void setEmail(String email) {
            this.email = email; }
        public LocalDateTime getCreatedAt() {
            return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt; }
    }

}
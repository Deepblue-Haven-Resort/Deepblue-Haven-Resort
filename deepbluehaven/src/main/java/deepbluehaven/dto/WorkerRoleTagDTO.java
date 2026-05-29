package deepbluehaven.dto;

import deepbluehaven.pojo.enums.PermissionTag;

public class WorkerRoleTagDTO {

    public static class Request {
        private Long workerId;
        private PermissionTag roleTag;
        private String description;

        public Request() {}
        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }
        public PermissionTag getRoleTag() { 
            return roleTag; }
        public void setRoleTag(PermissionTag roleTag) { 
            this.roleTag = roleTag; }
        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }
    }

    public static class Response {
        private Long id;
        private Long workerId;
        private PermissionTag roleTag;
        private String description;

        public Response() {}
        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }
        public Long getWorkerId() { 
            return workerId; }
        public void setWorkerId(Long workerId) { 
            this.workerId = workerId; }
        public PermissionTag getRoleTag() { 
            return roleTag; }
        public void setRoleTag(PermissionTag roleTag) { 
            this.roleTag = roleTag; }
        public String getDescription() { 
            return description; }
        public void setDescription(String description) { 
            this.description = description; }
    }
}
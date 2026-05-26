package deepbluehaven.dto;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthAccessLogDTO {

    public static class Request {
        @NotNull(message = "Account ID is required")
        private Long accountId;

        @NotBlank(message = "Account Type is required")
        private String accountType;

        @NotBlank(message = "Action is required")
        private String action;

        @NotBlank(message = "IP Address is required")
        @Size(max = 45, message = "IP Address cannot exceed 45 characters")
        private String ipAddress;

        private String userAgent;

        public Request() {}

        public Long getAccountId() { 
            return accountId; }
        public void setAccountId(Long accountId) { 
            this.accountId = accountId; }

        public String getAccountType() { 
            return accountType; }
        public void setAccountType(String accountType) { 
            this.accountType = accountType; }

        public String getAction() { 
            return action; }
        public void setAction(String action) { 
            this.action = action; }

        public String getIpAddress() { 
            return ipAddress; }
        public void setIpAddress(String ipAddress) { 
            this.ipAddress = ipAddress; }

        public String getUserAgent() { 
            return userAgent; }
        public void setUserAgent(String userAgent) { 
            this.userAgent = userAgent; }
    }

    public static class Response {
        private Long id;
        private Long accountId;
        private String accountType;
        private String action;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime timestamp;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getAccountId() { 
            return accountId; }
        public void setAccountId(Long accountId) { 
            this.accountId = accountId; }

        public String getAccountType() { 
            return accountType; }
        public void setAccountType(String accountType) { 
            this.accountType = accountType; }

        public String getAction() { 
            return action; }
        public void setAction(String action) { 
            this.action = action; }

        public String getIpAddress() { 
            return ipAddress; }
        public void setIpAddress(String ipAddress) { 
            this.ipAddress = ipAddress; }

        public String getUserAgent() { 
            return userAgent; }
        public void setUserAgent(String userAgent) { 
            this.userAgent = userAgent; }

        public LocalDateTime getTimestamp() { 
            return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { 
            this.timestamp = timestamp; }
    }
}
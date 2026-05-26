package deepbluehaven.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public class CustomerProfileDTO {

    public static class Request {
        @Past(message = "Birth day must be in the past")
        private LocalDate birthDay;

        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name cannot exceed 120 characters")
        private String fullName;

        @NotBlank(message = "Phone number is required")
        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        private String phoneNumber;

        @Email(message = "Must be a valid email format")
        private String email;

        public Request() {}

        public LocalDate getBirthDay() { 
            return birthDay; }
        public void setBirthDay(LocalDate birthDay) { 
            this.birthDay = birthDay; }

        public String getFullName() { 
            return fullName; }
        public void setFullName(String fullName) { 
            this.fullName = fullName; }

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
        private Long customerId;
        private LocalDate birthDay;
        private String fullName;
        private String phoneNumber;
        private String email;
        private Integer totalBookings;
        private BigDecimal totalSpent;
        private Integer totalPoints;
        private String segment;
        private Long membershipTierId;

        public Response() {}

        public Long getCustomerId() { 
            return customerId; }
        public void setCustomerId(Long customerId) { 
            this.customerId = customerId; }

        public LocalDate getBirthDay() { 
            return birthDay; }
        public void setBirthDay(LocalDate birthDay) { 
            this.birthDay = birthDay; }

        public String getFullName() { 
            return fullName; }
        public void setFullName(String fullName) { 
            this.fullName = fullName; }

        public String getPhoneNumber() { 
            return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { 
            this.phoneNumber = phoneNumber; }

        public String getEmail() { 
            return email; }
        public void setEmail(String email) { 
            this.email = email; }

        public Integer getTotalBookings() { 
            return totalBookings; }
        public void setTotalBookings(Integer totalBookings) { 
            this.totalBookings = totalBookings; }

        public BigDecimal getTotalSpent() { 
            return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { 
            this.totalSpent = totalSpent; }

        public Integer getTotalPoints() { 
            return totalPoints; }
        public void setTotalPoints(Integer totalPoints) { 
            this.totalPoints = totalPoints; }

        public String getSegment() { 
            return segment; }
        public void setSegment(String segment) { 
            this.segment = segment; }

        public Long getMembershipTierId() { 
            return membershipTierId; }
        public void setMembershipTierId(Long membershipTierId) { 
            this.membershipTierId = membershipTierId; }
    }
}
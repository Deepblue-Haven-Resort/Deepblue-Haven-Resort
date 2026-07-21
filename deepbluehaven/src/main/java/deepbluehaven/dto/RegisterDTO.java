package deepbluehaven.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDTO {

    public static class Request {

        @NotBlank(message = "Username is required")
        @Size(max = 100, message = "Username cannot exceed 100 characters")
        private String username;

        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name cannot exceed 120 characters")
        private String fullName;

        @NotBlank(message = "Email or phone number is required")
        @Size(max = 120, message = "Contact info cannot exceed 120 characters")
        private String contact;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotBlank(message = "Please confirm your password")
        private String confirmPassword;

        private boolean terms;

        public Request() {}

        @AssertTrue(message = "Passwords do not match")
        public boolean isPasswordMatching() {
            return password != null && password.equals(confirmPassword);
        }

        @AssertTrue(message = "You must agree to the Terms and Privacy Policy")
        public boolean isTermsAccepted() {
            return terms;
        }

        public String getUsername() { 
            return username; }
        public void setUsername(String username) { 
            this.username = username; }

        public String getFullName() { 
            return fullName; }
        public void setFullName(String fullName) { 
            this.fullName = fullName; }

        public String getContact() { 
            return contact; }
        public void setContact(String contact) { 
            this.contact = contact; }

        public String getPassword() { 
            return password; }
        public void setPassword(String password) { 
            this.password = password; }

        public String getConfirmPassword() { 
            return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { 
            this.confirmPassword = confirmPassword; }

        public boolean isTerms() { 
            return terms; }
        public void setTerms(boolean terms) { 
            this.terms = terms; }
    }
}
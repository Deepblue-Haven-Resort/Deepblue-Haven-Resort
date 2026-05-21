package deepbluehaven.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupplierDTO {

    public static class Request {
        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Name cannot exceed 150 characters")
        private String name;

        @NotBlank(message = "Phone number is required")
        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        private String phoneNumber;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email format")
        private String email;

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address cannot exceed 255 characters")
        private String address;

        public Request() {}

        public String getName() { 
            return name; }
        public void setName(String name) { 
            this.name = name; }

        public String getPhoneNumber() { 
            return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { 
            this.phoneNumber = phoneNumber; }

        public String getEmail() { 
            return email; }
        public void setEmail(String email) { 
            this.email = email; }

        public String getAddress() { 
            return address; }
        public void setAddress(String address) { 
            this.address = address; }
    }

    public static class Response {
        private Long id;
        private String name;
        private String phoneNumber;
        private String email;
        private String address;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public String getName() { 
            return name; }
        public void setName(String name) { 
            this.name = name; }

        public String getPhoneNumber() { 
            return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { 
            this.phoneNumber = phoneNumber; }

        public String getEmail() { 
            return email; }
        public void setEmail(String email) { 
            this.email = email; }

        public String getAddress() { 
            return address; }
        public void setAddress(String address) { 
            this.address = address; }
    }
}
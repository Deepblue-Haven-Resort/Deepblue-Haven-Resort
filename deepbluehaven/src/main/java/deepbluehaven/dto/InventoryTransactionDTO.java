package deepbluehaven.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InventoryTransactionDTO {

    public static class Request {
        @NotNull(message = "Item ID is required")
        private Long itemId;

        @NotNull(message = "Change amount is required")
        private Integer changeAmount;

        @NotBlank(message = "Reason is required")
        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        private String reason;

        public Request() {}

        public Long getItemId() { 
            return itemId; }
        public void setItemId(Long itemId) { 
            this.itemId = itemId; }

        public Integer getChangeAmount() { 
            return changeAmount; }
        public void setChangeAmount(Integer changeAmount) { 
            this.changeAmount = changeAmount; }

        public String getReason() { 
            return reason; }
        public void setReason(String reason) { 
            this.reason = reason; }
    }

    public static class Response {
        private Long id;
        private Long itemId;
        private Integer changeAmount;
        private String reason;

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getItemId() { 
            return itemId; }
        public void setItemId(Long itemId) { 
            this.itemId = itemId; }

        public Integer getChangeAmount() { 
            return changeAmount; }
        public void setChangeAmount(Integer changeAmount) { 
            this.changeAmount = changeAmount; }

        public String getReason() { 
            return reason; }
        public void setReason(String reason) { 
            this.reason = reason; }
    }
}
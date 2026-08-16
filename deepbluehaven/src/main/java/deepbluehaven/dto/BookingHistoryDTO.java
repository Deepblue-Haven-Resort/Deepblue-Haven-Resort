package deepbluehaven.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import deepbluehaven.pojo.enums.BookingStatus;

public class BookingHistoryDTO {

    public static class ServiceItem {
        private Long orderId;
        private String serviceName;
        private int quantity;
        private BigDecimal unitPriceVnd;
        private BigDecimal totalAmountVnd;
        private String status;
        private boolean canCancel;

        public ServiceItem() {}

        public ServiceItem(Long orderId, String serviceName, int quantity, BigDecimal unitPriceVnd, BigDecimal totalAmountVnd, String status, boolean canCancel) {
            this.orderId = orderId;
            this.serviceName = serviceName;
            this.quantity = quantity;
            this.unitPriceVnd = unitPriceVnd;
            this.totalAmountVnd = totalAmountVnd;
            this.status = status;
            this.canCancel = canCancel;
        }

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPriceVnd() { return unitPriceVnd; }
        public void setUnitPriceVnd(BigDecimal unitPriceVnd) { this.unitPriceVnd = unitPriceVnd; }

        public BigDecimal getTotalAmountVnd() { return totalAmountVnd; }
        public void setTotalAmountVnd(BigDecimal totalAmountVnd) { this.totalAmountVnd = totalAmountVnd; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public boolean isCanCancel() { return canCancel; }
        public void setCanCancel(boolean canCancel) { this.canCancel = canCancel; }
    }

    public static class Response {
        private Long id;
        private Long roomId;
        private String bookingCode;
        private String roomName;
        private String roomType;
        private String roomNumber;
        private String roomImageUrl;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private long nights;
        private LocalDateTime bookedOn;
        private BigDecimal totalAmountVnd;
        private BigDecimal totalAmountUsd;
        private BookingStatus rawStatus;
        private String timeGroup;
        private String statusText;
        private String statusClass;
        private boolean needsManualCheckIn;
        private BigDecimal pricePerNightVnd;
        private BigDecimal roomChargeVnd;
        private BigDecimal serviceChargeVnd;
        private BigDecimal taxVnd;
        private BigDecimal discountVnd;
        private String specialRequest;

        private String guestName;
        private String guestEmail;
        private String guestPhone;

        private List<ServiceItem> serviceItems = new ArrayList<>();

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

        public Long getRoomId() { 
            return roomId; }
        public void setRoomId(Long roomId) {
            this.roomId = roomId; }

        public String getBookingCode() { 
            return bookingCode; }
        public void setBookingCode(String bookingCode) { 
            this.bookingCode = bookingCode; }

        public String getRoomName() { 
            return roomName; }
        public void setRoomName(String roomName) { 
            this.roomName = roomName; }

        public String getRoomType() { 
            return roomType; }
        public void setRoomType(String roomType) { 
            this.roomType = roomType; }

        public String getRoomNumber() { 
            return roomNumber; }
        public void setRoomNumber(String roomNumber) { 
            this.roomNumber = roomNumber; }

        public String getRoomImageUrl() { 
            return roomImageUrl; }
        public void setRoomImageUrl(String roomImageUrl) { 
            this.roomImageUrl = roomImageUrl; }

        public LocalDate getCheckIn() { 
            return checkIn; }
        public void setCheckIn(LocalDate checkIn) { 
            this.checkIn = checkIn; }

        public LocalDate getCheckOut() { 
            return checkOut; }
        public void setCheckOut(LocalDate checkOut) { 
            this.checkOut = checkOut; }

        public long getNights() { 
            return nights; }
        public void setNights(long nights) { 
            this.nights = nights; }

        public LocalDateTime getBookedOn() { 
            return bookedOn; }
        public void setBookedOn(LocalDateTime bookedOn) { 
            this.bookedOn = bookedOn; }

        public BigDecimal getTotalAmountVnd() { 
            return totalAmountVnd; }
        public void setTotalAmountVnd(BigDecimal totalAmountVnd) { 
            this.totalAmountVnd = totalAmountVnd; }

        public BigDecimal getTotalAmountUsd() { 
            return totalAmountUsd; }
        public void setTotalAmountUsd(BigDecimal totalAmountUsd) { 
            this.totalAmountUsd = totalAmountUsd; }

        public BookingStatus getRawStatus() { 
            return rawStatus; }
        public void setRawStatus(BookingStatus rawStatus) { 
            this.rawStatus = rawStatus; }

        public String getTimeGroup() { 
            return timeGroup; }
        public void setTimeGroup(String timeGroup) { 
            this.timeGroup = timeGroup; }

        public String getStatusText() { 
            return statusText; }
        public void setStatusText(String statusText) { 
            this.statusText = statusText; }

        public String getStatusClass() { 
            return statusClass; }
        public void setStatusClass(String statusClass) { 
            this.statusClass = statusClass; }

        public boolean isNeedsManualCheckIn() { 
            return needsManualCheckIn; }
        public void setNeedsManualCheckIn(boolean needsManualCheckIn) { 
            this.needsManualCheckIn = needsManualCheckIn; }

        public BigDecimal getPricePerNightVnd() { 
            return pricePerNightVnd; }
        public void setPricePerNightVnd(BigDecimal pricePerNightVnd) { 
            this.pricePerNightVnd = pricePerNightVnd; }

        public BigDecimal getRoomChargeVnd() { 
            return roomChargeVnd; }
        public void setRoomChargeVnd(BigDecimal roomChargeVnd) { 
            this.roomChargeVnd = roomChargeVnd; }

        public BigDecimal getServiceChargeVnd() { 
            return serviceChargeVnd; }
        public void setServiceChargeVnd(BigDecimal serviceChargeVnd) { 
            this.serviceChargeVnd = serviceChargeVnd; }

        public BigDecimal getTaxVnd() { 
            return taxVnd; }
        public void setTaxVnd(BigDecimal taxVnd) { 
            this.taxVnd = taxVnd; }

        public BigDecimal getDiscountVnd() { 
            return discountVnd; }
        public void setDiscountVnd(BigDecimal discountVnd) { 
            this.discountVnd = discountVnd; }

        public BigDecimal getDepositAmountVnd() {
            return totalAmountVnd != null ? totalAmountVnd.multiply(new BigDecimal("0.30")) : BigDecimal.ZERO;
        }

        public BigDecimal getRemainingAmountVnd() {
            return totalAmountVnd != null ? totalAmountVnd.multiply(new BigDecimal("0.70")) : BigDecimal.ZERO;
        }

        public String getSpecialRequest() { 
            return specialRequest; }
        public void setSpecialRequest(String specialRequest) { 
            this.specialRequest = specialRequest; }

        public String getGuestName() { 
            return guestName; }
        public void setGuestName(String guestName) { 
            this.guestName = guestName; }

        public String getGuestEmail() { 
            return guestEmail; }
        public void setGuestEmail(String guestEmail) { 
            this.guestEmail = guestEmail; }

        public String getGuestPhone() { 
            return guestPhone; }
        public void setGuestPhone(String guestPhone) { 
            this.guestPhone = guestPhone; }

        public List<ServiceItem> getServiceItems() { 
            return serviceItems; }
        public void setServiceItems(List<ServiceItem> serviceItems) { 
            this.serviceItems = serviceItems; }

        public String getServiceItemsJson() {
            if (serviceItems == null || serviceItems.isEmpty()) return "[]";
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < serviceItems.size(); i++) {
                ServiceItem item = serviceItems.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                  .append("\"orderId\":").append(item.getOrderId()).append(",")
                  .append("\"serviceName\":\"").append(item.getServiceName() != null ? item.getServiceName().replace("\"", "\\\"") : "").append("\",")
                  .append("\"quantity\":").append(item.getQuantity()).append(",")
                  .append("\"unitPriceVnd\":\"").append(item.getUnitPriceVnd() != null ? String.format("%,d VND", item.getUnitPriceVnd().longValue()) : "0 VND").append("\",")
                  .append("\"totalAmountVnd\":\"").append(item.getTotalAmountVnd() != null ? String.format("%,d VND", item.getTotalAmountVnd().longValue()) : "0 VND").append("\",")
                  .append("\"status\":\"").append(item.getStatus() != null ? item.getStatus() : "CONFIRMED").append("\",")
                  .append("\"canCancel\":").append(item.isCanCancel())
                  .append("}");
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
package deepbluehaven.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import deepbluehaven.pojo.enums.BookingStatus;

public class BookingHistoryDTO {

    public static class Response {
        private Long id;
        private String bookingCode;
        private String roomName;
        private String roomType;
        private String roomNumber;
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

        public Response() {}

        public Long getId() { 
            return id; }
        public void setId(Long id) { 
            this.id = id; }

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

        public String getSpecialRequest() {
            return specialRequest; }
        public void setSpecialRequest(String specialRequest) {
            this.specialRequest = specialRequest; }
    }
}
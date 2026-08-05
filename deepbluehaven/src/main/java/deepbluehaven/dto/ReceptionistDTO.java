package deepbluehaven.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.PaymentMethod;
import deepbluehaven.pojo.enums.RoomStatus;

public class ReceptionistDTO {

    public static class DashboardView {
        private int pendingCheckInsCount = 0;
        private int pendingCheckOutsCount = 0;
        private int inHouseGuestsCount = 0;
        private int availableRoomsCount = 0;

        private List<CheckInQueueItem> checkInQueue = new ArrayList<>();
        private List<CheckOutQueueItem> checkOutQueue = new ArrayList<>();
        private List<ActivityItem> recentActivities = new ArrayList<>();
        private List<AvailableRoomOption> availableRoomsList = new ArrayList<>();

        public int getPendingCheckInsCount() { return pendingCheckInsCount; }
        public void setPendingCheckInsCount(int pendingCheckInsCount) { this.pendingCheckInsCount = pendingCheckInsCount; }

        public int getPendingCheckOutsCount() { return pendingCheckOutsCount; }
        public void setPendingCheckOutsCount(int pendingCheckOutsCount) { this.pendingCheckOutsCount = pendingCheckOutsCount; }

        public int getInHouseGuestsCount() { return inHouseGuestsCount; }
        public void setInHouseGuestsCount(int inHouseGuestsCount) { this.inHouseGuestsCount = inHouseGuestsCount; }

        public int getAvailableRoomsCount() { return availableRoomsCount; }
        public void setAvailableRoomsCount(int availableRoomsCount) { this.availableRoomsCount = availableRoomsCount; }

        public List<CheckInQueueItem> getCheckInQueue() { return checkInQueue; }
        public void setCheckInQueue(List<CheckInQueueItem> checkInQueue) { this.checkInQueue = checkInQueue; }

        public List<CheckOutQueueItem> getCheckOutQueue() { return checkOutQueue; }
        public void setCheckOutQueue(List<CheckOutQueueItem> checkOutQueue) { this.checkOutQueue = checkOutQueue; }

        public List<ActivityItem> getRecentActivities() { return recentActivities; }
        public void setRecentActivities(List<ActivityItem> recentActivities) { this.recentActivities = recentActivities; }

        public List<AvailableRoomOption> getAvailableRoomsList() { return availableRoomsList; }
        public void setAvailableRoomsList(List<AvailableRoomOption> availableRoomsList) { this.availableRoomsList = availableRoomsList; }
    }

    public static class CheckInQueueItem {
        private Long bookingId;
        private String bookingCode;
        private String customerName;
        private String customerPhone;
        private String customerEmail;
        private String roomTypeName;
        private Long assignedRoomId;
        private String assignedRoomNumber;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private long nights = 1;
        private String totalAmountStr;
        private String specialNote;
        private BookingStatus rawStatus;
        private List<AvailableRoomOption> availableRooms = new ArrayList<>();

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public String getBookingCode() { return bookingCode; }
        public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

        public String getRoomTypeName() { return roomTypeName; }
        public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

        public Long getAssignedRoomId() { return assignedRoomId; }
        public void setAssignedRoomId(Long assignedRoomId) { this.assignedRoomId = assignedRoomId; }

        public String getAssignedRoomNumber() { return assignedRoomNumber; }
        public void setAssignedRoomNumber(String assignedRoomNumber) { this.assignedRoomNumber = assignedRoomNumber; }

        public LocalDate getCheckInDate() { return checkInDate; }
        public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

        public LocalDate getCheckOutDate() { return checkOutDate; }
        public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

        public long getNights() { return nights; }
        public void setNights(long nights) { this.nights = nights; }

        public String getTotalAmountStr() { return totalAmountStr; }
        public void setTotalAmountStr(String totalAmountStr) { this.totalAmountStr = totalAmountStr; }

        public String getSpecialNote() { return specialNote; }
        public void setSpecialNote(String specialNote) { this.specialNote = specialNote; }

        public BookingStatus getRawStatus() { return rawStatus; }
        public void setRawStatus(BookingStatus rawStatus) { this.rawStatus = rawStatus; }

        public List<AvailableRoomOption> getAvailableRooms() { return availableRooms; }
        public void setAvailableRooms(List<AvailableRoomOption> availableRooms) { this.availableRooms = availableRooms; }
    }

    public static class AvailableRoomOption {
        private Long roomId;
        private String roomNumber;
        private String roomTypeName;
        private String basePriceStr;

        public AvailableRoomOption() {}
        public AvailableRoomOption(Long roomId, String roomNumber, String roomTypeName, String basePriceStr) {
            this.roomId = roomId;
            this.roomNumber = roomNumber;
            this.roomTypeName = roomTypeName;
            this.basePriceStr = basePriceStr;
        }

        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

        public String getRoomTypeName() { return roomTypeName; }
        public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

        public String getBasePriceStr() { return basePriceStr; }
        public void setBasePriceStr(String basePriceStr) { this.basePriceStr = basePriceStr; }
    }

    public static class CheckInRequest {
        private Long bookingId;
        private Long roomId;
        private String notes;

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class WalkInBookingRequest {
        private String fullName;
        private String phone;
        private String email;
        private Long roomId;
        private String checkInDate;
        private String checkOutDate;
        private String notes;
        private String paymentMethod = "CREDIT_CARD";

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }

        public String getCheckInDate() { return checkInDate; }
        public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }

        public String getCheckOutDate() { return checkOutDate; }
        public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    public static class CheckOutQueueItem {
        private Long bookingId;
        private String bookingCode;
        private String customerName;
        private String customerPhone;
        private String roomNumber;
        private String roomTypeName;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private long nightsStayed = 1;
        private BigDecimal roomCharge = BigDecimal.ZERO;
        private BigDecimal serviceCharge = BigDecimal.ZERO;
        private BigDecimal discountAmount = BigDecimal.ZERO;
        private BigDecimal taxAmount = BigDecimal.ZERO;
        private BigDecimal totalFolio = BigDecimal.ZERO;
        private String roomChargeStr;
        private String serviceChargeStr;
        private String discountAmountStr;
        private String taxStr;
        private String totalFolioStr;
        private BigDecimal pricingRuleMultiplier = BigDecimal.ONE;
        private String pricingRuleNote;
        private String discountCode;
        private Long appliedCustomerDiscountId;
        private List<ServiceOrderItem> serviceOrders = new ArrayList<>();

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public String getBookingCode() { return bookingCode; }
        public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

        public String getRoomTypeName() { return roomTypeName; }
        public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

        public LocalDate getCheckInDate() { return checkInDate; }
        public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

        public LocalDate getCheckOutDate() { return checkOutDate; }
        public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

        public long getNightsStayed() { return nightsStayed; }
        public void setNightsStayed(long nightsStayed) { this.nightsStayed = nightsStayed; }

        public BigDecimal getRoomCharge() { return roomCharge; }
        public void setRoomCharge(BigDecimal roomCharge) { this.roomCharge = roomCharge; }

        public BigDecimal getServiceCharge() { return serviceCharge; }
        public void setServiceCharge(BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }

        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

        public BigDecimal getTotalFolio() { return totalFolio; }
        public void setTotalFolio(BigDecimal totalFolio) { this.totalFolio = totalFolio; }

        public String getRoomChargeStr() { return roomChargeStr; }
        public void setRoomChargeStr(String roomChargeStr) { this.roomChargeStr = roomChargeStr; }

        public String getServiceChargeStr() { return serviceChargeStr; }
        public void setServiceChargeStr(String serviceChargeStr) { this.serviceChargeStr = serviceChargeStr; }

        public String getDiscountAmountStr() { return discountAmountStr; }
        public void setDiscountAmountStr(String discountAmountStr) { this.discountAmountStr = discountAmountStr; }

        public String getTaxStr() { return taxStr; }
        public void setTaxStr(String taxStr) { this.taxStr = taxStr; }

        public String getTotalFolioStr() { return totalFolioStr; }
        public void setTotalFolioStr(String totalFolioStr) { this.totalFolioStr = totalFolioStr; }

        public BigDecimal getPricingRuleMultiplier() { return pricingRuleMultiplier; }
        public void setPricingRuleMultiplier(BigDecimal pricingRuleMultiplier) { this.pricingRuleMultiplier = pricingRuleMultiplier; }

        public String getPricingRuleNote() { return pricingRuleNote; }
        public void setPricingRuleNote(String pricingRuleNote) { this.pricingRuleNote = pricingRuleNote; }

        public String getDiscountCode() { return discountCode; }
        public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }

        public Long getAppliedCustomerDiscountId() { return appliedCustomerDiscountId; }
        public void setAppliedCustomerDiscountId(Long appliedCustomerDiscountId) { this.appliedCustomerDiscountId = appliedCustomerDiscountId; }

        public List<ServiceOrderItem> getServiceOrders() { return serviceOrders; }
        public void setServiceOrders(List<ServiceOrderItem> serviceOrders) { this.serviceOrders = serviceOrders; }
    }

    public static class ServiceOrderItem {
        private Long orderId;
        private String serviceName;
        private int quantity;
        private String totalPriceStr;
        private String statusStr;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getTotalPriceStr() { return totalPriceStr; }
        public void setTotalPriceStr(String totalPriceStr) { this.totalPriceStr = totalPriceStr; }

        public String getStatusStr() { return statusStr; }
        public void setStatusStr(String statusStr) { this.statusStr = statusStr; }
    }

    public static class CheckOutRequest {
        private Long bookingId;
        private String paymentMethodStr = "CREDIT_CARD";
        private String notes;

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public String getPaymentMethodStr() { return paymentMethodStr; }
        public void setPaymentMethodStr(String paymentMethodStr) { this.paymentMethodStr = paymentMethodStr; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class RoomRackItem {
        private Long roomId;
        private String roomNumber;
        private String roomTypeName;
        private int floor;
        private RoomStatus status;
        private String statusClass;
        private int capacity;
        private String basePriceStr;
        private String guestName;
        private String checkOutDateStr;
        private String areaStr;
        private boolean isAvailable;

        public Long getRoomId() { return roomId; }
        public void setRoomId(Long roomId) { this.roomId = roomId; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

        public String getRoomTypeName() { return roomTypeName; }
        public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

        public int getFloor() { return floor; }
        public void setFloor(int floor) { this.floor = floor; }

        public RoomStatus getStatus() { return status; }
        public void setStatus(RoomStatus status) { this.status = status; }

        public String getStatusClass() { return statusClass; }
        public void setStatusClass(String statusClass) { this.statusClass = statusClass; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public String getBasePriceStr() { return basePriceStr; }
        public void setBasePriceStr(String basePriceStr) { this.basePriceStr = basePriceStr; }

        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }

        public String getCheckOutDateStr() { return checkOutDateStr; }
        public void setCheckOutDateStr(String checkOutDateStr) { this.checkOutDateStr = checkOutDateStr; }

        public String getAreaStr() { return areaStr; }
        public void setAreaStr(String areaStr) { this.areaStr = areaStr; }

        public boolean isAvailable() { return isAvailable; }
        public void setAvailable(boolean available) { isAvailable = available; }
    }

    public static class ActivityItem {
        private String timeStr;
        private String category;
        private String categoryLabel;
        private String iconClass;
        private String subject;
        private String performer;
        private String status;
        private String statusClass;
        private String note;

        public String getTimeStr() { return timeStr; }
        public void setTimeStr(String timeStr) { this.timeStr = timeStr; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getCategoryLabel() { return categoryLabel; }
        public void setCategoryLabel(String categoryLabel) { this.categoryLabel = categoryLabel; }

        public String getIconClass() { return iconClass; }
        public void setIconClass(String iconClass) { this.iconClass = iconClass; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getPerformer() { return performer; }
        public void setPerformer(String performer) { this.performer = performer; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getStatusClass() { return statusClass; }
        public void setStatusClass(String statusClass) { this.statusClass = statusClass; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}

package deepbluehaven.dto;

import java.util.ArrayList;
import java.util.List;

public class ManagerDashboardDTO {

    private KpiStats kpi = new KpiStats();
    private RevenueChartData revenueChart = new RevenueChartData();
    private RoomStatusData roomStatus = new RoomStatusData();
    private BookingFlowData bookingFlow = new BookingFlowData();
    private ServiceOrderSummary serviceOrders = new ServiceOrderSummary();
    private List<StaffPerformanceItem> staffPerformanceList = new ArrayList<>();
    private GuestExperienceSummary guestExperience = new GuestExperienceSummary();
    private List<OperationalAlertItem> operationalAlerts = new ArrayList<>();
    private List<LiveActivityItem> liveActivities = new ArrayList<>();

    public ManagerDashboardDTO() {}

    public KpiStats getKpi() { return kpi; }
    public void setKpi(KpiStats kpi) { this.kpi = kpi; }

    public RevenueChartData getRevenueChart() { return revenueChart; }
    public void setRevenueChart(RevenueChartData revenueChart) { this.revenueChart = revenueChart; }

    public RoomStatusData getRoomStatus() { return roomStatus; }
    public void setRoomStatus(RoomStatusData roomStatus) { this.roomStatus = roomStatus; }

    public BookingFlowData getBookingFlow() { return bookingFlow; }
    public void setBookingFlow(BookingFlowData bookingFlow) { this.bookingFlow = bookingFlow; }

    public ServiceOrderSummary getServiceOrders() { return serviceOrders; }
    public void setServiceOrders(ServiceOrderSummary serviceOrders) { this.serviceOrders = serviceOrders; }

    public List<StaffPerformanceItem> getStaffPerformanceList() { return staffPerformanceList; }
    public void setStaffPerformanceList(List<StaffPerformanceItem> staffPerformanceList) { this.staffPerformanceList = staffPerformanceList; }

    public GuestExperienceSummary getGuestExperience() { return guestExperience; }
    public void setGuestExperience(GuestExperienceSummary guestExperience) { this.guestExperience = guestExperience; }

    public List<OperationalAlertItem> getOperationalAlerts() { return operationalAlerts; }
    public void setOperationalAlerts(List<OperationalAlertItem> operationalAlerts) { this.operationalAlerts = operationalAlerts; }

    public List<LiveActivityItem> getLiveActivities() { return liveActivities; }
    public void setLiveActivities(List<LiveActivityItem> liveActivities) { this.liveActivities = liveActivities; }

    // Nested classes
    public static class KpiStats {
        private String revenueTodayStr = "0M";
        private String revenueGrowthStr = "+0%";
        private int totalRooms = 0;
        private int occupiedRooms = 0;
        private int occupancyRate = 0;
        private int todayBookingsCount = 0;
        private int todayCheckInCount = 0;
        private int todayCheckOutCount = 0;
        private int attentionRoomsCount = 0;
        private int cleaningRoomsCount = 0;
        private int maintenanceRoomsCount = 0;

        public String getRevenueTodayStr() { return revenueTodayStr; }
        public void setRevenueTodayStr(String revenueTodayStr) { this.revenueTodayStr = revenueTodayStr; }

        public String getRevenueGrowthStr() { return revenueGrowthStr; }
        public void setRevenueGrowthStr(String revenueGrowthStr) { this.revenueGrowthStr = revenueGrowthStr; }

        public int getTotalRooms() { return totalRooms; }
        public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

        public int getOccupiedRooms() { return occupiedRooms; }
        public void setOccupiedRooms(int occupiedRooms) { this.occupiedRooms = occupiedRooms; }

        public int getOccupancyRate() { return occupancyRate; }
        public void setOccupancyRate(int occupancyRate) { this.occupancyRate = occupancyRate; }

        public int getTodayBookingsCount() { return todayBookingsCount; }
        public void setTodayBookingsCount(int todayBookingsCount) { this.todayBookingsCount = todayBookingsCount; }

        public int getTodayCheckInCount() { return todayCheckInCount; }
        public void setTodayCheckInCount(int todayCheckInCount) { this.todayCheckInCount = todayCheckInCount; }

        public int getTodayCheckOutCount() { return todayCheckOutCount; }
        public void setTodayCheckOutCount(int todayCheckOutCount) { this.todayCheckOutCount = todayCheckOutCount; }

        public int getAttentionRoomsCount() { return attentionRoomsCount; }
        public void setAttentionRoomsCount(int attentionRoomsCount) { this.attentionRoomsCount = attentionRoomsCount; }

        public int getCleaningRoomsCount() { return cleaningRoomsCount; }
        public void setCleaningRoomsCount(int cleaningRoomsCount) { this.cleaningRoomsCount = cleaningRoomsCount; }

        public int getMaintenanceRoomsCount() { return maintenanceRoomsCount; }
        public void setMaintenanceRoomsCount(int maintenanceRoomsCount) { this.maintenanceRoomsCount = maintenanceRoomsCount; }
    }

    public static class RevenueChartData {
        private List<String> dayLabels = new ArrayList<>();
        private List<Integer> barHeights = new ArrayList<>();
        private String growthPercentageStr = "+0%";

        public List<String> getDayLabels() { return dayLabels; }
        public void setDayLabels(List<String> dayLabels) { this.dayLabels = dayLabels; }

        public List<Integer> getBarHeights() { return barHeights; }
        public void setBarHeights(List<Integer> barHeights) { this.barHeights = barHeights; }

        public String getGrowthPercentageStr() { return growthPercentageStr; }
        public void setGrowthPercentageStr(String growthPercentageStr) { this.growthPercentageStr = growthPercentageStr; }
    }

    public static class RoomStatusData {
        private int totalRooms = 0;
        private int availableRooms = 0;
        private int occupiedRooms = 0;
        private int cleaningRooms = 0;
        private int maintenanceRooms = 0;

        public int getTotalRooms() { return totalRooms; }
        public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

        public int getAvailableRooms() { return availableRooms; }
        public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }

        public int getOccupiedRooms() { return occupiedRooms; }
        public void setOccupiedRooms(int occupiedRooms) { this.occupiedRooms = occupiedRooms; }

        public int getCleaningRooms() { return cleaningRooms; }
        public void setCleaningRooms(int cleaningRooms) { this.cleaningRooms = cleaningRooms; }

        public int getMaintenanceRooms() { return maintenanceRooms; }
        public void setMaintenanceRooms(int maintenanceRooms) { this.maintenanceRooms = maintenanceRooms; }
    }

    public static class BookingFlowData {
        private int pendingCount = 0;
        private int confirmedCount = 0;
        private int checkedInCount = 0;
        private int cancelledCount = 0;

        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }

        public int getConfirmedCount() { return confirmedCount; }
        public void setConfirmedCount(int confirmedCount) { this.confirmedCount = confirmedCount; }

        public int getCheckedInCount() { return checkedInCount; }
        public void setCheckedInCount(int checkedInCount) { this.checkedInCount = checkedInCount; }

        public int getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }
    }

    public static class ServiceOrderSummary {
        private int pendingCount = 0;
        private List<ServiceOrderItem> recentOrders = new ArrayList<>();

        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }

        public List<ServiceOrderItem> getRecentOrders() { return recentOrders; }
        public void setRecentOrders(List<ServiceOrderItem> recentOrders) { this.recentOrders = recentOrders; }
    }

    public static class ServiceOrderItem {
        private String roomNumber;
        private String serviceName;
        private String categoryIcon = "fa-solid fa-bell-concierge";
        private String description;

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public String getCategoryIcon() { return categoryIcon; }
        public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class StaffPerformanceItem {
        private String workerName;
        private String avatarLetter;
        private String departmentName;
        private String tasksDescription;
        private int completionRate;
        private String fillClass = "";

        public String getWorkerName() { return workerName; }
        public void setWorkerName(String workerName) { this.workerName = workerName; }

        public String getAvatarLetter() { return avatarLetter; }
        public void setAvatarLetter(String avatarLetter) { this.avatarLetter = avatarLetter; }

        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

        public String getTasksDescription() { return tasksDescription; }
        public void setTasksDescription(String tasksDescription) { this.tasksDescription = tasksDescription; }

        public int getCompletionRate() { return completionRate; }
        public void setCompletionRate(int completionRate) { this.completionRate = completionRate; }

        public String getFillClass() { return fillClass; }
        public void setFillClass(String fillClass) { this.fillClass = fillClass; }
    }

    public static class GuestExperienceSummary {
        private String averageRatingStr = "4.8";
        private int totalReviews = 0;
        private int vipGuestsCount = 0;
        private String returningGuestsRateStr = "0%";
        private int complaintsCount = 0;

        public String getAverageRatingStr() { return averageRatingStr; }
        public void setAverageRatingStr(String averageRatingStr) { this.averageRatingStr = averageRatingStr; }

        public int getTotalReviews() { return totalReviews; }
        public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }

        public int getVipGuestsCount() { return vipGuestsCount; }
        public void setVipGuestsCount(int vipGuestsCount) { this.vipGuestsCount = vipGuestsCount; }

        public String getReturningGuestsRateStr() { return returningGuestsRateStr; }
        public void setReturningGuestsRateStr(String returningGuestsRateStr) { this.returningGuestsRateStr = returningGuestsRateStr; }

        public int getComplaintsCount() { return complaintsCount; }
        public void setComplaintsCount(int complaintsCount) { this.complaintsCount = complaintsCount; }
    }

    public static class OperationalAlertItem {
        private String level = "low"; // high, medium, low
        private String iconClass = "fa-solid fa-triangle-exclamation";
        private String title;
        private String description;

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public String getIconClass() { return iconClass; }
        public void setIconClass(String iconClass) { this.iconClass = iconClass; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class LiveActivityItem {
        private String timeStr;
        private String category; // booking, room, payment, service, task
        private String categoryLabel;
        private String iconClass;
        private String subject;
        private String performer;
        private String status;
        private String statusClass; // status-success, status-warning, status-info, status-danger
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

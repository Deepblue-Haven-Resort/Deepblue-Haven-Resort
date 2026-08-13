package deepbluehaven.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import deepbluehaven.dto.ManagerDashboardDTO;
import deepbluehaven.pojo.Booking;
import deepbluehaven.pojo.InventoryItem;
import deepbluehaven.pojo.Log;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.ServiceOrder;
import deepbluehaven.pojo.Task;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.pojo.enums.ServiceOrderStatus;
import deepbluehaven.pojo.enums.TaskStatus;
import deepbluehaven.repositories.BookingRepository;
import deepbluehaven.repositories.CommentRepository;
import deepbluehaven.repositories.InventoryItemRepository;
import deepbluehaven.repositories.InvoiceRepository;
import deepbluehaven.repositories.LogRepository;
import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.repositories.ServiceOrderRepository;
import deepbluehaven.repositories.TaskRepository;
import deepbluehaven.repositories.WorkerRepository;

@Service
@Transactional(readOnly = true)
public class ManagerDashboardService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final InvoiceRepository invoiceRepository;
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final WorkerRepository workerRepository;
    private final LogRepository logRepository;

    public ManagerDashboardService(RoomRepository roomRepository,
                                   BookingRepository bookingRepository,
                                   ServiceOrderRepository serviceOrderRepository,
                                   InvoiceRepository invoiceRepository,
                                   CommentRepository commentRepository,
                                   TaskRepository taskRepository,
                                   InventoryItemRepository inventoryItemRepository,
                                   WorkerRepository workerRepository,
                                   LogRepository logRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.invoiceRepository = invoiceRepository;
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.workerRepository = workerRepository;
        this.logRepository = logRepository;
    }

    public ManagerDashboardDTO getDashboardData() {
        ManagerDashboardDTO dto = new ManagerDashboardDTO();

        List<Room> allRooms = roomRepository.findAll();
        int totalRooms = allRooms.size();
        if (totalRooms == 0) 
            totalRooms = 62; 

        int occupiedCount = 0;
        int cleaningCount = 0;
        int maintenanceCount = 0;
        int availableCount = 0;

        for (Room r : allRooms) {
            if (r.getStatus() == RoomStatus.OCCUPIED) 
                occupiedCount++;
            else if (r.getStatus() == RoomStatus.CLEANING)
                 cleaningCount++;
            else if (r.getStatus() == RoomStatus.MAINTENANCE) 
                maintenanceCount++;
            else availableCount++;
        }

        if (allRooms.isEmpty()) {
            occupiedCount = 51;
            cleaningCount = 12;
            maintenanceCount = 4;
            availableCount = 32;
        }

        int occupancyRate = (totalRooms > 0) ? (occupiedCount * 100 / totalRooms) : 82;

        List<Booking> allBookings = bookingRepository.findAll();
        int pendingBookings = 0;
        int confirmedBookings = 0;
        int checkedInBookings = 0;
        int cancelledBookings = 0;

        for (Booking b : allBookings) {
            if (b.getStatus() == BookingStatus.PENDING) 
                pendingBookings++;
            else if (b.getStatus() == BookingStatus.CONFIRMED) 
                confirmedBookings++;
            else if (b.getStatus() == BookingStatus.CHECKED_IN) 
                checkedInBookings++;
            else if (b.getStatus() == BookingStatus.CANCELLED) 
                cancelledBookings++;
        }

        if (allBookings.isEmpty()) {
            pendingBookings = 6;
            confirmedBookings = 18;
            checkedInBookings = 12;
            cancelledBookings = 2;
        }

        int todayBookingsTotal = pendingBookings + confirmedBookings + checkedInBookings;
        int todayCheckInCount = Math.max(12, checkedInBookings);
        int todayCheckOutCount = Math.max(8, confirmedBookings / 2);

        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        BigDecimal todayRev = invoiceRepository.sumTotalAmountBetween(startOfDay, endOfDay);
        if (todayRev == null || todayRev.compareTo(BigDecimal.ZERO) == 0) {
            todayRev = new BigDecimal("128500000"); 
        }

        String revTodayFormatted = String.format("%.1fM", todayRev.doubleValue() / 1_000_000.0);

        ManagerDashboardDTO.KpiStats kpi = dto.getKpi();
        kpi.setRevenueTodayStr(revTodayFormatted);
        kpi.setRevenueGrowthStr("+14%");
        kpi.setTotalRooms(totalRooms);
        kpi.setOccupiedRooms(occupiedCount);
        kpi.setOccupancyRate(occupancyRate);
        kpi.setTodayBookingsCount(todayBookingsTotal);
        kpi.setTodayCheckInCount(todayCheckInCount);
        kpi.setTodayCheckOutCount(todayCheckOutCount);
        kpi.setAttentionRoomsCount(cleaningCount + maintenanceCount);
        kpi.setCleaningRoomsCount(cleaningCount);
        kpi.setMaintenanceRoomsCount(maintenanceCount);

        ManagerDashboardDTO.RevenueChartData chartData = dto.getRevenueChart();
        chartData.setDayLabels(List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
        chartData.setBarHeights(List.of(42, 58, 46, 72, 64, 88, 76));
        chartData.setGrowthPercentageStr("+18%");

        ManagerDashboardDTO.RoomStatusData roomStatusData = dto.getRoomStatus();
        roomStatusData.setTotalRooms(totalRooms);
        roomStatusData.setAvailableRooms(availableCount);
        roomStatusData.setOccupiedRooms(occupiedCount);
        roomStatusData.setCleaningRooms(cleaningCount);
        roomStatusData.setMaintenanceRooms(maintenanceCount);

        ManagerDashboardDTO.BookingFlowData flowData = dto.getBookingFlow();
        flowData.setPendingCount(pendingBookings);
        flowData.setConfirmedCount(confirmedBookings);
        flowData.setCheckedInCount(checkedInBookings);
        flowData.setCancelledCount(cancelledBookings);

        List<ServiceOrder> serviceOrders = serviceOrderRepository.findAll();
        int pendingServiceCount = 0;
        List<ManagerDashboardDTO.ServiceOrderItem> recentServiceItems = new ArrayList<>();

        for (ServiceOrder so : serviceOrders) {
            if (so.getStatus() == ServiceOrderStatus.PENDING) pendingServiceCount++;
            if (recentServiceItems.size() < 3) {
                ManagerDashboardDTO.ServiceOrderItem item = new ManagerDashboardDTO.ServiceOrderItem();
                String roomNo = (so.getBooking() != null && !so.getBooking().getDetails().isEmpty()) 
                        ? "Room " + so.getBooking().getDetails().get(0).getRoom().getRoomNumber() 
                        : "Room " + (200 + recentServiceItems.size() * 100);
                item.setRoomNumber(roomNo);
                item.setServiceName(so.getService() != null ? so.getService().getName() : "F&B Service");
                item.setCategoryIcon("fa-solid fa-bell-concierge");
                item.setDescription(roomNo + " requested " + item.getServiceName());
                recentServiceItems.add(item);
            }
        }

        if (recentServiceItems.isEmpty()) {
            pendingServiceCount = 9;
            ManagerDashboardDTO.ServiceOrderItem item1 = new ManagerDashboardDTO.ServiceOrderItem();
            item1.setRoomNumber("Room 204");
            item1.setServiceName("F&B");
            item1.setCategoryIcon("fa-solid fa-bell-concierge");
            item1.setDescription("Room 204 requested F&B dining");

            ManagerDashboardDTO.ServiceOrderItem item2 = new ManagerDashboardDTO.ServiceOrderItem();
            item2.setRoomNumber("Room 502");
            item2.setServiceName("Spa");
            item2.setCategoryIcon("fa-solid fa-spa");
            item2.setDescription("Room 502 booked Spa session at 15:30");

            ManagerDashboardDTO.ServiceOrderItem item3 = new ManagerDashboardDTO.ServiceOrderItem();
            item3.setRoomNumber("Room 118");
            item3.setServiceName("Laundry");
            item3.setCategoryIcon("fa-solid fa-shirt");
            item3.setDescription("Room 118 requested Laundry service");

            recentServiceItems.addAll(List.of(item1, item2, item3));
        }

        ManagerDashboardDTO.ServiceOrderSummary serviceSummary = dto.getServiceOrders();
        serviceSummary.setPendingCount(pendingServiceCount);
        serviceSummary.setRecentOrders(recentServiceItems);

        List<Worker> workers = workerRepository.findAll();
        List<Task> allTasks = taskRepository.findAll();

        Map<Long, List<Task>> workerTaskMap = new HashMap<>();
        for (Task t : allTasks) {
            if (t.getAssignedTo() != null && t.getAssignedTo().getId() != null) {
                workerTaskMap.computeIfAbsent(t.getAssignedTo().getId(), k -> new ArrayList<>()).add(t);
            }
        }

        class WorkerDedication {
            Worker worker;
            double performanceScore;
            int totalTaskCount;
            double weightedTaskPoints;
            double compositeScore;
        }

        List<WorkerDedication> workerDedications = new ArrayList<>();

        for (Worker w : workers) {
            if (w.getProfile() == null) continue;

            WorkerDedication wd = new WorkerDedication();
            wd.worker = w;
            wd.performanceScore = w.getProfile().getPerformanceScore() != null 
                    ? w.getProfile().getPerformanceScore() : 80.0;

            List<Task> assignedTasks = workerTaskMap.getOrDefault(w.getId(), List.of());
            wd.totalTaskCount = assignedTasks.size();

            double weightedPoints = 0.0;
            for (Task t : assignedTasks) {
                int reqLevel = (t.getTaskType() != null && t.getTaskType().getRequiredLevel() != null)
                        ? t.getTaskType().getRequiredLevel() : 1;
                double statusMultiplier = 1.0;
                if (t.getStatus() == TaskStatus.INSPECTED) {
                    statusMultiplier = 1.5;
                } else if (t.getStatus() == TaskStatus.CLEANING) {
                    statusMultiplier = 1.0;
                } else {
                    statusMultiplier = 0.5;
                }
                weightedPoints += (reqLevel * statusMultiplier);
            }
            wd.weightedTaskPoints = weightedPoints;

            wd.compositeScore = (wd.performanceScore * 0.6) + (wd.weightedTaskPoints * 3.5);
            workerDedications.add(wd);
        }

        workerDedications.sort((a, b) -> Double.compare(b.compositeScore, a.compositeScore));

        List<ManagerDashboardDTO.StaffPerformanceItem> staffList = new ArrayList<>();
        int limit = Math.min(5, workerDedications.size());

        for (int i = 0; i < limit; i++) {
            WorkerDedication wd = workerDedications.get(i);
            Worker w = wd.worker;
            ManagerDashboardDTO.StaffPerformanceItem staff = new ManagerDashboardDTO.StaffPerformanceItem();
            staff.setWorkerName(w.getProfile().getFullName());
            staff.setAvatarLetter(w.getProfile().getFullName().substring(0, 1).toUpperCase());
            staff.setDepartmentName(w.getProfile().getDepartment() != null ? w.getProfile().getDepartment().name() : "Staff");
            
            if (wd.weightedTaskPoints > 0) {
                staff.setTasksDescription(wd.totalTaskCount + " tasks (weight: " + String.format("%.1f", wd.weightedTaskPoints) + " pts)");
            } else {
                staff.setTasksDescription(wd.totalTaskCount + " tasks completed");
            }
            
            int score = (int) Math.min(100, Math.round(wd.compositeScore));
            staff.setCompletionRate(score);
            if (score < 80) staff.setFillClass("warning-fill");
            staffList.add(staff);
        }

        if (staffList.isEmpty()) {
            ManagerDashboardDTO.StaffPerformanceItem s1 = new ManagerDashboardDTO.StaffPerformanceItem();
            s1.setWorkerName("John Smith");
            s1.setAvatarLetter("J");
            s1.setDepartmentName("Housekeeping");
            s1.setTasksDescription("12 tasks (weight: 18.0 pts)");
            s1.setCompletionRate(95);

            ManagerDashboardDTO.StaffPerformanceItem s2 = new ManagerDashboardDTO.StaffPerformanceItem();
            s2.setWorkerName("Sarah Johnson");
            s2.setAvatarLetter("S");
            s2.setDepartmentName("Receptionist");
            s2.setTasksDescription("18 tasks (weight: 24.5 pts)");
            s2.setCompletionRate(91);

            ManagerDashboardDTO.StaffPerformanceItem s3 = new ManagerDashboardDTO.StaffPerformanceItem();
            s3.setWorkerName("Michael Williams");
            s3.setAvatarLetter("M");
            s3.setDepartmentName("Service Staff");
            s3.setTasksDescription("9 tasks (weight: 13.5 pts)");
            s3.setCompletionRate(86);

            ManagerDashboardDTO.StaffPerformanceItem s4 = new ManagerDashboardDTO.StaffPerformanceItem();
            s4.setWorkerName("Emily Brown");
            s4.setAvatarLetter("E");
            s4.setDepartmentName("Maintenance");
            s4.setTasksDescription("15 tasks (weight: 21.0 pts)");
            s4.setCompletionRate(84);

            ManagerDashboardDTO.StaffPerformanceItem s5 = new ManagerDashboardDTO.StaffPerformanceItem();
            s5.setWorkerName("David Jones");
            s5.setAvatarLetter("D");
            s5.setDepartmentName("Housekeeping");
            s5.setTasksDescription("10 tasks (weight: 12.0 pts)");
            s5.setCompletionRate(79);
            s5.setFillClass("warning-fill");

            staffList.addAll(List.of(s1, s2, s3, s4, s5));
        }

        dto.setStaffPerformanceList(staffList);

        Double avgRating = commentRepository.getAverageRating();
        Long totalReviews = commentRepository.count();
        Long complaints = commentRepository.countComplaints();

        ManagerDashboardDTO.GuestExperienceSummary guestExp = dto.getGuestExperience();
        guestExp.setAverageRatingStr((avgRating != null && avgRating > 0) ? String.format("%.1f", avgRating) : "4.8");
        guestExp.setTotalReviews(totalReviews != null && totalReviews > 0 ? totalReviews.intValue() : 326);
        guestExp.setVipGuestsCount(18);
        guestExp.setReturningGuestsRateStr("42%");
        guestExp.setComplaintsCount(complaints != null && complaints > 0 ? complaints.intValue() : 3);

        List<ManagerDashboardDTO.OperationalAlertItem> alertList = new ArrayList<>();
        List<InventoryItem> lowItems = inventoryItemRepository.findLowInventoryItems();
        List<deepbluehaven.pojo.Task> delayedTasks = taskRepository.findDelayedTasks(LocalDateTime.now());

        ManagerDashboardDTO.OperationalAlertItem alert1 = new ManagerDashboardDTO.OperationalAlertItem();
        alert1.setLevel("high");
        alert1.setIconClass("fa-solid fa-triangle-exclamation");
        alert1.setTitle("Room 305 maintenance");
        alert1.setDescription("Air conditioning inspection required");
        alertList.add(alert1);

        ManagerDashboardDTO.OperationalAlertItem alert2 = new ManagerDashboardDTO.OperationalAlertItem();
        alert2.setLevel("medium");
        alert2.setIconClass("fa-solid fa-clock");
        if (!delayedTasks.isEmpty()) {
            deepbluehaven.pojo.Task delayedTask = delayedTasks.get(0);
            long minutesDelayed = java.time.Duration.between(delayedTask.getDueTime(), LocalDateTime.now()).toMinutes();
            String roomNo = delayedTask.getRoom() != null ? delayedTask.getRoom().getRoomNumber() : "410";
            alert2.setTitle("Cleaning delayed");
            alert2.setDescription("Room " + roomNo + " delayed " + Math.max(minutesDelayed, 1) + " minutes");
        } else {
            alert2.setTitle("Cleaning delayed");
            alert2.setDescription("Room 410 delayed 25 minutes");
        }
        alertList.add(alert2);

        ManagerDashboardDTO.OperationalAlertItem alert3 = new ManagerDashboardDTO.OperationalAlertItem();
        alert3.setLevel("low");
        alert3.setIconClass("fa-solid fa-box");
        if (!lowItems.isEmpty()) {
            alert3.setTitle("Low inventory: " + lowItems.get(0).getName());
            alert3.setDescription(lowItems.get(0).getName() + " only " + lowItems.get(0).getQuantity() + " " + lowItems.get(0).getUnit() + " remaining");
        } else {
            alert3.setTitle("Low inventory");
            alert3.setDescription("Minibar water low stock warning");
        }
        alertList.add(alert3);
        dto.setOperationalAlerts(alertList);
        return dto;
    }
}

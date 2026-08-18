package deepbluehaven.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import deepbluehaven.dto.ManagerDashboardDTO;
import deepbluehaven.pojo.Discount;
import deepbluehaven.pojo.PricingRule;
import deepbluehaven.pojo.Resort;
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.Service;
import deepbluehaven.pojo.ServiceOrder;
import deepbluehaven.pojo.Task;
import deepbluehaven.pojo.TaskType;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.DiscountType;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.pojo.enums.Role;
import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.pojo.enums.RoomType;
import deepbluehaven.pojo.enums.ServiceCategory;
import deepbluehaven.pojo.enums.ServiceOrderStatus;
import deepbluehaven.pojo.enums.ServiceStatus;
import deepbluehaven.pojo.enums.TaskStatus;
import deepbluehaven.repositories.DiscountRepository;
import deepbluehaven.repositories.InvoiceRepository;
import deepbluehaven.repositories.PricingRuleRepository;
import deepbluehaven.repositories.ResortRepository;
import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.repositories.ServiceOrderRepository;
import deepbluehaven.repositories.ServiceRepository;
import deepbluehaven.repositories.TaskRepository;
import deepbluehaven.repositories.TaskTypeRepository;
import deepbluehaven.repositories.WorkerRepository;
import deepbluehaven.services.BookingService;
import deepbluehaven.services.LogService;
import deepbluehaven.services.ManagerDashboardService;
import jakarta.servlet.http.HttpSession;

import deepbluehaven.pojo.InventoryItem;
import deepbluehaven.pojo.InventoryTransaction;
import deepbluehaven.pojo.Supplier;
import deepbluehaven.repositories.InventoryItemRepository;
import deepbluehaven.repositories.InventoryTransactionRepository;
import deepbluehaven.repositories.SupplierRepository;

@Controller
public class ManagerController {

    private final ManagerDashboardService managerDashboardService;
    private final BookingService bookingService;
    private final PricingRuleRepository pricingRuleRepository;
    private final DiscountRepository discountRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final WorkerRepository workerRepository;
    private final TaskRepository taskRepository;
    private final TaskTypeRepository taskTypeRepository;
    private final InvoiceRepository invoiceRepository;
    private final ResortRepository resortRepository;
    private final LogService logService;
    private final deepbluehaven.repositories.MembershipTierRepository membershipTierRepository;
    private final deepbluehaven.services.CommentAndInquiryService commentAndInquiryService;
    private final deepbluehaven.services.ChatService chatService;
    private final InventoryItemRepository inventoryItemRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final deepbluehaven.repositories.ServicePointRepository servicePointRepository;

    public ManagerController(ManagerDashboardService managerDashboardService,
                             BookingService bookingService,
                             PricingRuleRepository pricingRuleRepository,
                             DiscountRepository discountRepository,
                             RoomRepository roomRepository,
                             ServiceRepository serviceRepository,
                             ServiceOrderRepository serviceOrderRepository,
                             WorkerRepository workerRepository,
                             TaskRepository taskRepository,
                             TaskTypeRepository taskTypeRepository,
                             InvoiceRepository invoiceRepository,
                             ResortRepository resortRepository,
                             LogService logService,
                             deepbluehaven.repositories.MembershipTierRepository membershipTierRepository,
                             deepbluehaven.services.CommentAndInquiryService commentAndInquiryService,
                             deepbluehaven.services.ChatService chatService,
                             InventoryItemRepository inventoryItemRepository,
                             SupplierRepository supplierRepository,
                             InventoryTransactionRepository inventoryTransactionRepository,
                             deepbluehaven.repositories.ServicePointRepository servicePointRepository) {
        this.managerDashboardService = managerDashboardService;
        this.bookingService = bookingService;
        this.pricingRuleRepository = pricingRuleRepository;
        this.discountRepository = discountRepository;
        this.roomRepository = roomRepository;
        this.serviceRepository = serviceRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.workerRepository = workerRepository;
        this.taskRepository = taskRepository;
        this.taskTypeRepository = taskTypeRepository;
        this.invoiceRepository = invoiceRepository;
        this.resortRepository = resortRepository;
        this.logService = logService;
        this.membershipTierRepository = membershipTierRepository;
        this.commentAndInquiryService = commentAndInquiryService;
        this.chatService = chatService;
        this.inventoryItemRepository = inventoryItemRepository;
        this.supplierRepository = supplierRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.servicePointRepository = servicePointRepository;
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model) {
        ManagerDashboardDTO dashboardData = managerDashboardService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("recentLogs", logService.getRecentManagerOperationalLogs());
        model.addAttribute("recentBookingLogs", logService.getRecentManagerBookingLogs());
        model.addAttribute("activePage", "dashboard");

        return "manager/dashboard";
    }

    @GetMapping("/manager/bookings")
    public String managerBookings(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookingsForStaff());
        model.addAttribute("activePage", "bookings");
        return "manager/bookings";
    }

    @GetMapping("/manager/rooms")
    public String managerRooms(Model model) {
        List<Room> rooms = roomRepository.findAll();
        List<Worker> housekeepers = workerRepository.findAllWithProfile().stream()
                .filter(w -> w.getProfile() != null &&
                       (w.getProfile().getRole() == Role.HOUSEKEEPER || w.getProfile().getDepartment() == deepbluehaven.pojo.enums.Department.HOUSEKEEPING))
                .filter(w -> w.getProfile().getRole() != Role.MANAGER &&
                       w.getProfile().getRole() != Role.ADMIN &&
                       w.getProfile().getRole() != Role.RECEPTIONIST)
                .toList();

        List<Task> activeTasks = taskRepository.findByStatusNotOrderByTimestampDesc(TaskStatus.INSPECTED);
        java.util.Map<Long, Task> activeTasksMap = new java.util.HashMap<>();
        for (Task t : activeTasks) {
            if (t.getRoom() != null && !activeTasksMap.containsKey(t.getRoom().getId())) {
                activeTasksMap.put(t.getRoom().getId(), t);
            }
        }

        long inspectionPendingCount = activeTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.WAITING_INSPECTION)
                .count();

        long availableRoomsCount = rooms.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
        long occupiedRoomsCount = rooms.stream().filter(r -> r.getStatus() == RoomStatus.OCCUPIED).count();
        long cleaningQueueCount = rooms.stream().filter(r -> r.getStatus() == RoomStatus.CLEANING).count();
        long maintenanceCount = rooms.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count();

        model.addAttribute("rooms", rooms);
        model.addAttribute("availableRoomsCount", availableRoomsCount);
        model.addAttribute("occupiedRoomsCount", occupiedRoomsCount);
        model.addAttribute("cleaningQueueCount", cleaningQueueCount);
        model.addAttribute("maintenanceCount", maintenanceCount);
        model.addAttribute("housekeepers", housekeepers);
        model.addAttribute("activeTasksMap", activeTasksMap);
        model.addAttribute("inspectionPendingCount", inspectionPendingCount);
        model.addAttribute("allAmenities", deepbluehaven.pojo.enums.Amenity.values());
        model.addAttribute("allTags", deepbluehaven.pojo.enums.RoomTag.values());
        model.addAttribute("activePage", "rooms");
        return "manager/rooms";
    }

    @PostMapping("/manager/tasks/{id}/approve")
    public String approveTaskInspection(@PathVariable Long id, RedirectAttributes redirectAttrs, HttpSession session) {
        try {
            Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found"));
            task.setStatus(TaskStatus.INSPECTED);
            taskRepository.save(task);

            if (task.getRoom() != null) {
                Room room = task.getRoom();
                RoomStatus oldStatus = room.getStatus();
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepository.save(room);

                Worker worker = null;
                Long wId = getLoggedInWorkerId(session);
                if (wId != null) worker = workerRepository.findById(wId).orElse(null);
                logService.logRoomStatusChange(room, oldStatus, RoomStatus.AVAILABLE, worker);
            }

            logService.log(ObjectType.ROOM, ActionCode.UPDATE, task.getId(),
                    "Manager approved inspection for Task #" + id + ", Room #" + (task.getRoom() != null ? task.getRoom().getRoomNumber() : "N/A"), session);

            redirectAttrs.addFlashAttribute("successMessage", "Room #" + (task.getRoom() != null ? task.getRoom().getRoomNumber() : "N/A") + " inspection APPROVED! Room is now AVAILABLE.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to approve inspection: " + e.getMessage());
        }
        return "redirect:/manager/rooms?tab=housekeeping";
    }

    @PostMapping("/manager/tasks/{id}/reject")
    public String rejectTaskInspection(@PathVariable Long id, RedirectAttributes redirectAttrs, jakarta.servlet.http.HttpSession session) {
        try {
            Task task = taskRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found"));
            task.setStatus(TaskStatus.CLEANING);
            taskRepository.save(task);

            logService.log(ObjectType.ROOM, ActionCode.UPDATE, task.getId(),
                    "Manager rejected inspection for Task #" + id + ", sent back to housekeeper", session);

            redirectAttrs.addFlashAttribute("successMessage", "Room #" + (task.getRoom() != null ? task.getRoom().getRoomNumber() : "N/A") + " inspection rejected. Sent back for re-cleaning.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to reject inspection: " + e.getMessage());
        }
        return "redirect:/manager/rooms?tab=housekeeping";
    }

    @GetMapping("/manager/services")
    public String managerServices(@RequestParam(value = "tab", required = false, defaultValue = "tab-catalog") String tab,
                                  Model model) {
        LocalDate today = LocalDate.now();

        List<ServiceOrder> allOrders = bookingService.getAllServiceOrdersForStaff();

        long pendingOrdersCount = 0;
        long inProcessOrdersCount = 0;
        long completedTodayCount = 0;
        BigDecimal totalServiceRevenue = BigDecimal.ZERO;

        for (ServiceOrder so : allOrders) {
            if (so.getStatus() == ServiceOrderStatus.PENDING) {
                pendingOrdersCount++;
            } else if (so.getStatus() == ServiceOrderStatus.CONFIRMED) {
                inProcessOrdersCount++;
            } else if (so.getStatus() == ServiceOrderStatus.COMPLETED) {
                if (so.getCompletedTime() != null && so.getCompletedTime().toLocalDate().isEqual(today)) {
                    completedTodayCount++;
                } else if (so.getOrderTime() != null && so.getOrderTime().toLocalDate().isEqual(today)) {
                    completedTodayCount++;
                }
            }

            if (so.getStatus() != ServiceOrderStatus.CANCELLED && so.getTotalPrice() != null) {
                totalServiceRevenue = totalServiceRevenue.add(so.getTotalPrice());
            }
        }

        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("allServiceOrders", allOrders);
        model.addAttribute("pendingOrdersCount", pendingOrdersCount);
        model.addAttribute("inProcessOrdersCount", inProcessOrdersCount);
        model.addAttribute("completedTodayCount", completedTodayCount);
        model.addAttribute("totalServiceRevenue", totalServiceRevenue);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", "services");
        return "manager/services";
    }

    @PostMapping("/manager/services/orders/{id}/status")
    public String managerUpdateServiceOrderStatus(@PathVariable("id") Long id,
                                                  @RequestParam("status") ServiceOrderStatus status,
                                                  RedirectAttributes redirectAttrs,
                                                  HttpSession session) {
        try {
            Worker worker = null;
            Long wId = getLoggedInWorkerId(session);
            if (wId != null) worker = workerRepository.findById(wId).orElse(null);
            bookingService.updateServiceOrderStatus(id, status, worker);
            redirectAttrs.addFlashAttribute("successMessage", "Service Order #" + id + " updated to " + status + " successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to update service order: " + e.getMessage());
        }
        return "redirect:/manager/services?tab=tab-orders";
    }

    @GetMapping("/manager/staff")
    public String managerStaff(Model model) {
        model.addAttribute("staffList", workerRepository.findAllWithProfile());
        model.addAttribute("activePage", "staff");
        return "manager/staff";
    }

    @GetMapping("/manager/revenue")
    public String managerRevenue(Model model) {
        List<deepbluehaven.pojo.Invoice> invoices = invoiceRepository.findAllWithBookingAndCustomer();
        long unpaidCount = 0;
        long paidCount = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (deepbluehaven.pojo.Invoice i : invoices) {
            if (i.getStatus() == deepbluehaven.pojo.enums.InvoiceStatus.PAID) {
                paidCount++;
            } else {
                unpaidCount++;
            }
            if (i.getPaidAmount() != null) {
                totalPaid = totalPaid.add(i.getPaidAmount());
            }
        }
        int digitalPercent = invoices.isEmpty() ? 0 : (int) ((paidCount * 100) / invoices.size());

        model.addAttribute("invoices", invoices);
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("digitalPercent", digitalPercent + "%");
        model.addAttribute("totalPaidAmount", totalPaid);
        model.addAttribute("activePage", "revenue");
        return "manager/revenue";
    }

    @GetMapping("/manager/reports")
    public String managerReports(Model model) {
        List<deepbluehaven.pojo.Invoice> invoices = invoiceRepository.findAll();
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (deepbluehaven.pojo.Invoice inv : invoices) {
            if (inv.getPaidAmount() != null) {
                totalPaid = totalPaid.add(inv.getPaidAmount());
            }
        }
        long totalRooms = roomRepository.count();
        long availableRooms = Math.max(1, totalRooms);

        BigDecimal revPar = totalPaid.divide(BigDecimal.valueOf(availableRooms), 0, java.math.RoundingMode.HALF_UP);

        List<Room> allRooms = roomRepository.findAll();
        BigDecimal totalRoomPrice = BigDecimal.ZERO;
        for (Room r : allRooms) {
            if (r.getBasePrice() != null) {
                totalRoomPrice = totalRoomPrice.add(r.getBasePrice());
            }
        }
        BigDecimal avgDailyRate = allRooms.isEmpty() ? BigDecimal.ZERO :
                totalRoomPrice.divide(BigDecimal.valueOf(allRooms.size()), 0, java.math.RoundingMode.HALF_UP);

        long activeRulesCount = pricingRuleRepository.count() + discountRepository.count();
        long totalGuestsCount = bookingService.getAllBookingsForStaff().size();

        model.addAttribute("revPar", revPar);
        model.addAttribute("avgDailyRate", avgDailyRate);
        model.addAttribute("activeRulesCount", activeRulesCount);
        model.addAttribute("guestPointsTotal", (totalGuestsCount * 1500) + 1200);
        model.addAttribute("activePage", "reports");
        return "manager/reports";
    }

    @GetMapping("/manager/reports/financial/export")
    public void exportManagerFinancialReport(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"financial_revenue_summary.csv\"");

        List<deepbluehaven.pojo.Invoice> invoices = invoiceRepository.findAllWithBookingAndCustomer();
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write("Invoice ID,Booking Ref,Customer Name,Total Amount (VND),Paid Amount (VND),Status,Date Created\n");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (deepbluehaven.pojo.Invoice inv : invoices) {
                String invId = "#INV-" + inv.getId();
                String bookingCode = inv.getBooking() != null ? inv.getBooking().getBookingCode() : "N/A";
                String customerName = "Guest";
                if (inv.getCustomer() != null && inv.getCustomer().getProfile() != null) {
                    customerName = inv.getCustomer().getProfile().getFullName();
                } else if (inv.getBooking() != null && inv.getBooking().getCustomer() != null && inv.getBooking().getCustomer().getProfile() != null) {
                    customerName = inv.getBooking().getCustomer().getProfile().getFullName();
                }
                String total = inv.getTotalAmount() != null ? inv.getTotalAmount().toPlainString() : "0";
                String paid = inv.getPaidAmount() != null ? inv.getPaidAmount().toPlainString() : "0";
                String status = inv.getStatus() != null ? inv.getStatus().name() : "PENDING";
                String date = inv.getTimestamp() != null ? inv.getTimestamp().format(dtf) : "N/A";

                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escapeCsv(invId), escapeCsv(bookingCode), escapeCsv(customerName),
                        escapeCsv(total), escapeCsv(paid), escapeCsv(status), escapeCsv(date)));
            }
            writer.flush();
        }
    }

    @GetMapping("/manager/reports/occupancy/export")
    public void exportManagerOccupancyReport(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"occupancy_room_utilization.csv\"");

        List<Room> rooms = roomRepository.findAll();
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write("Room ID,Room Number,Type,Base Price (VND),Status,Capacity,Area (m2)\n");

            for (Room r : rooms) {
                String rId = String.valueOf(r.getId());
                String rNum = r.getRoomNumber();
                String rType = r.getRoomType() != null ? r.getRoomType().name() : "N/A";
                String price = r.getBasePrice() != null ? r.getBasePrice().toPlainString() : "0";
                String status = r.getStatus() != null ? r.getStatus().name() : "AVAILABLE";
                String cap = r.getCapacity() != null ? String.valueOf(r.getCapacity()) : "2";
                String area = r.getArea() != null ? String.valueOf(r.getArea()) : "35";

                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escapeCsv(rId), escapeCsv(rNum), escapeCsv(rType), escapeCsv(price),
                        escapeCsv(status), escapeCsv(cap), escapeCsv(area)));
            }
            writer.flush();
        }
    }

    @GetMapping("/manager/inventory/export-csv")
    public void exportManagerInventoryReport(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"inventory_stock_report.csv\"");

        List<deepbluehaven.pojo.InventoryItem> items = inventoryItemRepository.findAll();
        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(response.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write("Item ID,Item Name,Unit,Quantity In Stock,Min Threshold,Supplier,Status\n");

            for (deepbluehaven.pojo.InventoryItem item : items) {
                String idStr = "#ITEM-" + item.getId();
                String name = item.getName() != null ? item.getName() : "N/A";
                String unit = item.getUnit() != null ? item.getUnit() : "pcs";
                int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                int threshold = item.getMinThreshold() != null ? item.getMinThreshold() : 0;
                String supplier = item.getSupplier() != null ? item.getSupplier().getName() : "N/A";
                String status = qty <= threshold ? "LOW_STOCK" : "IN_STOCK";

                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%d\",\"%d\",\"%s\",\"%s\"\n",
                        escapeCsv(idStr), escapeCsv(name), escapeCsv(unit),
                        qty, threshold, escapeCsv(supplier), escapeCsv(status)));
            }
            writer.flush();
        }
    }

    @GetMapping("/manager/revenue/export-csv")
    public void exportManagerRevenueReport(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        exportManagerFinancialReport(response);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    @GetMapping("/manager/pricing")
    public String managerPricing(@RequestParam(value = "tab", required = false, defaultValue = "tab-pricing-rules") String tab,
                                 Model model) {
        model.addAttribute("pricingRules", pricingRuleRepository.findAll());
        model.addAttribute("discounts", discountRepository.findAll());
        model.addAttribute("membershipTiers", membershipTierRepository.findAllOrderedForProgression());
        model.addAttribute("servicePoints", servicePointRepository.findAll());
        model.addAttribute("serviceCategories", deepbluehaven.pojo.enums.ServiceCategory.values());
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", "pricing");
        return "manager/pricing";
    }

    @PostMapping("/manager/pricing/service-points/save")
    public String saveServicePoint(@RequestParam(value = "id", required = false) Long id,
                                   @RequestParam("serviceCategory") deepbluehaven.pojo.enums.ServiceCategory serviceCategory,
                                   @RequestParam("calculationType") deepbluehaven.pojo.enums.CalculationType calculationType,
                                   @RequestParam(value = "fixedPoints", required = false) Integer fixedPoints,
                                   @RequestParam(value = "rewardPercentage", required = false) BigDecimal rewardPercentage,
                                   @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
                                   RedirectAttributes redirectAttrs,
                                   HttpSession session) {
        try {
            deepbluehaven.pojo.ServicePoint sp;
            if (id != null) {
                sp = servicePointRepository.findById(id).orElse(new deepbluehaven.pojo.ServicePoint());
            } else {
                sp = servicePointRepository.findByServiceCategory(serviceCategory).orElse(new deepbluehaven.pojo.ServicePoint());
            }
            sp.setServiceCategory(serviceCategory);
            sp.setCalculationType(calculationType);
            sp.setFixedPoints(fixedPoints);
            sp.setRewardPercentage(rewardPercentage);
            sp.setIsActive(isActive);
            servicePointRepository.save(sp);

            logService.log(ObjectType.SYSTEM, ActionCode.UPDATE, sp.getId(),
                    "Manager saved loyalty point rule for category " + serviceCategory, session);

            redirectAttrs.addFlashAttribute("successMessage", "Loyalty point rule for " + serviceCategory.getDisplayName() + " saved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save loyalty point rule: " + e.getMessage());
        }
        return "redirect:/manager/pricing?tab=tab-service-points";
    }

    @PostMapping("/manager/pricing/membership-tiers/save")
    public String saveMembershipTier(@RequestParam("id") Long id,
                                    @RequestParam("minSpent") BigDecimal minSpent,
                                    @RequestParam("minPoints") Integer minPoints,
                                    @RequestParam("pointMultiplier") BigDecimal pointMultiplier,
                                    @RequestParam("discountRate") BigDecimal discountRate,
                                    @RequestParam(value = "description", required = false) String description,
                                    RedirectAttributes redirectAttrs) {
        System.out.println("[ManagerController] Received saveMembershipTier POST request for id: " + id + ", minSpent: " + minSpent + ", minPoints: " + minPoints);
        try {
            deepbluehaven.pojo.MembershipTier tier = membershipTierRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Membership Tier not found: " + id));
            tier.setMinSpent(minSpent);
            tier.setMinPoints(minPoints);
            tier.setPointMultiplier(pointMultiplier);
            tier.setDiscountRate(discountRate);
            if (description != null) {
                tier.setDescription(description);
            }
            membershipTierRepository.save(tier);
            System.out.println("[ManagerController] Successfully saved Membership Tier: " + tier.getTierName());
            redirectAttrs.addFlashAttribute("successMessage", "Membership Tier rules for " + tier.getTierName() + " updated successfully!");
        } catch (Exception e) {
            System.err.println("[ManagerController] Error saving Membership Tier: " + e.getMessage());
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save Membership Tier rule: " + e.getMessage());
        }
        return "redirect:/manager/pricing?tab=tab-membership-tiers";
    }

    private Long getLoggedInWorkerId(jakarta.servlet.http.HttpSession session) {
        if (session == null) return null;
        Object id = session.getAttribute("loggedInWorkerId");
        return id instanceof Long ? (Long) id : null;
    }

    @PostMapping("/manager/pricing/rules/save")
    public String savePricingRule(@RequestParam(value = "id", required = false) Long id,
                                  @RequestParam String roomType,
                                  @RequestParam BigDecimal multiplier,
                                  @RequestParam String startDate,
                                  @RequestParam String endDate,
                                  RedirectAttributes redirectAttrs,
                                  jakarta.servlet.http.HttpSession session) {
        System.out.println("[ManagerController] Received savePricingRule POST request for id: " + id + ", roomType: " + roomType);
        try {
            RoomType rt;
            try {
                if ("DELUXE".equalsIgnoreCase(roomType)) rt = RoomType.SUITE;
                else if ("PRESIDENTIAL".equalsIgnoreCase(roomType)) rt = RoomType.PRESIDENT;
                else rt = RoomType.valueOf(roomType.toUpperCase());
            } catch (Exception ex) {
                rt = RoomType.STANDARD;
            }

            PricingRule rule;
            if (id != null) {
                rule = pricingRuleRepository.findById(id).orElse(new PricingRule());
            } else {
                rule = new PricingRule();
            }
            rule.setRoomType(rt);
            rule.setMultiplier(multiplier);
            rule.setStartDate(LocalDate.parse(startDate));
            rule.setEndDate(LocalDate.parse(endDate));
            pricingRuleRepository.save(rule);

            logService.log(ObjectType.SYSTEM, ActionCode.UPDATE, rule.getId(),
                    "Manager saved pricing rule for " + rt + " (multiplier: " + multiplier + ")", session);

            System.out.println("[ManagerController] Successfully saved pricing rule #" + rule.getId() + " for " + rt);
            redirectAttrs.addFlashAttribute("successMessage", "Pricing rule saved successfully!");
        } catch (Exception e) {
            System.err.println("[ManagerController] Error saving pricing rule: " + e.getMessage());
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save pricing rule: " + e.getMessage());
        }
        return "redirect:/manager/pricing?tab=tab-pricing-rules";
    }

    @PostMapping("/manager/pricing/discounts/save")
    public String saveDiscount(@RequestParam String code,
                               @RequestParam String type,
                               @RequestParam BigDecimal discountValue,
                               @RequestParam(required = false) String endDate,
                               RedirectAttributes redirectAttrs,
                               jakarta.servlet.http.HttpSession session) {
        try {
            DiscountType dt;
            try {
                if ("FIXED".equalsIgnoreCase(type)) dt = DiscountType.FIXED_AMOUNT;
                else dt = DiscountType.valueOf(type.toUpperCase());
            } catch (Exception ex) {
                dt = DiscountType.PERCENTAGE;
            }

            Discount discount = new Discount();
            discount.setCode(code.toUpperCase());
            discount.setType(dt);
            discount.setDiscountValue(discountValue);
            if (endDate != null && !endDate.isBlank()) {
                discount.setEndDate(LocalDate.parse(endDate));
            }
            discount.setIsActive(true);
            discountRepository.save(discount);

            logService.log(ObjectType.SYSTEM, ActionCode.UPDATE, discount.getId(),
                    "Manager created discount code: " + code.toUpperCase(), session);

            redirectAttrs.addFlashAttribute("successMessage", "Discount code saved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save discount: " + e.getMessage());
        }
        return "redirect:/manager/pricing?tab=tab-discounts";
    }

    @PostMapping("/manager/rooms/{id}/status")
    public String updateRoomStatus(@PathVariable Long id,
                                   @RequestParam RoomStatus status,
                                   RedirectAttributes redirectAttrs,
                                   jakarta.servlet.http.HttpSession session) {
        try {
            Room room = roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found"));
            RoomStatus oldStatus = room.getStatus();
            room.setStatus(status);
            roomRepository.save(room);

            Worker worker = null;
            Long wId = getLoggedInWorkerId(session);
            if (wId != null) worker = workerRepository.findById(wId).orElse(null);

            logService.logRoomStatusChange(room, oldStatus, status, worker);

            redirectAttrs.addFlashAttribute("successMessage", "Room #" + room.getRoomNumber() + " status updated to " + status);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to update room status: " + e.getMessage());
        }
        return "redirect:/manager/rooms";
    }

    @PostMapping("/manager/tasks/assign")
    public String assignTask(@RequestParam Long roomId,
                             @RequestParam Long assigneeId,
                             @RequestParam(required = false) String action,
                             RedirectAttributes redirectAttrs,
                             jakarta.servlet.http.HttpSession session) {
        try {
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));
            Worker assignee = workerRepository.findById(assigneeId).orElseThrow(() -> new IllegalArgumentException("Worker not found"));
            TaskType taskType = taskTypeRepository.findAll().stream().findFirst().orElseGet(() -> {
                TaskType tt = new TaskType();
                tt.setName("Cleaning & Prep");
                tt.setDescription("General room cleaning and housekeeping prep");
                return taskTypeRepository.save(tt);
            });

            Task task = new Task();
            task.setRoom(room);
            task.setAssignedTo(assignee);
            task.setTaskType(taskType);
            task.setStatus(TaskStatus.ASSIGNED);
            task.setAction(action != null && !action.isBlank() ? action : "Cleaning and preparing room #" + room.getRoomNumber());
            taskRepository.save(task);

            RoomStatus oldStatus = room.getStatus();
            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);

            Worker worker = null;
            Long wId = getLoggedInWorkerId(session);
            if (wId != null) worker = workerRepository.findById(wId).orElse(null);

            logService.logRoomStatusChange(room, oldStatus, RoomStatus.CLEANING, worker);
            logService.log(ObjectType.WORKER, ActionCode.UPDATE, assigneeId,
                    "Manager assigned housekeeping task for Room #" + room.getRoomNumber() + " to Worker #" + assigneeId, session);

            redirectAttrs.addFlashAttribute("successMessage", "Task assigned to " + (assignee.getProfile() != null ? assignee.getProfile().getFullName() : assignee.getUsername()) + " for Room #" + room.getRoomNumber());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to assign task: " + e.getMessage());
        }
        return "redirect:/manager/rooms?tab=housekeeping";
    }

    @PostMapping("/manager/confirm-booking/{id}")
    public String confirmBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttrs,
                                 jakarta.servlet.http.HttpSession session) {
        try {
            Long wId = getLoggedInWorkerId(session);
            boolean success = bookingService.confirmBookingByStaff(id, wId != null ? wId : 1L);
            if (success) {
                logService.log(ObjectType.BOOKING, ActionCode.UPDATE, id,
                        "Manager confirmed booking #" + id, session);
                redirectAttrs.addFlashAttribute("successMessage", "Booking #" + id + " has been successfully CONFIRMED!");
            } else {
                redirectAttrs.addFlashAttribute("errorMessage", "Failed to confirm booking #" + id);
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager/dashboard";
    }

    @PostMapping("/manager/rooms/save")
    public String saveRoom(@RequestParam(required = false) Long id,
                           @RequestParam String roomNumber,
                           @RequestParam String roomType,
                           @RequestParam Integer capacity,
                           @RequestParam BigDecimal basePrice,
                           @RequestParam(required = false) Integer area,
                           @RequestParam RoomStatus status,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) String planUrl,
                           @RequestParam(required = false) List<deepbluehaven.pojo.enums.Amenity> amenities,
                           @RequestParam(required = false) List<deepbluehaven.pojo.enums.RoomTag> tags,
                           @RequestParam(required = false) String imageUrl,
                           RedirectAttributes redirectAttrs,
                           jakarta.servlet.http.HttpSession session) {
        try {
            RoomType rt;
            try {
                if ("DELUXE".equalsIgnoreCase(roomType)) rt = RoomType.SUITE;
                else if ("PRESIDENTIAL".equalsIgnoreCase(roomType)) rt = RoomType.PRESIDENT;
                else rt = RoomType.valueOf(roomType.toUpperCase());
            } catch (Exception ex) {
                rt = RoomType.STANDARD;
            }

            Room room;
            boolean isNew = (id == null);
            if (id != null) {
                room = roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found"));
            } else {
                room = new Room();
                Resort resort = resortRepository.findAll().stream().findFirst().orElseGet(() -> {
                    Resort r = new Resort();
                    r.setName("Deepblue Haven Resort");
                    r.setLocation("Da Nang, Vietnam");
                    return resortRepository.save(r);
                });
                room.setResort(resort);
            }

            room.setRoomNumber(roomNumber);
            room.setRoomType(rt);
            room.setCapacity(capacity);
            room.setBasePrice(basePrice);
            if (area != null) room.setArea(area);
            if (description != null) room.setDescription(description);
            if (planUrl != null) room.setPlanUrl(planUrl);
            room.setStatus(status);

            if (amenities != null) {
                room.getAmenities().clear();
                room.getAmenities().addAll(amenities);
            }
            if (tags != null) {
                room.getTags().clear();
                room.getTags().addAll(tags);
            }

            if (imageUrl != null && !imageUrl.isBlank()) {
                room.getImages().clear();
                room.getImages().add(imageUrl.trim());
            }

            roomRepository.save(room);
            logService.log(ObjectType.ROOM, isNew ? ActionCode.CREATE : ActionCode.UPDATE, room.getId(),
                    "Manager saved room #" + roomNumber + " (" + rt + ")", session);

            redirectAttrs.addFlashAttribute("successMessage", "Room #" + roomNumber + " saved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save room: " + e.getMessage());
        }
        return "redirect:/manager/rooms";
    }

    @PostMapping("/manager/services/save")
    public String saveService(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam String category,
                              @RequestParam BigDecimal basePrice,
                              @RequestParam ServiceStatus status,
                              @RequestParam(required = false) String imageUrl,
                              RedirectAttributes redirectAttrs,
                              jakarta.servlet.http.HttpSession session) {
        try {
            ServiceCategory serviceCategory;
            try {
                if ("FNB".equalsIgnoreCase(category) || "Food & Beverage".equalsIgnoreCase(category)) {
                    serviceCategory = ServiceCategory.FOOD_BEVERAGE;
                } else {
                    serviceCategory = ServiceCategory.valueOf(category.toUpperCase());
                }
            } catch (Exception ex) {
                serviceCategory = ServiceCategory.OTHER;
            }

            Service service;
            boolean isNew = (id == null);
            if (id != null) {
                service = serviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Service not found"));
            } else {
                service = new Service();
                Resort resort = resortRepository.findAll().stream().findFirst().orElseGet(() -> {
                    Resort r = new Resort();
                    r.setName("Deepblue Haven Resort");
                    r.setLocation("Da Nang, Vietnam");
                    return resortRepository.save(r);
                });
                service.setResort(resort);
                service.setType(serviceCategory.name());
                service.setUnit("service");
            }

            service.setName(name);
            service.setCategory(serviceCategory);
            service.setBasePrice(basePrice);
            service.setStatus(status);

            if (imageUrl != null && !imageUrl.isBlank()) {
                service.getImages().clear();
                service.getImages().add(imageUrl.trim());
            }

            serviceRepository.save(service);
            logService.log(ObjectType.SERVICE, isNew ? ActionCode.CREATE : ActionCode.UPDATE, service.getId(),
                    "Manager saved service '" + name + "'", session);

            redirectAttrs.addFlashAttribute("successMessage", "Service '" + name + "' saved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save service: " + e.getMessage());
        }
        return "redirect:/manager/services";
    }

    @PostMapping("/manager/services/{id}/status")
    public String updateServiceStatus(@PathVariable Long id,
                                      @RequestParam ServiceStatus status,
                                      RedirectAttributes redirectAttrs,
                                      jakarta.servlet.http.HttpSession session) {
        try {
            Service service = serviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Service not found"));
            service.setStatus(status);
            serviceRepository.save(service);
            logService.log(ObjectType.SERVICE, ActionCode.UPDATE, id,
                    "Manager updated service status for '" + service.getName() + "' to " + status, session);
            redirectAttrs.addFlashAttribute("successMessage", "Service '" + service.getName() + "' status updated to " + status);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to update service status: " + e.getMessage());
        }
        return "redirect:/manager/services";
    }

    @GetMapping("/manager/settings")
    public String managerSettings(Model model) {
        model.addAttribute("activePage", "settings");
        return "manager/settings";
    }

    @GetMapping("/manager/logs")
    public String managerLogs(Model model) {
        model.addAttribute("activePage", "logs");
        model.addAttribute("operationalLogs", logService.getManagerOperationalLogs());
        model.addAttribute("bookingLogs", logService.getManagerBookingLogs());
        model.addAttribute("roomStatusLogs", logService.getManagerRoomStatusLogs());
        model.addAttribute("invoiceStatusLogs", logService.getManagerInvoiceStatusLogs());
        return "manager/logs";
    }

    @GetMapping("/manager/comments")
    public String managerComments(@RequestParam(value = "filter", required = false, defaultValue = "all") String filter,
                                  @RequestParam(value = "inquiryStatus", required = false, defaultValue = "ALL") String inquiryStatus,
                                  Model model) {
        model.addAttribute("activePage", "comments");
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentInquiryStatus", inquiryStatus);
        model.addAttribute("comments", commentAndInquiryService.getComments(filter));
        model.addAttribute("inquiries", commentAndInquiryService.getInquiries(inquiryStatus));
        model.addAttribute("statistics", commentAndInquiryService.getStatistics());
        return "manager/comments";
    }

    @PostMapping("/manager/comments/{id}/reply")
    public String managerReplyComment(@PathVariable("id") Long id,
                                      @RequestParam("response") String response,
                                      RedirectAttributes redirectAttrs,
                                      jakarta.servlet.http.HttpServletRequest req) {
        try {
            Long workerId = (Long) req.getSession().getAttribute("loggedInWorkerId");
            boolean success = commentAndInquiryService.replyToComment(id, response, workerId);
            if (success) {
                redirectAttrs.addFlashAttribute("successMessage", "Replied to customer feedback successfully!");
            } else {
                redirectAttrs.addFlashAttribute("errorMessage", "Comment not found.");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager/comments";
    }

    @PostMapping("/manager/comments/{id}/resolve")
    public String managerResolveComment(@PathVariable("id") Long id,
                                        @RequestParam(value = "isResolved", required = false, defaultValue = "true") Boolean isResolved,
                                        RedirectAttributes redirectAttrs) {
        try {
            commentAndInquiryService.toggleResolveComplaint(id, isResolved);
            redirectAttrs.addFlashAttribute("successMessage", "Feedback status updated.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager/comments";
    }

    @PostMapping("/manager/inquiries/{id}/update")
    public String managerUpdateInquiry(@PathVariable("id") Long id,
                                       @RequestParam("status") deepbluehaven.pojo.enums.InquiryStatus status,
                                       @RequestParam(value = "replyNotes", required = false) String replyNotes,
                                       RedirectAttributes redirectAttrs,
                                       jakarta.servlet.http.HttpServletRequest req) {
        try {
            Long workerId = (Long) req.getSession().getAttribute("loggedInWorkerId");
            boolean success = commentAndInquiryService.updateInquiry(id, status, replyNotes, workerId);
            if (success) {
                redirectAttrs.addFlashAttribute("successMessage", "Contact inquiry #" + id + " updated to " + status);
            } else {
                redirectAttrs.addFlashAttribute("errorMessage", "Inquiry not found.");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/manager/comments";
    }

    @GetMapping("/manager/chat")
    public String managerChat(Model model) {
        model.addAttribute("activePage", "chat");
        model.addAttribute("sessions", chatService.getStaffSessions("ALL"));
        return "manager/chat";
    }

    @GetMapping("/manager/inventory")
    public String managerInventory(Model model) {
        List<InventoryItem> items = inventoryItemRepository.findAllWithSupplierAndResort();
        List<Supplier> suppliers = supplierRepository.findAllWithItems();
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findAllWithItemOrderByIdDesc();

        long totalItems = items.size();
        long lowStockCount = items.stream().filter(i -> i.getQuantity() != null && i.getMinThreshold() != null && i.getQuantity() <= i.getMinThreshold() && i.getQuantity() > 0).count();
        long outOfStockCount = items.stream().filter(i -> i.getQuantity() == null || i.getQuantity() <= 0).count();
        long totalSuppliers = suppliers.size();

        model.addAttribute("items", items);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("totalSuppliers", totalSuppliers);
        model.addAttribute("activePage", "inventory");
        return "manager/inventory";
    }

    @PostMapping("/manager/inventory/save")
    public String saveInventoryItem(@RequestParam(value = "id", required = false) Long id,
                                   @RequestParam("name") String name,
                                   @RequestParam("unit") String unit,
                                   @RequestParam("quantity") Integer quantity,
                                   @RequestParam("minThreshold") Integer minThreshold,
                                   @RequestParam("supplierId") Long supplierId,
                                   RedirectAttributes redirectAttrs,
                                   HttpSession session) {
        try {
            InventoryItem item;
            boolean isNew = (id == null);
            if (id != null) {
                item = inventoryItemRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
            } else {
                item = new InventoryItem();
                Resort resort = resortRepository.findAll().stream().findFirst().orElseGet(() -> {
                    Resort r = new Resort();
                    r.setName("Deepblue Haven Resort");
                    r.setLocation("Da Nang, Vietnam");
                    return resortRepository.save(r);
                });
                item.setResort(resort);
            }

            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

            item.setName(name);
            item.setUnit(unit);
            item.setQuantity(quantity);
            item.setMinThreshold(minThreshold);
            item.setSupplier(supplier);

            inventoryItemRepository.save(item);
            logService.log(ObjectType.SYSTEM, isNew ? ActionCode.CREATE : ActionCode.UPDATE, item.getId(),
                    "Manager saved inventory item: " + name + " (Qty: " + quantity + " " + unit + ")", session);

            redirectAttrs.addFlashAttribute("successMessage", "Inventory item '" + name + "' saved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save item: " + e.getMessage());
        }
        return "redirect:/manager/inventory";
    }

    @PostMapping("/manager/inventory/transaction")
    public String recordInventoryTransaction(@RequestParam("itemId") Long itemId,
                                             @RequestParam("type") String type,
                                             @RequestParam("quantity") Integer quantity,
                                             @RequestParam(value = "reason", required = false) String reason,
                                             RedirectAttributes redirectAttrs,
                                             HttpSession session) {
        try {
            InventoryItem item = inventoryItemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));

            int qty = (quantity != null && quantity > 0) ? quantity : 1;
            int change = "EXPORT".equalsIgnoreCase(type) ? -qty : qty;
            int currentQty = item.getQuantity() != null ? item.getQuantity() : 0;
            int newQty = Math.max(0, currentQty + change);

            item.setQuantity(newQty);
            inventoryItemRepository.save(item);

            InventoryTransaction tx = new InventoryTransaction();
            tx.setInventoryItem(item);
            tx.setChangeAmount(change);
            String desc = reason != null && !reason.isBlank() ? reason : ("IMPORT".equalsIgnoreCase(type) ? "Restock shipment" : "Housekeeping usage");
            tx.setReason(desc);
            inventoryTransactionRepository.save(tx);

            logService.log(ObjectType.SYSTEM, ActionCode.UPDATE, item.getId(),
                    "Manager recorded inventory " + type + " of " + qty + " " + item.getUnit() + " for " + item.getName() + " (New Qty: " + newQty + ")", session);

            redirectAttrs.addFlashAttribute("successMessage", "Recorded " + type + " of " + qty + " " + item.getUnit() + " for '" + item.getName() + "'. New balance: " + newQty);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to record transaction: " + e.getMessage());
        }
        return "redirect:/manager/inventory";
    }

    @PostMapping("/manager/inventory/supplier/save")
    public String saveSupplier(@RequestParam(value = "id", required = false) Long id,
                               @RequestParam("name") String name,
                               @RequestParam("phoneNumber") String phoneNumber,
                               @RequestParam("email") String email,
                               @RequestParam("address") String address,
                               RedirectAttributes redirectAttrs,
                               HttpSession session) {
        try {
            Supplier supplier;
            boolean isNew = (id == null);
            if (id != null) {
                supplier = supplierRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
            } else {
                supplier = new Supplier();
            }

            supplier.setName(name);
            supplier.setPhoneNumber(phoneNumber);
            supplier.setEmail(email);
            supplier.setAddress(address);

            supplierRepository.save(supplier);
            logService.log(ObjectType.SYSTEM, isNew ? ActionCode.CREATE : ActionCode.UPDATE, supplier.getId(),
                    "Manager saved supplier: " + name, session);

            redirectAttrs.addFlashAttribute("successMessage", "Supplier '" + name + "' saved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save supplier: " + e.getMessage());
        }
        return "redirect:/manager/inventory?tab=suppliers";
    }

    @PostMapping("/manager/inventory/{id}/delete")
    public String deleteInventoryItem(@PathVariable("id") Long id,
                                      RedirectAttributes redirectAttrs,
                                      HttpSession session) {
        try {
            InventoryItem item = inventoryItemRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));
            String itemName = item.getName();
            inventoryItemRepository.delete(item);
            logService.log(ObjectType.SYSTEM, ActionCode.DELETE, id,
                    "Manager deleted inventory item: " + itemName, session);
            redirectAttrs.addFlashAttribute("successMessage", "Deleted inventory item '" + itemName + "'");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to delete item: " + e.getMessage());
        }
        return "redirect:/manager/inventory";
    }
}
package deepbluehaven.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import deepbluehaven.pojo.Room;
import deepbluehaven.pojo.Task;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.DiscountType;
import deepbluehaven.pojo.enums.Role;
import deepbluehaven.pojo.enums.RoomStatus;
import deepbluehaven.pojo.enums.RoomType;
import deepbluehaven.pojo.enums.TaskStatus;
import deepbluehaven.repositories.DiscountRepository;
import deepbluehaven.repositories.PricingRuleRepository;
import deepbluehaven.repositories.RoomRepository;
import deepbluehaven.pojo.TaskType;
import deepbluehaven.repositories.InvoiceRepository;
import deepbluehaven.repositories.ServiceRepository;
import deepbluehaven.repositories.TaskRepository;
import deepbluehaven.repositories.TaskTypeRepository;
import deepbluehaven.repositories.WorkerRepository;
import deepbluehaven.services.BookingService;
import deepbluehaven.services.ManagerDashboardService;

import deepbluehaven.pojo.Resort;
import deepbluehaven.pojo.Service;
import deepbluehaven.pojo.enums.ServiceCategory;
import deepbluehaven.pojo.enums.ServiceStatus;
import deepbluehaven.repositories.ResortRepository;

@Controller
public class ManagerController {

    private final ManagerDashboardService managerDashboardService;
    private final BookingService bookingService;
    private final PricingRuleRepository pricingRuleRepository;
    private final DiscountRepository discountRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;
    private final WorkerRepository workerRepository;
    private final TaskRepository taskRepository;
    private final TaskTypeRepository taskTypeRepository;
    private final InvoiceRepository invoiceRepository;
    private final ResortRepository resortRepository;

    public ManagerController(ManagerDashboardService managerDashboardService,
                             BookingService bookingService,
                             PricingRuleRepository pricingRuleRepository,
                             DiscountRepository discountRepository,
                             RoomRepository roomRepository,
                             ServiceRepository serviceRepository,
                             WorkerRepository workerRepository,
                             TaskRepository taskRepository,
                             TaskTypeRepository taskTypeRepository,
                             InvoiceRepository invoiceRepository,
                             ResortRepository resortRepository) {
        this.managerDashboardService = managerDashboardService;
        this.bookingService = bookingService;
        this.pricingRuleRepository = pricingRuleRepository;
        this.discountRepository = discountRepository;
        this.roomRepository = roomRepository;
        this.serviceRepository = serviceRepository;
        this.workerRepository = workerRepository;
        this.taskRepository = taskRepository;
        this.taskTypeRepository = taskTypeRepository;
        this.invoiceRepository = invoiceRepository;
        this.resortRepository = resortRepository;
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model) {
        ManagerDashboardDTO dashboardData = managerDashboardService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
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
        List<Worker> housekeepers = workerRepository.findAll().stream()
                .filter(w -> w.getProfile() != null && w.getProfile().getRole() == Role.HOUSEKEEPER)
                .toList();

        model.addAttribute("rooms", rooms);
        model.addAttribute("housekeepers", housekeepers);
        model.addAttribute("activePage", "rooms");
        return "manager/rooms";
    }

    @GetMapping("/manager/services")
    public String managerServices(Model model) {
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("activePage", "services");
        return "manager/services";
    }

    @GetMapping("/manager/staff")
    public String managerStaff(Model model) {
        model.addAttribute("staffList", workerRepository.findAll());
        model.addAttribute("activePage", "staff");
        return "manager/staff";
    }

    @GetMapping("/manager/revenue")
    public String managerRevenue(Model model) {
        model.addAttribute("invoices", invoiceRepository.findAllWithBookingAndCustomer());
        model.addAttribute("activePage", "revenue");
        return "manager/revenue";
    }

    @GetMapping("/manager/reports")
    public String managerReports(Model model) {
        model.addAttribute("activePage", "reports");
        return "manager/reports";
    }

    @GetMapping("/manager/pricing")
    public String managerPricing(Model model) {
        model.addAttribute("pricingRules", pricingRuleRepository.findAll());
        model.addAttribute("discounts", discountRepository.findAll());
        model.addAttribute("activePage", "pricing");
        return "manager/pricing";
    }

    @PostMapping("/manager/pricing/rules/save")
    public String savePricingRule(@RequestParam String roomType,
                                  @RequestParam BigDecimal multiplier,
                                  @RequestParam String startDate,
                                  @RequestParam String endDate,
                                  RedirectAttributes redirectAttrs) {
        try {
            RoomType rt;
            try {
                if ("DELUXE".equalsIgnoreCase(roomType)) rt = RoomType.SUITE;
                else if ("PRESIDENTIAL".equalsIgnoreCase(roomType)) rt = RoomType.PRESIDENT;
                else rt = RoomType.valueOf(roomType.toUpperCase());
            } catch (Exception ex) {
                rt = RoomType.STANDARD;
            }

            PricingRule rule = new PricingRule();
            rule.setRoomType(rt);
            rule.setMultiplier(multiplier);
            rule.setStartDate(LocalDate.parse(startDate));
            rule.setEndDate(LocalDate.parse(endDate));
            pricingRuleRepository.save(rule);
            redirectAttrs.addFlashAttribute("successMessage", "Pricing rule saved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save pricing rule: " + e.getMessage());
        }
        return "redirect:/manager/pricing";
    }

    @PostMapping("/manager/pricing/discounts/save")
    public String saveDiscount(@RequestParam String code,
                               @RequestParam String type,
                               @RequestParam BigDecimal discountValue,
                               @RequestParam(required = false) String endDate,
                               RedirectAttributes redirectAttrs) {
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
            redirectAttrs.addFlashAttribute("successMessage", "Discount code saved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save discount: " + e.getMessage());
        }
        return "redirect:/manager/pricing";
    }

    @PostMapping("/manager/rooms/{id}/status")
    public String updateRoomStatus(@PathVariable Long id,
                                   @RequestParam RoomStatus status,
                                   RedirectAttributes redirectAttrs) {
        try {
            Room room = roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found"));
            room.setStatus(status);
            roomRepository.save(room);
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
                             RedirectAttributes redirectAttrs) {
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

            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);

            redirectAttrs.addFlashAttribute("successMessage", "Task assigned to " + (assignee.getProfile() != null ? assignee.getProfile().getFullName() : assignee.getUsername()) + " for Room #" + room.getRoomNumber());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to assign task: " + e.getMessage());
        }
        return "redirect:/manager/rooms";
    }

    @PostMapping("/manager/confirm-booking/{id}")
    public String confirmBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        try {
            boolean success = bookingService.confirmBookingByStaff(id, 1L);
            if (success) {
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
                           @RequestParam(required = false) String imageUrl,
                           RedirectAttributes redirectAttrs) {
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
            room.setStatus(status);

            if (imageUrl != null && !imageUrl.isBlank()) {
                room.getImages().clear();
                room.getImages().add(imageUrl.trim());
            }

            roomRepository.save(room);
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
                              RedirectAttributes redirectAttrs) {
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
            redirectAttrs.addFlashAttribute("successMessage", "Service '" + name + "' saved successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to save service: " + e.getMessage());
        }
        return "redirect:/manager/services";
    }

    @PostMapping("/manager/services/{id}/status")
    public String updateServiceStatus(@PathVariable Long id,
                                      @RequestParam ServiceStatus status,
                                      RedirectAttributes redirectAttrs) {
        try {
            Service service = serviceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Service not found"));
            service.setStatus(status);
            serviceRepository.save(service);
            redirectAttrs.addFlashAttribute("successMessage", "Service '" + service.getName() + "' status updated to " + status);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Failed to update service status: " + e.getMessage());
        }
        return "redirect:/manager/services";
    }
}
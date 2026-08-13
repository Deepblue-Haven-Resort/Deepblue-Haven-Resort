package deepbluehaven.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import deepbluehaven.dto.ReceptionistDTO;
import deepbluehaven.pojo.CustomerProfile;
import deepbluehaven.pojo.Worker;
import deepbluehaven.repositories.WorkerRepository;
import deepbluehaven.services.BookingService;
import deepbluehaven.services.ReceptionistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/receptionist")
public class ReceptionistController {

    private final ReceptionistService receptionistService;
    private final BookingService bookingService;
    private final WorkerRepository workerRepository;

    public ReceptionistController(ReceptionistService receptionistService, BookingService bookingService, WorkerRepository workerRepository) {
        this.receptionistService = receptionistService;
        this.bookingService = bookingService;
        this.workerRepository = workerRepository;
    }

    @GetMapping("")
    public String index() {
        return "redirect:/receptionist/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        ReceptionistDTO.DashboardView dashboardData = receptionistService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("activePage", "dashboard");
        return "receptionist/dashboard";
    }

    @GetMapping("/bookings")
    public String bookingsPage(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookingsForStaff());
        model.addAttribute("activePage", "bookings");
        return "receptionist/bookings";
    }

    @GetMapping("/check-in")
    public String checkInPage(Model model) {
        List<ReceptionistDTO.CheckInQueueItem> checkInQueue = receptionistService.getPendingCheckInQueue();
        model.addAttribute("checkInQueue", checkInQueue);
        model.addAttribute("checkInRequest", new ReceptionistDTO.CheckInRequest());
        model.addAttribute("activePage", "check-in");
        return "receptionist/checkin";
    }

    @PostMapping("/check-in/execute")
    public String executeCheckIn(@ModelAttribute ReceptionistDTO.CheckInRequest request, RedirectAttributes redirectAttrs, HttpServletRequest req) {
        try {
            Worker receptionist = getActiveReceptionist(req);
            receptionistService.executeCheckIn(request, receptionist);
            redirectAttrs.addFlashAttribute("successMessage", "Check-In successfully completed for Booking #" + request.getBookingId());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Check-In failed: " + e.getMessage());
        }
        return "redirect:/receptionist/check-in";
    }

    @PostMapping("/confirm-booking/{id}")
    public String confirmBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttrs) {
        try {
            Worker receptionist = getActiveReceptionist();
            boolean success = bookingService.confirmBookingByStaff(id, receptionist != null ? receptionist.getId() : 1L);
            if (success) {
                redirectAttrs.addFlashAttribute("successMessage", "Booking #" + id + " has been successfully CONFIRMED!");
            } else {
                redirectAttrs.addFlashAttribute("errorMessage", "Failed to confirm booking #" + id);
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/receptionist/check-in";
    }

    @GetMapping("/check-out")
    public String checkOutPage(Model model) {
        List<ReceptionistDTO.CheckOutQueueItem> checkOutQueue = receptionistService.getPendingCheckOutQueue();
        model.addAttribute("checkOutQueue", checkOutQueue);
        model.addAttribute("checkOutRequest", new ReceptionistDTO.CheckOutRequest());
        model.addAttribute("activePage", "check-out");
        return "receptionist/checkout";
    }

    @PostMapping("/check-out/execute")
    public String executeCheckOut(@ModelAttribute ReceptionistDTO.CheckOutRequest request, RedirectAttributes redirectAttrs, HttpServletRequest req) {
        try {
            Worker receptionist = getActiveReceptionist(req);
            receptionistService.executeCheckOut(request, receptionist);
            redirectAttrs.addFlashAttribute("successMessage", "Check-Out and Invoice Settlement completed successfully for Booking #" + request.getBookingId());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Check-Out failed: " + e.getMessage());
        }
        return "redirect:/receptionist/check-out";
    }

    @GetMapping("/room-grid")
    public String roomGridPage(Model model) {
        List<ReceptionistDTO.RoomRackItem> roomGrid = receptionistService.getRoomGridData();
        Map<Integer, List<ReceptionistDTO.RoomRackItem>> roomsByFloor = receptionistService.getRoomGridGroupedByFloor();
        model.addAttribute("roomGrid", roomGrid);
        model.addAttribute("roomsByFloor", roomsByFloor);
        model.addAttribute("activePage", "room-grid");
        return "receptionist/room-grid";
    }

    @PostMapping("/walkin-booking")
    public String executeWalkInBooking(@ModelAttribute ReceptionistDTO.WalkInBookingRequest request, RedirectAttributes redirectAttrs, HttpServletRequest req) {
        try {
            Worker receptionist = getActiveReceptionist(req);
            receptionistService.executeWalkInBooking(request, receptionist);
            redirectAttrs.addFlashAttribute("successMessage", "Walk-In Booking created and guest checked in successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Walk-In Booking failed: " + e.getMessage());
        }
        return "redirect:/receptionist/dashboard";
    }

    @GetMapping("/guests")
    public String guestDirectoryPage(Model model, HttpServletRequest req) {
        Worker receptionist = getActiveReceptionist(req);
        if (receptionist == null) {
            return "redirect:/staff-login";
        }
        List<CustomerProfile> customerProfiles = receptionistService.getAllCustomerProfiles();
        model.addAttribute("guests", customerProfiles);
        model.addAttribute("activePage", "guests");
        return "receptionist/guests";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, HttpServletRequest req) {
        Worker receptionist = getActiveReceptionist(req);
        if (receptionist == null) {
            return "redirect:/staff-login";
        }
        model.addAttribute("worker", receptionist);
        model.addAttribute("activePage", "profile");
        return "receptionist/profile";
    }

    @GetMapping("/settings")
    public String receptionistSettings(Model model) {
        model.addAttribute("activePage", "settings");
        return "receptionist/settings";
    }

    private Worker getActiveReceptionist(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loggedInWorkerId") != null) {
            Long workerId = (Long) session.getAttribute("loggedInWorkerId");
            return workerRepository.findById(workerId).orElse(null);
        }
        return getActiveReceptionist();
    }

    private Worker getActiveReceptionist() {
        return workerRepository.findAll().stream().findFirst().orElse(null);
    }
}
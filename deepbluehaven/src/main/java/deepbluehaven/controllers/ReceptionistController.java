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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import deepbluehaven.dto.ReceptionistDTO;
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
    private final deepbluehaven.services.CommentAndInquiryService commentAndInquiryService;
    private final deepbluehaven.services.ChatService chatService;

    public ReceptionistController(ReceptionistService receptionistService,
                                  BookingService bookingService,
                                  WorkerRepository workerRepository,
                                  deepbluehaven.services.CommentAndInquiryService commentAndInquiryService,
                                  deepbluehaven.services.ChatService chatService) {
        this.receptionistService = receptionistService;
        this.bookingService = bookingService;
        this.workerRepository = workerRepository;
        this.commentAndInquiryService = commentAndInquiryService;
        this.chatService = chatService;
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
    public String confirmBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttrs, HttpServletRequest req) {
        try {
            Worker receptionist = getActiveReceptionist(req);
            boolean success = bookingService.confirmBookingByStaff(id, receptionist != null ? receptionist.getId() : 1L);
            if (success) {
                redirectAttrs.addFlashAttribute("successMessage", "Booking #" + id + " has been successfully CONFIRMED!");
            } else {
                redirectAttrs.addFlashAttribute("errorMessage", "Failed to confirm booking #" + id);
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/receptionist/bookings";
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

    @GetMapping("/comments")
    public String receptionistComments(@RequestParam(value = "filter", required = false, defaultValue = "all") String filter,
                                       @RequestParam(value = "inquiryStatus", required = false, defaultValue = "ALL") String inquiryStatus,
                                       Model model) {
        model.addAttribute("activePage", "comments");
        model.addAttribute("currentFilter", filter);
        model.addAttribute("currentInquiryStatus", inquiryStatus);
        model.addAttribute("comments", commentAndInquiryService.getComments(filter));
        model.addAttribute("inquiries", commentAndInquiryService.getInquiries(inquiryStatus));
        model.addAttribute("statistics", commentAndInquiryService.getStatistics());
        return "receptionist/comments";
    }

    @PostMapping("/comments/{id}/reply")
    public String receptionistReplyComment(@PathVariable("id") Long id,
                                           @RequestParam("response") String response,
                                           RedirectAttributes redirectAttrs,
                                           HttpServletRequest req) {
        try {
            Worker worker = getActiveReceptionist(req);
            Long workerId = worker != null ? worker.getId() : null;
            boolean success = commentAndInquiryService.replyToComment(id, response, workerId);
            if (success) {
                redirectAttrs.addFlashAttribute("successMessage", "Replied to customer feedback successfully!");
            } else {
                redirectAttrs.addFlashAttribute("errorMessage", "Comment not found.");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/receptionist/comments";
    }

    @PostMapping("/comments/{id}/resolve")
    public String receptionistResolveComment(@PathVariable("id") Long id,
                                             @RequestParam(value = "isResolved", required = false, defaultValue = "true") Boolean isResolved,
                                             RedirectAttributes redirectAttrs) {
        try {
            commentAndInquiryService.toggleResolveComplaint(id, isResolved);
            redirectAttrs.addFlashAttribute("successMessage", "Feedback status updated.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/receptionist/comments";
    }

    @PostMapping("/inquiries/{id}/update")
    public String receptionistUpdateInquiry(@PathVariable("id") Long id,
                                            @RequestParam("status") deepbluehaven.pojo.enums.InquiryStatus status,
                                            @RequestParam(value = "replyNotes", required = false) String replyNotes,
                                            RedirectAttributes redirectAttrs,
                                            HttpServletRequest req) {
        try {
            Worker worker = getActiveReceptionist(req);
            Long workerId = worker != null ? worker.getId() : null;
            boolean success = commentAndInquiryService.updateInquiry(id, status, replyNotes, workerId);
            if (success) {
                redirectAttrs.addFlashAttribute("successMessage", "Contact inquiry #" + id + " updated to " + status);
            } else {
                redirectAttrs.addFlashAttribute("errorMessage", "Inquiry not found.");
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/receptionist/comments";
    }

    @GetMapping("/chat")
    public String receptionistChat(Model model) {
        model.addAttribute("activePage", "chat");
        model.addAttribute("sessions", chatService.getStaffSessions("ALL"));
        return "receptionist/chat";
    }

    private Worker getActiveReceptionist(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loggedInWorkerId") != null) {
            Long workerId = (Long) session.getAttribute("loggedInWorkerId");
            return workerRepository.findWithProfileAndPermissionsById(workerId)
                    .orElseGet(() -> workerRepository.findById(workerId).orElse(null));
        }
        return getActiveReceptionist();
    }

    private Worker getActiveReceptionist() {
        return workerRepository.findAllWithProfile().stream()
                .filter(w -> w.getProfile() != null && w.getProfile().getRole() == deepbluehaven.pojo.enums.Role.RECEPTIONIST)
                .findFirst()
                .orElseGet(() -> workerRepository.findAllWithProfile().stream().findFirst().orElse(null));
    }
}
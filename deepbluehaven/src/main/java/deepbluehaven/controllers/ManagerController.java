package deepbluehaven.controllers;

import deepbluehaven.dto.ManagerDashboardDTO;
import deepbluehaven.services.BookingService;
import deepbluehaven.services.ManagerDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ManagerController {

    private final ManagerDashboardService managerDashboardService;
    private final BookingService bookingService;

    public ManagerController(ManagerDashboardService managerDashboardService, BookingService bookingService) {
        this.managerDashboardService = managerDashboardService;
        this.bookingService = bookingService;
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard(Model model) {
        ManagerDashboardDTO dashboardData = managerDashboardService.getDashboardData();
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("activePage", "dashboard");

        return "manager/dashboard";
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
}
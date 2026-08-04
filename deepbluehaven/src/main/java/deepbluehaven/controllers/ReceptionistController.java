package deepbluehaven.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReceptionistController {

    @GetMapping("/receptionist/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "receptionist/dashboard";
    }

    @GetMapping("/receptionist/checkin")
    public String checkin(Model model) {
        model.addAttribute("activePage", "checkin");
        return "receptionist/checkin";
    }

    @GetMapping("/receptionist/bookings")
    public String bookings(Model model) {
        model.addAttribute("activePage", "bookings");
        return "receptionist/bookings";
    }

    @GetMapping("/receptionist/guests")
    public String guests(Model model) {
        model.addAttribute("activePage", "guests");
        return "receptionist/guests";
    }

    @GetMapping("/receptionist/profile")
    public String profile(Model model) {
        model.addAttribute("activePage", "profile");
        return "receptionist/profile";
    }

}
